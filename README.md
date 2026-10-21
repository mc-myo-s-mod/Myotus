# Myotus

Myotus is an Applied Energistics 2 addon and integration library. It exposes terminal configuration tabs, item-backed terminal upgrade hooks, a shared creative tab, annotation-driven commands, optional-integration metadata, and raw-XP planning/payment helpers.

For players, install the normal Myotus mod JAR together with AE2 and the matching Minecraft loader. The `api` JAR is for compiling addons, not for a `mods` folder.

## Versions in this checkout

These are build inputs from the linked `gradle.properties` files, not a claim that every version is available from a remote Maven repository or a mod download site.

| Module | Minecraft | Loader build | Java | AE2 build | Myotus version |
| --- | --- | --- | --- | --- | --- |
| [forge-1-20-1](forge-1-20-1/gradle.properties) | 1.20.1 | Forge 47.4.17 | 17 | 15.4.10 | 15.1.1 |
| [neoforge-1-21-1](neoforge-1-21-1/gradle.properties) | 1.21.1 | NeoForge 21.1.219 | 21 | 19.2.17 | 19.2.1-SNAPSHOT |
| [neoforge-26-1-2](neoforge-26-1-2/gradle.properties) | 26.1.2 | NeoForge 26.1.2.97 | 25 | 26.1.10-beta | 26.0.1 |

The root Gradle 8.14 build includes `common`, Forge 1.20.1, NeoForge 1.21.1, and a NeoForge 26.1.2 proxy project. Root `build` builds all three mod versions; 26.1.2 tasks are forwarded to the module's Gradle 9.2.1 wrapper, which selects Java 25. Run the root build itself on Java 17 or 21, not Java 25.

The Forge 1.20.1 and NeoForge 1.21.1 wrappers also work from their module directories: they use the parent settings and select that module's tasks. The 26.1.2 directory remains a native standalone Gradle build; keep it as a separate IntelliJ Gradle link for the real 26.1.2 source model. The root proxy is only a task entry point.

Shared code without Minecraft dependencies lives in `common/src/main/java`. Shared code that uses Minecraft or AE2 types lives in `common/src/minecraft/java` and is compiled by each loader module against its own dependencies. Shared NeoForge resources live in `common/src/neoforge/resources`; Forge retains its version-specific resource paths. Loader resources override common resources by whole file.

## Developer documentation

- [Quickstart](openwiki/quickstart.md): dependencies, runtime metadata, and first API calls.
- [API reference](openwiki/api.md): actual methods, examples, and limitations.
- [Architecture and lifecycle](openwiki/architecture.md): bootstrap order, sides, resource precedence, and version differences.
- [Build and verification](openwiki/workflows.md): compile, tests, datagen, runtime checks, and local publishing.

## Public entry points

```java
import me.myogoo.myotus.api.MyotusAPI;
import me.myogoo.myotus.api.experience.ExperienceMath;

// Balances and cost are raw vanilla XP points, not levels or mB.
var plan = MyotusAPI.experience().plan(
        75, new ExperienceMath.ExperienceAmounts(30, 40, 50));
boolean payable = plan.canPay(); // true: 30 player + 40 fluid + 5 Applied Experienced
```

| Entry point / package | Purpose |
| --- | --- |
| `MyotusAPI.get()`, `tryGet()`, `isInitialized()` | Access/check the runtime `IMyotusAPI` instance |
| `MyotusAPI.integrations()` | Query registered and active `@MyoMod` integrations |
| `MyotusAPI.configTabs()` | Register client-only `MyoConfigTab` definitions |
| `MyotusAPI.creativeTabs()` | Register item/stack suppliers for the shared creative tab |
| `MyotusAPI.terminalCards()` / `ITerminalCard` | 1.21.1 card queries and server-side menu callbacks |
| `MyotusAPI.terminalUpgrades()` / `ITerminalUpgradeCard` | Retained legacy API; deprecated on 1.21.1, still active on 1.20.1 and 26.1.2 |
| `MyotusAPI.terminalModules()` / `MyotusAPI.Client.registerTerminalModule` | 1.21.1-only permanent module slots, client widgets, and recipe-transfer targets |
| `MyotusAPI.experience()` / `api.experience` | XP math, pure plans, powered ME extraction, and server-side payment |
| `me.myogoo.ssec.api.command` / `CommandRegistrar` | SSEC command declarations and argument adapters |
| `api.recipe` / `api.datagen` | Table recipe adapters and loader-specific resource conditions |
| `MyotusAPI.Client.Widgets` | Client-only key-binding button factories |
| `MyotusAPI.network()` / `api.wt.AddTerminalEvent` | Forge 1.20.1-only networking and AE2WTLib registration hooks |

