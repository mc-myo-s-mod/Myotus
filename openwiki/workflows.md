# Build and Verification

[README](../README.md) · [Quickstart](quickstart.md) · [API](api.md) · [Architecture](architecture.md)

These are Windows wrapper invocations. Use Java 17 for Forge 1.20.1, Java 21 for NeoForge 1.21.1, and Java 25 for NeoForge 26.1.2. Keep `GRADLE_USER_HOME` on your existing cache; do not start offline until mapped Minecraft/dependency artifacts are present.

## Root modules

Run from the Myotus root. ForgeGradle checks use `--no-configuration-cache`:

```powershell
cmd.exe /c "gradlew.bat :common:test --console=plain --no-configuration-cache"
cmd.exe /c "gradlew.bat :forge-1-20-1:compileJava :forge-1-20-1:test :forge-1-20-1:build --console=plain --no-configuration-cache"
cmd.exe /c "gradlew.bat :neoforge-1-21-1:compileJava :neoforge-1-21-1:test :neoforge-1-21-1:build --console=plain --no-configuration-cache"
```

Runtime and data tasks, run as needed for each affected loader:

```powershell
cmd.exe /c "gradlew.bat :forge-1-20-1:runClient --console=plain --no-configuration-cache"
cmd.exe /c "gradlew.bat :forge-1-20-1:runServer --console=plain --no-configuration-cache"
cmd.exe /c "gradlew.bat :forge-1-20-1:runGameTestServer --console=plain --no-configuration-cache"
cmd.exe /c "gradlew.bat :forge-1-20-1:runData --console=plain --no-configuration-cache"
cmd.exe /c "gradlew.bat :neoforge-1-21-1:runClient --console=plain --no-configuration-cache"
cmd.exe /c "gradlew.bat :neoforge-1-21-1:runServer --console=plain --no-configuration-cache"
cmd.exe /c "gradlew.bat :neoforge-1-21-1:runGameTestServer --console=plain --no-configuration-cache"
cmd.exe /c "gradlew.bat :neoforge-1-21-1:runData --console=plain --no-configuration-cache"
```

## Standalone 26.1.2

Run from `neoforge-26-1-2`, using its wrapper and Java 25. It is not a `:neoforge-26-1-2` root subproject.

```powershell
cmd.exe /c "gradlew.bat compileJava test build --console=plain"
cmd.exe /c "gradlew.bat runClient --console=plain"
cmd.exe /c "gradlew.bat runServer --console=plain"
cmd.exe /c "gradlew.bat runGameTestServer --console=plain"
cmd.exe /c "gradlew.bat runData --console=plain"
```

Its `test` source set includes `common/src/test/java`. The root loader modules run their own tests; `:common:test` remains a separate root-build check.

## CI and release publishing

[`build.yml`](../.github/workflows/build.yml) runs the shared tests first, then runs `runData`, `build`, and artifact upload for all three Minecraft versions. The 26.1.2 matrix entry uses its own wrapper with Java 25; the older versions keep using the root wrapper and Java 17/21 toolchains.

[`publish-release.yml`](../.github/workflows/publish-release.yml) accepts matching `<minecraft>-<mod-version>` and optional `-hotfix<N>` release tags. A published GitHub Release builds the selected version and publishes the same JAR to the GitHub Release, Maven Central, Modrinth, and CurseForge. The workflow rejects `-SNAPSHOT` project versions; change the selected module to a release version before creating its release tag. Manual dispatch remains a Maven Central-only backfill path.

## Local artifacts

Root modules configure coordinates and artifacts in [build.gradle](../build.gradle). To populate your local Maven cache, from the root:

```powershell
cmd.exe /c "gradlew.bat :forge-1-20-1:publishToMavenLocal --console=plain --no-configuration-cache"
cmd.exe /c "gradlew.bat :neoforge-1-21-1:publishToMavenLocal --console=plain --no-configuration-cache"
```

The build enables publication signing; missing local signing configuration can block publishing. Building still produces JARs in each module's `build/libs`. Verify actual artifacts before giving coordinates to an addon. A configured Maven Central publication is not proof of a published release.

For 26.1.2, run from `neoforge-26-1-2` with Java 25:

```powershell
cmd.exe /c "gradlew.bat publishToMavenLocal --console=plain"
```

Its generated Maven POM identifies `me.myogoo:myotus:26.1.0`; the standalone build adds the `api` classifier and configures the same signing and Maven Central publication contract as the root modules. See the [26.1.2 setup](quickstart.md#neoforge-2612) for Maven and direct-JAR examples.

The normal Forge artifact is the unclassified jar-in-jar/reobfuscated mod, not `-dev.jar`. The `-api.jar` is compile-only on every loader. Local publication is not remote release publication.

## What each check establishes

The reusable [resource override check](../scripts/Test-ResourceOverrides.ps1) creates temporary collision fixtures,
runs the real resource tasks, checks loader `main`/`generated` precedence and incremental fallback to common,
then removes its own fixtures. Run it with the build's JDK configured:

```powershell
.\scripts\Test-ResourceOverrides.ps1 -ProjectRoot . -Modules forge-1-20-1,neoforge-1-21-1
# With Java 25, from the same repository root:
.\scripts\Test-ResourceOverrides.ps1 -ProjectRoot .\neoforge-26-1-2 -Modules . -CommonRoot ../common/src/main/resources
```

- `compileJava`: Java signatures and dependency compatibility at compile time.
- `test`: configured JVM tests, including [shared XP math](../common/src/test/java/me/myogoo/myotus/api/experience/ExperienceMathTest.java), [integration state](../common/src/test/java/me/myogoo/myotus/util/mod/ModIntegrationManagerTest.java), and loader `CommandsApiTest`/`ExperienceApiExtractionTest` classes.
- `build`: compilation, configured tests, resource processing, and packaging; not an in-game test.
- `runGameTestServer`: registered in-game checks, such as [Forge XP/terminal tests](../forge-1-20-1/src/main/java/me/myogoo/myotus/gametest/MyoExperienceGameTests.java), [1.21.1 tests](../neoforge-1-21-1/src/main/java/me/myogoo/myotus/gametest/MyoExperienceGameTests.java), and [26.1.2 tests](../neoforge-26-1-2/src/main/java/me/myogoo/myotus/gametest/MyoExperienceGameTests.java).
- `runClient` / `runServer`: startup and side-specific loading. Reaching the title screen or server startup does not verify card behavior, recipe transfer, rendering, or performance under a real workload.
- `runData`: generated resources. Review intentional `src/generated/resources` changes and verify packaged resources after common/loader overrides.

For terminal changes, also open a real terminal, exercise the changed UI/card path, close/reopen it, and test relevant death/relogin behavior. For XP changes, test insufficient power/XP, storage changes between simulation and execution, and the intended fluid conversion. Record how far each run got; startup success is not feature success.
