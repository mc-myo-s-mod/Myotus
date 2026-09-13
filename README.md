# Myotus

Myotus is an Applied Energistics 2 addon and integration library. It exposes terminal configuration tabs, item-backed terminal upgrade hooks, a shared creative tab, annotation-driven commands, optional-integration metadata, and raw-XP planning/payment helpers.

For players, install the normal Myotus mod JAR together with AE2 and the matching Minecraft loader. The `api` JAR is for compiling addons, not for a `mods` folder.

## Versions in this checkout

These are build inputs from the linked `gradle.properties` files, not a claim that every version is available from a remote Maven repository or a mod download site.

| Module | Minecraft | Loader build | Java | AE2 build | Myotus version |
| --- | --- | --- | --- | --- | --- |
| [forge-1-20-1](forge-1-20-1/gradle.properties) | 1.20.1 | Forge 47.4.17 | 17 | 15.4.10 | 15.1.0 |
| [neoforge-1-21-1](neoforge-1-21-1/gradle.properties) | 1.21.1 | NeoForge 21.1.219 | 21 | 19.2.17 | 19.1.1 |
| [neoforge-26-1-2](neoforge-26-1-2/gradle.properties) | 26.1.2 | NeoForge 26.1.2.97 | 25 | 26.1.10-beta | 26.1.0 |

The root Gradle build includes `common`, Forge 1.20.1, and NeoForge 1.21.1. The 26.1.2 directory is a **separate Gradle build** that also uses `common`; a root `build` does not build it.

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
| `MyotusAPI.terminalUpgrades()` / `ITerminalUpgradeCard` | Inspect cards and implement server-side menu callbacks |
| `MyotusAPI.experience()` / `api.experience` | XP math, pure plans, powered ME extraction, and server-side payment |
| `MyotusAPI.commands()` / `api.annotation.commands` | Command argument adapters and command declarations |
| `api.recipe` / `api.datagen` | Table recipe adapters and loader-specific resource conditions |
| `MyotusAPI.Client.Widgets` | Client-only key-binding button factories |
| `MyotusAPI.network()` / `api.wt.AddTerminalEvent` | Forge 1.20.1-only networking and AE2WTLib registration hooks |

Register runtime contributions after Myotus initialization. `tryGet()` reports current state; it does not defer a callback until initialization. Keep config tabs, widgets, and optional-mod class references in appropriately isolated code. See the [lifecycle rules](openwiki/architecture.md#bootstrap-and-registration-order).

## Compile against a local build

The root modules configure `me.myogoo:myotus:<version>` and an `api` classifier. After [publishing a matching local build](openwiki/workflows.md#local-artifacts), a Forge 1.20.1 addon can use:

```gradle
repositories {
    mavenLocal()
    // Keep the repositories required by your own loader and AE2 setup.
}

dependencies {
    compileOnly "me.myogoo:myotus:15.1.0:api"
    runtimeOnly fg.deobf("me.myogoo:myotus:15.1.0")
}
```

For NeoForge 1.21.1, use `19.1.1` and plain `runtimeOnly` without `fg.deobf`. For standalone 26.1.2, use `26.1.0` after publishing from that directory; see the [26.1.2 setup](openwiki/quickstart.md#neoforge-2612). Declare Myotus in the addon's loader metadata as well as in Gradle.

The API classifier includes `api/**` and supporting DTO/icon/button types exposed by those signatures. It omits runtime implementation and loader metadata. Prefer `me.myogoo.myotus.api.*`; an implementation class being `public` does not make it a cross-version compatibility promise.

## Build

On Windows, from this repository root:

```powershell
cmd.exe /c "gradlew.bat :common:test :forge-1-20-1:build :neoforge-1-21-1:build --console=plain --no-configuration-cache"
```

Build 26.1.2 separately, from `neoforge-26-1-2`, with Java 25:

```powershell
cmd.exe /c "gradlew.bat build --console=plain"
```

Compilation and tests do not establish in-game behavior or TPS/FPS. Use the matching [client/server/GameTest checks](openwiki/workflows.md) for the code being changed.

## License

[GNU Lesser General Public License v3.0](LICENSE).