Register runtime contributions after Myotus initialization. `tryGet()` reports current state; it does not defer a callback until initialization. Keep config tabs, widgets, and optional-mod class references in appropriately isolated code. See the [lifecycle rules](openwiki/architecture.md#bootstrap-and-registration-order).

On all three version lines, commands use the standalone SSEC library. New declarations use
`me.myogoo.ssec.api.command.SSCommand`, `SSCExecute`, `SSCArgument`, `SSCAlias`,
and `SSCPermission`, with `me.myogoo.ssec.api.command.permission.PermissionLevel`.
Register argument adapters through `me.myogoo.ssec.command.CommandRegistrar`.

Source builds need a sibling `SSECLib` checkout containing the three command-library
modules. Use `-PssecRoot=/path/to/SSECLib` when it is elsewhere. The shipped Myotus
JAR embeds the matching library, so players do not install SSEC separately.

On 1.21.1, existing `ITerminalUpgradeCard` implementations and `terminalUpgrades()` calls remain supported with their original signatures and behavior. New integrations should use `ITerminalCard` and `terminalCards()`. The legacy declarations use `@Deprecated(forRemoval = true)` to signal removal in a future version. No removal version is set, and their implementations remain available in this version. The replacement card API and module slots are not ported to 1.20.1 or 26.1.2.

## Compile against a local build

The root modules configure `me.myogoo:myotus:<version>` and an `api` classifier. After [publishing a matching local build](openwiki/workflows.md#local-artifacts), a Forge 1.20.1 addon can use:

```gradle
repositories {
    mavenLocal()
    // Keep the repositories required by your own loader and AE2 setup.
}

dependencies {
    compileOnly "me.myogoo:myotus:15.1.1:api"
    runtimeOnly fg.deobf("me.myogoo:myotus:15.1.1")
}
```

For this checkout's NeoForge 1.21.1 module APIs, use the locally published `19.2.1-SNAPSHOT` and plain `runtimeOnly` without `fg.deobf`. For NeoForge 26.1.2, use `26.0.1` after publishing through the root proxy or the native 26.1.2 build; see the [26.1.2 setup](openwiki/quickstart.md#neoforge-2612). Declare Myotus in the addon's loader metadata as well as in Gradle.

The API classifier includes `api/**` and supporting DTO/icon/button types exposed by those signatures. It omits runtime implementation and loader metadata. Prefer `me.myogoo.myotus.api.*`; an implementation class being `public` does not make it a cross-version compatibility promise.

## Build

On Windows, from this repository root with Java 17 or 21:

```powershell
cmd.exe /c "gradlew.bat build --console=plain --no-configuration-cache"
```

To run a specific 26.1.2 forwarded task from the root:

```powershell
cmd.exe /c "gradlew.bat :neoforge-26-1-2:runGameTestServer --console=plain --no-configuration-cache"
```

Compilation and tests do not establish in-game behavior or TPS/FPS. Use the matching [client/server/GameTest checks](openwiki/workflows.md) for the code being changed.

GameTests run from the development-only `gameTest` source set and `myotus_gametest` mod. `runGameTestServer` loads them automatically; normal mod, API, and source JARs do not include their classes or test structures.

Development example recipes and their advancements (`dev_*.json`) also stay out of packaged JARs. Datagen and local development runs keep the examples and their `myotus:dev` condition.

The 1.21.1 widget-dispatch regression runs in a real client and exits after its checks:

```powershell
cmd.exe /c "gradlew.bat :neoforge-1-21-1:runClient -Pwidget_smoke=true --console=plain"
```

It checks transformed AE2 widget dispatch, overlapping modules, consumed clicks, visibility, and per-screen selection. The smoke sources and temporary card registrations are excluded when building without `widget_smoke`. Native child controls retain AE2's input handling; module selection follows visible, in-bounds composite-widget dispatch.

## License

[GNU Lesser General Public License v3.0](LICENSE).
