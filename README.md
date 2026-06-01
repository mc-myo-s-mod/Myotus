# Myotus

Myotus is a small Applied Energistics 2 extension library for Minecraft mod developers.

It provides public API hooks for optional integrations, AE2 terminal configuration tabs,
shared creative-tab entries, terminal upgrade cards, and a few pure helper APIs used by
addons.

Myotus is intentionally not a large standalone content mod. Its main job is to be a
stable bridge that other AE2-related mods can depend on.

## Supported Minecraft Lines

| Module | Loader | Minecraft | Java | AE2 | Artifact version |
| --- | --- | --- | --- | --- | --- |
| `forge-1-20-1` | Forge `47.x` | `1.20.1` | `17` | `15.4.x` | `15.1.0` |
| `neoforge-1-21-1` | NeoForge `21.1.x` | `1.21.1` | `21` | `19.2.x` | `19.1.0` |

The project has a shared `common` source set plus loader-specific modules. Forge and
NeoForge APIs are kept separate when Minecraft/loader differences make that healthier
than forcing artificial parity.

## What Myotus Provides

- `MyotusAPI` as the main public API entry point.
- Optional integration discovery and runtime checks.
- AE2 terminal config-tab registration.
- Shared Myotus creative-tab registration.
- Item-backed AE2 terminal upgrade cards.
- Terminal upgrade query helpers.
- Client widget helpers.
- Pure experience math helpers for Applied Experienced / `fluid:xp` style integrations.
- A normal loader-discoverable Myotus mod artifact for addon development and runtime use.

## For Players and Modpacks

If another mod depends on Myotus, install Myotus alongside that mod, Applied Energistics 2,
and the correct loader for your Minecraft version.

Myotus can be included in modpacks. On its own it may look small, because most of its value
is exposed to other mods through API hooks.

## For Addon Developers

### Dependency Coordinates

Use the `myotus` artifact when compiling an addon against Myotus:

```gradle
repositories {
    mavenLocal() // useful while testing local builds
    mavenCentral()
    maven {
        name = "ModMaven"
        url = "https://modmaven.dev"
        content {
            includeGroup "appeng"
            includeGroup "de.mari_023"
        }
    }
}

dependencies {
    // Forge 1.20.1 line
    compileOnly "me.myogoo:myotus:15.1.0"

    // NeoForge 1.21.1 line
    // compileOnly "me.myogoo:myotus:19.1.0"
}
```

The published `myotus` jar is the normal loader-discoverable Myotus mod artifact. It is
not an API-only jar: it includes loader metadata, Myotus runtime classes, assets, and data
resources. Addons should still prefer the public `me.myogoo.myotus.api.*` packages and
avoid depending on implementation packages unless a class is explicitly promoted into the
public API.

When using Myotus features at runtime, also declare the matching Myotus mod dependency in
your loader metadata so the mod is present before your addon initializes.

### Local Publishing

From the repository root:

```bash
./gradlew :forge-1-20-1:publishToMavenLocal
./gradlew :neoforge-1-21-1:publishToMavenLocal
```

On Windows, use `gradlew.bat`. From WSL against a Windows-mounted workspace, this project
is commonly verified with:

```bash
/mnt/c/Windows/System32/cmd.exe /c "gradlew.bat build :forge-1-20-1:publishToMavenLocal :neoforge-1-21-1:publishToMavenLocal --console=plain"
```

## API Quick Start

### Access the API Safely

```java
if (MyotusAPI.isInitialized()) {
    MyotusAPI.configTabs();
}
```

Avoid calling `MyotusAPI.get()` from static initializers. Use it during or after normal mod
setup when Myotus has installed its API instance.

### Optional Integration Checks

```java
if (MyotusAPI.integrations().isLoaded("examplemod")) {
    // Run code that should only be active when examplemod is present.
}
```

You can also query by Myotus integration annotation class or ASM `Type` where appropriate.

### Terminal Config Tabs

Register custom tabs for AE2 terminal configuration screens:

```java
MyotusAPI.configTabs().registerTerminalConfigTab(new MyoConfigTab(
        ResourceLocation.fromNamespaceAndPath("examplemod", "terminal_settings"),
        Component.literal("Example"),
        Icon.COG,
        "example_terminal.json",
        new ExampleConfigScreen()
));
```

When the tab is only valid for certain terminal contexts, attach a visibility rule:

```java
MyotusAPI.configTabs().registerTerminalConfigTab(new MyoConfigTab(
        ResourceLocation.fromNamespaceAndPath("examplemod", "portable_terminal_settings"),
        Component.literal("Portable"),
        Icon.COG,
        "portable_terminal.json",
        new PortableConfigScreen()
).visibleWhen(context -> context.isItemHost()));
```

### Shared Creative Tab Entries

Add items or preconfigured stacks to the shared Myotus creative tab:

