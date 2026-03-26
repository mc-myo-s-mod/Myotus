<p align="center">
  <img width="160" src="readme/myotus.png" alt="Myotus logo">
</p>

<h1 align="center">Myotus</h1>

<p align="center">
  A NeoForge library for extending Applied Energistics 2 terminals.
</p>

<p align="center">
  Myotus exposes a small public API for optional integrations, terminal config tabs,
  and item-based terminal upgrade cards.
</p>

## Summary

Myotus is infrastructure for mods that want to customize or extend AE2 terminal behavior on Minecraft `1.21.1` with NeoForge.
It is not designed as a large standalone content mod. The main purpose of the project is to provide stable hooks that other mods can build on.

The library currently focuses on four areas:

- registration of optional integrations behind marker annotations
- registration of custom terminal configuration tabs
- runtime checks for which integrations are active
- item-backed terminal upgrade cards with lifecycle callbacks

See [SUMMARY.md](SUMMARY.md) if you need a short project blurb.

## Target Environment

- Minecraft `1.21.1`
- NeoForge `21.1.x`
- Java `21`
- Applied Energistics 2 `19.2.x`

## Core Capabilities

- Public API entry point via `MyotusAPI`
- Annotation-driven optional integration registration
- Custom AE2 terminal config tabs through `MyoConfigTab`
- Per-terminal tab visibility rules through `MyoConfigTabVisibility`
- Terminal upgrade card hooks with `onTerminalOpen`, `onTerminalClose`, and `onTerminalTick`
- Runtime integration state queries through `IModIntegrationManager`
- Player-persistent terminal upgrade storage

Optional integrations bundled in this repository currently target:

- JEI
- EMI
- REI
- GuideME
- AE2WTLib
- AE2FCT
- AE2TB

## For Players

If another mod depends on Myotus, install Myotus alongside that mod and AE2.

Myotus is intentionally small on its own because it mainly exists as a shared extension layer.

## For Developers

Myotus is published like a normal Gradle dependency.

Artifact coordinates:

```gradle
implementation "me.myogoo:myotus:<version>"
```

For local development before a release is available, use `mavenLocal()`:

```gradle
repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = "https://modmaven.dev"
        content {
            includeGroup "appeng"
            includeGroup "de.mari_023"
        }
    }
}

dependencies {
    implementation "me.myogoo:myotus:<version>"
}
```

Then publish Myotus locally:

```bash
./gradlew publishToMavenLocal
```

## Quick Start

### Register an Optional Integration

Marker annotations let Myotus discover integration code only when the target mod is present:

```java
MyotusAPI.modRegistrar()
        .registerLoadableMod(MyMarker.class, "examplemod", "[1.0.0,)");
```

At runtime:

```java
if (MyotusAPI.modIntegrationManager().isLoaded("examplemod")) {
    // Safe to run optional integration logic.
}
```

### Register a Terminal Config Tab

Tabs can expose custom UI inside the AE2 terminal settings screen:

```java
MyotusAPI.configRegistrar()
        .registerTerminalConfigTab(new MyoConfigTab(
                Component.literal("Example"),
                Icon.COG,
                "example_terminal.json",
                new ExampleConfigScreen()));
```

You can also restrict a tab to specific terminal contexts:

```java
MyotusAPI.configRegistrar()
        .registerTerminalConfigTab(new MyoConfigTab(
                Component.literal("Portable"),
                Icon.COG,
                "portable_terminal.json",
                new PortableConfigScreen())
                .visibleWhen(context -> context.isItemHost()));
```

### Implement a Terminal Upgrade Card

Upgrade cards are regular items that implement `ITerminalUpgradeCard`:

```java
public class MyUpgradeCardItem extends Item implements ITerminalUpgradeCard {
    @Override
    public void onTerminalOpen(MEStorageMenu menu, ItemStack stack) {
        // Apply behavior when the terminal is opened.
    }
}
```

Upgrade cards should use a max stack size of `1`, because terminal storage is tracked per installed item stack.

## Building

From the repository root:

```bash
./gradlew build
./gradlew runClient
./gradlew runGameTestServer
./gradlew publishToMavenLocal
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

## Versioning

This repository is versioned per Minecraft line.
The current branch targets Minecraft `1.21.1`.

## License

Code in this repository is licensed under **GNU LGPL 3.0**.

See [CHANGELOG.md](CHANGELOG.md) for release notes.
