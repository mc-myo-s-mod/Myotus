# Architecture and Lifecycle

[README](../README.md) · [Quickstart](quickstart.md) · [API](api.md) · [Workflows](workflows.md)

## Build boundaries

| Source/build | Responsibility |
| --- | --- |
| [common](../common) | Shared annotations, integration discovery, DTOs, reflection helpers, pure `ExperienceMath`, resources, and tests |
| [forge-1-20-1](../forge-1-20-1) | Java 17 / Forge APIs, AE2 hooks, Forge networking, and AE2WTLib registration bridge |
| [neoforge-1-21-1](../neoforge-1-21-1) | Java 21 / NeoForge APIs, AE2 hooks, codecs, and payload implementation |
| [neoforge-26-1-2](../neoforge-26-1-2) | Standalone Java 25 build using shared source plus 26.1.2 APIs/resources |

The [root settings](../settings.gradle) include only the first three rows. Root modules synchronize common Java into generated build directories. Standalone 26.1.2 adds common Java and common tests directly to its source sets. Do not edit synchronized Java in `build/generated`.

Shared resources live in `common/src/main/resources`. A resource in a loader's `src/main/resources` or `src/generated/resources` overrides a common file at the **same relative path**. Loader-generated resources remain datagen-owned output; do not rely on copy order to resolve accidental duplicates between two loader roots. Shared models, language files, textures, and guides belong in common when compatible; 26.1.2 item definitions under `assets/myotus/items` remain version-specific.

The governing configuration is [root build.gradle](../build.gradle) and the [26.1.2 build script](../neoforge-26-1-2/build.gradle), not the order of directories in an IDE.

## Bootstrap and registration order

1. Each loader's `Myotus` constructor installs `MyotusAPIImpl.INSTANCE` with `_setInstance`. This is an internal, one-time hook; addons must not call it.
2. Myotus supplies annotation scan data and the loader mod list to integration discovery. Forge's `integrations()` also initializes this through its guarded initializer; NeoForge initializes it from the mod constructor.
3. Addons register creative-tab contributions after Myotus initialization, client config tabs from client setup, and command adapters before the loader's command-registration event.
4. Myotus scans `@MyoCommand` classes on `RegisterCommandsEvent`. Creative-tab suppliers run when the tab is populated. Terminal card callbacks run from the server menu update/close flow.

`get()`, `configTabs()`, `creativeTabs()`, and Forge `network()` need the installed API instance. `isInitialized()` only checks that instance; it does not mean every addon has completed registration. `tryGet()` is a snapshot, not a lifecycle event. Static singleton access is not proof that loader discovery has finished.

Entrypoints: [Forge](../forge-1-20-1/src/main/java/me/myogoo/myotus/Myotus.java), [NeoForge 1.21.1](../neoforge-1-21-1/src/main/java/me/myogoo/myotus/Myotus.java), [NeoForge 26.1.2](../neoforge-26-1-2/src/main/java/me/myogoo/myotus/Myotus.java).

## Side and ownership rules

- `MyoConfigTab`, `MyoConfigTabScreen`, `MyotusAPI.Client`, and widget/icon types are client-only. Constructing them from common initialization can link client classes on a dedicated server.
- `ITerminalUpgradeCard` callbacks are server-side. Treat the menu and live card stack as callback-scoped; do not retain them or mutate them from worker threads.
- XP `consume` requires a server-side `ServerPlayer`. A plan is not authorization for a later payment; `consume` rechecks state, and callers must validate their menu/action requests.
- Forge packet handlers run on the logical main thread. Addons still check sender, permissions, menu ownership, and payload bounds.
- `@MyoMod` controls Myotus discovery, not JVM class loading in arbitrary addon code. Isolate optional-mod imports from always-loaded initializers.
- `ExperienceMath` has no Minecraft or optional-mod dependency and can be used in ordinary JVM tests. The outer `MyotusAPI` also references Minecraft/AE2 types.

## Version differences that affect addons

| Surface | Forge 1.20.1 | NeoForge 1.21.1 | NeoForge 26.1.2 |
| --- | --- | --- | --- |
| Public resource ID type | `ResourceLocation` | `ResourceLocation` | `Identifier` |
| Table input bound | `Container` | `RecipeInput` | `RecipeInput` |
| `slotIngredients()` | `NonNullList<Ingredient>` | `NonNullList<Ingredient>` | `NonNullList<Optional<Ingredient>>` |
| Default `findRecipeId()` | ID from recipe | Empty; retain holder ID | Empty; retain holder ID |
| Conditions | Forge `IConditionSerializer` | NeoForge `MapCodec` | NeoForge `MapCodec` |
| Public `network()` / `api.network` | Present | Not exposed | Not exposed |
| Public `api.wt.AddTerminalEvent` | Present | Not exposed | Not exposed |

The artifacts are separate compile targets, not interchangeable binaries. Use matching API JARs and dependencies when porting. In 26.1.2, replace `ResourceLocation.fromNamespaceAndPath(...)` examples with `Identifier.fromNamespaceAndPath(...)`; other Minecraft/AE2 signatures also need the matching source.

## API versus implementation

`me.myogoo.myotus.api.*` is the addon-facing surface. The API JAR also includes `MyoModDto`, `MyoModInfo`, `MyoIcon`, `CustomTabButton`, and `MyoCycleButton` because public signatures expose them. Prefer `MyoConfigTab.createButton(...)` when a plain Minecraft `Button` suffices.

Do not construct `MyotusAPIImpl`, register through internal managers, or manipulate `TerminalUpgradeStorageKey`/persistent NBT directly. Those are implementation details, not an addon storage schema. Public APIs do not promise arbitrary Minecraft-object thread safety, automatic JEI/EMI/REI plugin registration, or transactional multi-storage XP payments; the [reference](api.md) details those limits.
