# Addon Quickstart

[README](../README.md) · [API](api.md) · [Architecture](architecture.md) · [Workflows](workflows.md)

Choose the Minecraft/loader line in the [version matrix](../README.md#versions-in-this-checkout). These examples describe this checkout; remote artifact availability has not been verified.

## Forge 1.20.1 and NeoForge 1.21.1

First [publish the matching root module locally](workflows.md#local-artifacts). Add `mavenLocal()` alongside the repositories already required by your addon.

Forge 1.20.1:

```gradle
dependencies {
    compileOnly "me.myogoo:myotus:15.1.0:api"
    runtimeOnly fg.deobf("me.myogoo:myotus:15.1.0")
}
```

NeoForge 1.21.1:

```gradle
dependencies {
    compileOnly "me.myogoo:myotus:19.1.1:api"
    runtimeOnly "me.myogoo:myotus:19.1.1"
}
```

The `api` classifier is a mapped compile artifact, not a runtime mod. Keep the normal unclassified JAR in the development runtime. Configure the matching AE2 dependency in your addon when using AE2 types; these snippets are not complete loader build scripts.

## NeoForge 26.1.2

The standalone build has its own [settings](../neoforge-26-1-2/settings.gradle) and [build script](../neoforge-26-1-2/build.gradle). Its generated Maven publication uses `me.myogoo:myotus:26.1.0` from the configured group, project name, and version. Run `publishToMavenLocal` with its wrapper and Java 25 from that directory, then use:

```gradle
dependencies {
    compileOnly "me.myogoo:myotus:26.1.0:api"
    runtimeOnly "me.myogoo:myotus:26.1.0"
}
```

As a local-build alternative, copy the normal JAR and `-api.jar` from `build/libs` into your addon's `libs` directory:

```gradle
dependencies {
    compileOnly files("libs/Myotus-26.1.2-26.1.0-api.jar")
    runtimeOnly files("libs/Myotus-26.1.2-26.1.0.jar")
}
```

File dependencies have no transitive metadata: configure AE2 and any integration dependencies separately. Check generated filenames if you change `mod_version`. A generated/local Maven publication does not establish remote availability.

## Runtime metadata

Replace `examplemod` with your addon ID. These ranges are examples bounded to the corresponding Myotus major line; use the minimum version that actually supplies the API your addon uses.

Forge `META-INF/mods.toml`:

```toml
[[dependencies.examplemod]]
modId = "myotus"
mandatory = true
versionRange = "[15.1.0,16)"
ordering = "AFTER"
side = "BOTH"
```

NeoForge `META-INF/neoforge.mods.toml`:

```toml
[[dependencies.examplemod]]
modId = "myotus"
type = "required"
versionRange = "[19.1.1,20)"
ordering = "AFTER"
side = "BOTH"
```

For current 26.1.2 source, use `[26.1.0,27)` instead. A Myotus integration annotation does not replace this loader dependency.

## First runtime contribution

Call this from your addon's normal setup after Myotus initialization, not a static initializer:

```java
import me.myogoo.myotus.api.MyotusAPI;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

MyotusAPI.creativeTabs()
        .registerCreativeTabItem(() -> Items.REDSTONE)
        .registerCreativeTabStack(() -> new ItemStack(Items.DIAMOND));
```

Use your own registered item suppliers in a real addon. Suppliers are evaluated when the creative tab is populated; they must not return `null`. Register each contribution once.

For a required dependency, calling `get()` after the proper lifecycle boundary exposes initialization mistakes. For optional access, `tryGet()` returns `Optional<IMyotusAPI>`; an empty result does not schedule a retry and does not make referencing an absent Myotus class safe.

## Next integration

- [Optional integrations](api.md#optional-integrations): declare your marker before querying active state.
- [Terminal config tabs](api.md#terminal-config-tabs): client-only builders and style resources.
- [Terminal upgrade cards](api.md#terminal-upgrade-cards): server open/tick/close callbacks.
- [Experience API](api.md#experience-api): points, storage units, simulation, and mutation.
- [Commands](api.md#commands): declarations and adapters instead of the internal registrar.
