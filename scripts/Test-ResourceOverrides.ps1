param(
    [Parameter(Mandatory)][string]$ProjectRoot,
    [Parameter(Mandatory)][string[]]$Modules,
    [string]$CommonRoot = 'common/src/main/resources'
)

# Run with the project's JDK in JAVA_HOME. Exercises the real processResources tasks,
# including incremental fallback after removing a loader override. No clean build needed.
$ErrorActionPreference = 'Stop'
$projectPath = (Resolve-Path -LiteralPath $ProjectRoot).Path
$commonPath = [IO.Path]::GetFullPath((Join-Path $projectPath $CommonRoot))
$probeDirectory = '__myotus_resource_override_test__'
$createdFiles = [Collections.Generic.List[string]]::new()
$modulePaths = @($Modules | ForEach-Object {
    if ($_ -eq '.') { $projectPath } else { (Resolve-Path -LiteralPath (Join-Path $projectPath $_)).Path }
})
$tasks = @($Modules | ForEach-Object {
    if ($_ -eq '.') { 'processResources' } else { ':' + $_ + ':processResources' }
})

function Add-Probe([string]$Directory, [string]$Name, [string]$Value) {
    $probe = Join-Path $Directory "$probeDirectory/$Name.txt"
    if (Test-Path -LiteralPath $probe) { throw "Refusing to overwrite existing fixture: $probe" }
    [IO.Directory]::CreateDirectory((Split-Path $probe -Parent)) | Out-Null
    [IO.File]::WriteAllText($probe, $Value)
    $createdFiles.Add($probe)
    return $probe
}

function Invoke-Resources {
    $wrapper = Join-Path $projectPath 'gradlew.bat'
    if (!(Test-Path -LiteralPath $wrapper)) { throw "No Windows Gradle wrapper: $wrapper" }
    $command = '"' + $wrapper + '" -p "' + $projectPath + '" ' + ($tasks -join ' ') +
        ' --console=plain --no-daemon --no-configuration-cache'
    & cmd.exe /d /s /c "`"$command`""
    if ($LASTEXITCODE -ne 0) { throw "processResources failed: $projectPath" }
}

function Assert-Probe([string]$ModulePath, [string]$Name, [string]$Expected) {
    $output = Join-Path $ModulePath "build/resources/main/$probeDirectory/$Name.txt"
    if (!(Test-Path -LiteralPath $output) -or [IO.File]::ReadAllText($output) -ne $Expected) {
        throw "Wrong resource winner: $output (expected $Expected)"
    }
}

try {
    foreach ($name in @('common', 'main', 'generated')) { Add-Probe $commonPath $name 'common' | Out-Null }
    $overrides = @()
    foreach ($modulePath in $modulePaths) {
        $overrides += Add-Probe (Join-Path $modulePath 'src/main/resources') 'main' 'loader-main'
        $overrides += Add-Probe (Join-Path $modulePath 'src/generated/resources') 'generated' 'loader-generated'
    }
    Invoke-Resources
    foreach ($modulePath in $modulePaths) {
        Assert-Probe $modulePath 'common' 'common'
        Assert-Probe $modulePath 'main' 'loader-main'
        Assert-Probe $modulePath 'generated' 'loader-generated'
    }
    foreach ($probe in $overrides) { Remove-Item -LiteralPath $probe }
    Invoke-Resources
    foreach ($modulePath in $modulePaths) {
        Assert-Probe $modulePath 'main' 'common'
        Assert-Probe $modulePath 'generated' 'common'
    }
    Write-Output "PASS: loader main/generated precedence and incremental fallback in $projectPath ($($Modules -join ', '))"
}
finally {
    # Remove only files created by this invocation, never user files or resource directories.
    foreach ($probe in $createdFiles) {
        if (Test-Path -LiteralPath $probe) { Remove-Item -LiteralPath $probe }
    }
    Invoke-Resources
}