```java
MyotusAPI.creativeTabs().registerCreativeTabItem(MY_ITEM);

MyotusAPI.creativeTabs().registerCreativeTabStack(
        () -> new ItemStack(MY_ITEM.get(), 1)
);
```

### Terminal Upgrade Cards

Upgrade cards are normal items that implement `ITerminalUpgradeCard`:

```java
public class MyUpgradeCardItem extends Item implements ITerminalUpgradeCard {
    public MyUpgradeCardItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void onTerminalOpen(MEStorageMenu menu, ItemStack stack) {
        // Apply behavior when the terminal opens.
    }

    @Override
    public void onTerminalClose(MEStorageMenu menu, ItemStack stack) {
        // Clean up behavior when the terminal closes.
    }
}
```

Useful helper queries:

```java
boolean installed = MyotusAPI.terminalUpgrades().hasUpgrade(menu, MY_UPGRADE_ID);
List<ItemStack> upgrades = MyotusAPI.terminalUpgrades().getInstalledUpgrades(menu);
```

A terminal can only install one copy of the same upgrade item type. Upgrade card items
should generally have max stack size `1`.

### Experience Helpers

`MyotusAPI.experience()` exposes pure Java helpers for raw vanilla XP point math.
These helpers do not import Applied Experienced classes, so they are safe to use even when
Applied Experienced is not installed.

```java
long appliedExperience = 0; // use 0 when Applied Experienced data is absent
long fluidXp = 7;

long total = MyotusAPI.experience().totalExperience(appliedExperience, fluidXp);
int level = MyotusAPI.experience().levelForTotalExperience(total);
long progress = MyotusAPI.experience().experienceIntoLevel(total);
long next = MyotusAPI.experience().experienceToNextLevel(level);
```

For anvil-like flows that can spend XP from multiple pools, use `consumeExperience(...)`.
The default priority is:

```text
player XP -> fluid:xp -> appex:experience_amount
```

```java
long requiredXp = 75;
long playerXp = 30;
long fluidXp = 40;
long appliedExperienceAmount = 50;

var plan = MyotusAPI.experience().consumeExperience(
        requiredXp,
        playerXp,
        fluidXp,
        appliedExperienceAmount
);

if (plan.canPay()) {
    long playerUsed = plan.playerExperienceUsed();
    long fluidUsed = plan.fluidXpUsed();
    long appliedUsed = plan.appliedExperiencedAmountUsed();

    // Spend each source using the amounts reported by the plan.
}
```

Custom source ordering is also supported:

```java
import me.myogoo.myotus.api.experience.ExperienceMath;

var plan = MyotusAPI.experience().consumeExperience(
        requiredXp,
        playerXp,
        fluidXp,
        appliedExperienceAmount,
        List.of(
                ExperienceMath.ExperienceSource.FLUID_XP,
                ExperienceMath.ExperienceSource.APPLIED_EXPERIENCED_AMOUNT,
                ExperienceMath.ExperienceSource.PLAYER
        )
);
```

Stable IDs are exposed for integrations that need to compare external keys:

```java
String appexMod = MyotusAPI.experience().appliedExperiencedModId();          // "appex"
String appexKey = MyotusAPI.experience().appliedExperiencedAeKeyId();        // "appex:experience"
String amountId = MyotusAPI.experience().appliedExperiencedAmountComponentId();
String fluidXpId = MyotusAPI.experience().fluidXpId();                       // "fluid:xp"
```

## Repository Layout

```text
common/             Shared source and tests copied into loader modules
forge-1-20-1/       Forge 1.20.1 implementation and API surface
neoforge-1-21-1/    NeoForge 1.21.1 implementation and API surface
readme/             Mod-page README drafts/assets
repo/               Optional project-local Maven repository
```

## Building and Testing

From the repository root:

```bash
./gradlew build
./gradlew :common:test
./gradlew :forge-1-20-1:compileJava
./gradlew :neoforge-1-21-1:compileJava
```

Loader-specific runtime tasks live in each module:

```bash
./gradlew :forge-1-20-1:runClient
./gradlew :forge-1-20-1:runGameTestServer

./gradlew :neoforge-1-21-1:runClient
./gradlew :neoforge-1-21-1:runGameTestServer
```

## API Boundary Notes

- Prefer `me.myogoo.myotus.api.*` classes from the `myotus` artifact.
- Do not rely on `me.myogoo.myotus.util`, `dto`, `client`, `init`, or other implementation
  packages unless they are explicitly promoted into the public API.
- Optional-mod integrations should avoid importing optional classes into always-loaded code
  paths unless guarded by a safe loader-specific isolation pattern.
- Some Forge-only or NeoForge-only API differences are deliberate.

## License

Myotus is licensed under the **GNU Lesser General Public License v3.0**.
