# API Reference

[README](../README.md) · [Quickstart](quickstart.md) · [Architecture](architecture.md) · [Workflows](workflows.md)

This page follows the source, not older facade names. `IntegrationsApi`, `TerminalUpgradesApi`, `CommandsApi`, and `ExperienceApi` are nested classes of `MyotusAPI`. There is no public `TerminalUpgradeFacade` or `ExperienceFacade` in this checkout.

Sources: [Forge MyotusAPI](../forge-1-20-1/src/main/java/me/myogoo/myotus/api/MyotusAPI.java), [NeoForge 1.21.1 MyotusAPI](../neoforge-1-21-1/src/main/java/me/myogoo/myotus/api/MyotusAPI.java), [NeoForge 26.1.2 MyotusAPI](../neoforge-26-1-2/src/main/java/me/myogoo/myotus/api/MyotusAPI.java).

## Runtime access

- `MyotusAPI.get()` returns `IMyotusAPI` or throws when no runtime instance has been installed.
- `tryGet()` returns `Optional<IMyotusAPI>`; `isInitialized()` checks the same state.
- `configTabs()` and `creativeTabs()` delegate to the runtime instance. Forge-only `network()` does too.
- `IMyotusAPI` also offers fluent `registerConfigTab`, `registerCreativeTabItem`, and `registerCreativeTabStack` aliases.
- `_setInstance(...)` is internal bootstrap, not an addon registration API.

Use the [lifecycle and side rules](architecture.md#bootstrap-and-registration-order). Do not silently discard required registration by putting it in an early `tryGet().ifPresent(...)` callback.

## Optional integrations

`MyotusAPI.integrations()` reports **Myotus-managed integrations**, not an unrestricted query of every mod installed in the loader.

| Method | Meaning |
| --- | --- |
| `isRegistered(String)` / `isRegistered(Class<? extends Annotation>)` | Myotus knows a matching registration; declared IDs/aliases need not be active. String queries also recognize active integrations' namespace/display names |
| `isLoaded(String/Class/Type/MyoModDto)` | A matching integration is active after mod presence, version, custom-condition, and mode checks |
| `find(String)` | An active `MyoModDto`, if available |
| `findAnnotation(String)` | A unique matching annotation, preferring active matches; empty on ambiguity |
| `findAnnotation(MyoModDto)` | The annotation for that active registration |
| `activeIntegrations()` | Unmodifiable map of active DTOs to annotation classes |

Use a stable mod ID or explicit alias for string lookups. If several registrations share a mod ID, use their annotation classes or distinct aliases to identify one registration.

Declare a runtime-retained marker annotation in your addon, for example:

```java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import me.myogoo.myotus.api.annotation.MyoMod;

@MyoMod(value = "examplemod", alias = "example_feature", versionRange = "[1.0,2.0)")
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface ExampleIntegration {}
```

After loader initialization:

```java
boolean known = MyotusAPI.integrations().isRegistered(ExampleIntegration.class);
boolean active = MyotusAPI.integrations().isLoaded(ExampleIntegration.class);
```

`@MyoMod` defaults to `versionRange = "*"`, `mode = DEFAULT`, and no custom condition. `customCondition` names a no-argument-constructible implementation of `MyoCustomCondition.test(MyoModInfo)`. A false result disables that registration; a condition construction/evaluation failure is logged and treated as false.

Registrations without custom conditions for the same mod share aliases and intersect their version ranges. Conflicting aliases/ranges are rejected. An installed mod outside an applicable range causes a loading error, not merely `isLoaded(...) == false`. Within an activation group, active `OVERRIDE` registrations suppress non-override registrations; custom-condition registrations normally form separate groups unless marked `EXTENDED`. This is Myotus discovery policy, not a replacement for loader dependency metadata or safe optional-class isolation.

Sources: [MyoMod](../common/src/main/java/me/myogoo/myotus/api/annotation/MyoMod.java), [custom condition](../common/src/main/java/me/myogoo/myotus/api/integration/MyoCustomCondition.java), [integration manager](../common/src/main/java/me/myogoo/myotus/util/mod/ModIntegrationManager.java).

### Item-list annotations

The shared API contains `@JEI`, `@EMI`, `@REI`, `@RecipeAdd`, `@RecipeCategory`, `@RecipeTransfer`, `@JEIGuiHandler`, and `@MyotusSubscriber`.

The [subscriber helper](../neoforge-1-21-1/src/main/java/me/myogoo/myotus/integration/itemList/ItemListModLoadHelper.java) accepts only static subscriber methods with exactly one parameter of the requested registration type. It filters inactive integration markers and logs invalid signatures/invocation failures.

However, this checkout has **no caller that connects that helper to a shipped JEI/EMI/REI plugin registration event**. The annotations alone do not register recipes, categories, or transfer handlers. Use the target mod's normal plugin API for an actual integration; do not infer an automatic registration bridge from the marker names or their Javadocs.

## Shared creative tab

`ICreativeTabRegistrar` exposes:

- `creativeTabItem(Supplier<? extends ItemLike>)` and `creativeTabStack(Supplier<ItemStack>)`.
- Fluent aliases `registerCreativeTabItem(...)` / `registerCreativeTabStack(...)`.
- Iterable aliases `registerCreativeTabItems(...)` / `registerCreativeTabStacks(...)`.

Register once after Myotus initialization. Suppliers are evaluated during tab population, must return non-null values, and should construct fresh stacks when carrying custom state. Item suppliers are populated before stack suppliers. This does not register items into Minecraft's item registry.

Example: [first runtime contribution](quickstart.md#first-runtime-contribution). Source: [ICreativeTabRegistrar](../neoforge-1-21-1/src/main/java/me/myogoo/myotus/api/registrar/ICreativeTabRegistrar.java).

## Terminal config tabs

`MyoConfigTab` is a client-only immutable definition with a stable ID, title, icon, style path, `MyoConfigTabScreen`, and visibility predicate. Icon overloads accept an AE2 `Icon`, `Blitter`, `MyoIcon`, or `ItemStack`. Stack icons are copied. IDs must be unique and style paths non-blank.

A complete registration method for a client-only class on 1.20.1/1.21.1:

```java
import me.myogoo.myotus.api.MyotusAPI;
import me.myogoo.myotus.api.config.MyoConfigTab;
import me.myogoo.myotus.api.config.MyoConfigTabScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public static void registerTerminalTab(String stylePath, MyoConfigTabScreen contents) {
    MyotusAPI.configTabs().registerTerminalConfigTab(new MyoConfigTab(
            ResourceLocation.fromNamespaceAndPath("examplemod", "terminal_settings"),
            Component.literal("Example"), new ItemStack(Items.REDSTONE),
            stylePath, contents
    ).visibleWhen(context -> context.isItemHost()));
}
```

Supply your own style and builder; this method does not create either. `MyoConfigTabScreen.buildTab(WidgetContainer, AEBaseScreen<?>)` adds the tab contents. The current screen loads `"/screens/config/" + stylePath` through AE2's screen style mechanism; it does not resolve the tab ID as a style resource. Existing shared styles are under [assets/ae2/screens/config](../common/src/main/resources/assets/ae2/screens/config).

`visibleWhen(...)` returns a new tab definition. Context exposes `menu()`, `host()`, `player()`, `isItemHost()`, `getHostItemStack()`, `isHostItem(Item/id)`, and `isHostItemFrom(namespace)`. Do not retain or mutate a host stack from a visibility predicate. Visibility controls presentation, not server authorization.

`IConfigRegistrar` also exposes `terminalConfigTab(...)` and iterable `registerTerminalConfigTabs(...)`. Duplicate IDs throw instead of replacing an existing tab. `createButton(...)` returns a Minecraft `Button`; `getTabButton(...)` exposes the supporting `CustomTabButton` type.

Sources: [MyoConfigTab](../neoforge-1-21-1/src/main/java/me/myogoo/myotus/api/config/MyoConfigTab.java), [context](../neoforge-1-21-1/src/main/java/me/myogoo/myotus/api/config/MyoConfigTabContext.java), [screen builder](../neoforge-1-21-1/src/main/java/me/myogoo/myotus/api/config/MyoConfigTabScreen.java).

### Client widgets

`MyotusAPI.Client.Widgets.keyBindingButton(label, changeListener)` and the overload with `initialKey` return `AbstractButton`; the listener receives `List<InputConstants.Key>`.

The API classifier also includes `MyoCycleButton` from `me.myogoo.myotus.client.gui.widgets.button`. Its constructors accept either `Consumer<MyoCycleButton.MouseButton>` (`LEFT`/`RIGHT`) or separate left/right `Runnable` actions, plus item/tooltip suppliers; icon-supplier overloads are available. Keyboard activation follows the left-click action. All of these types are client-only and their underlying Minecraft widget APIs differ by version.

## Terminal upgrade cards

Implement [ITerminalUpgradeCard](../neoforge-1-21-1/src/main/java/me/myogoo/myotus/api/ITerminalUpgradeCard.java) on a normally registered item. For 1.20.1/1.21.1:

```java
import appeng.menu.me.common.MEStorageMenu;
import me.myogoo.myotus.api.ITerminalUpgradeCard;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ExampleCard extends Item implements ITerminalUpgradeCard {
    public ExampleCard(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void onTerminalTick(MEStorageMenu menu, ItemStack stack) {
        // Your server-side behavior while this terminal is open.
    }
}
```

All three callbacks default to no-op:

| Callback | When it runs |
| --- | --- |
| `onTerminalOpen(menu, stack)` | First server menu update after concrete menu construction, or insertion into an active menu |
| `onTerminalTick(menu, stack)` | Once per server tick while installed in an open terminal |
| `onTerminalClose(menu, stack)` | Menu close or card removal; removal receives the last snapshot |

An installed callback stack is live and may carry the card's own data. Do not change its item/count or retain the reference. A card is not a background ticker after the menu closes. Only one copy of the same item type is accepted in a terminal; use a non-stackable item.

`MyotusAPI.terminalUpgrades()` exposes `installedUpgrades(menu)`, `installedUpgradeItems(menu)`, `availableUpgradeCards(menu)`, `availableUpgradeTooltip(menu)`, `hasUpgrade(menu, item/id)`, `countUpgrade(menu, item/id)`, and `canInsertUpgrade(installedStacks, slot, stack)`.

```java
boolean installed = MyotusAPI.terminalUpgrades().hasUpgrade(menu, cardItem);
var snapshots = MyotusAPI.terminalUpgrades().installedUpgrades(menu);
```

`installedUpgrades` returns copied stacks; modifying them does not modify the terminal. Available cards are registry-discovered eligible card items excluding installed item types, not the player's inventory. ID queries return false/zero for missing items. These helpers describe Myotus upgrade slots; they do not install AE2 machine upgrades or automatically connect AE2 View Cell filtering on versions without that channel.

### Terminal modules (1.21.1 development branch)

The 1.21.1 card hierarchy separates behavior upgrades from GUI/native-slot modules:

```text
ITerminalCard
├── ITerminalUpgradeCard  (behavior upgrades; existing implementations remain valid)
└── ITerminalModuleCard   (terminal modules; not an upgrade-card subtype)
```

Both kinds use the existing terminal card inventory and the same server lifecycle callbacks. They are called **per open menu/viewer**, not once globally for a shared terminal. Items must not retain a player, menu, or live stack in their singleton instance. `ITerminalCard.isAvailable()` defaults to true; override it only for stable, side-consistent dependency availability, not per-player conditions. Unavailable cards cannot be inserted, do not count as active in `hasCard`/`countCard`, and receive no lifecycle or widget-factory calls. Installed-card snapshots still include them so cards restored from saves remain visible and recoverable; available-card tooltips exclude them.

Use `MyotusAPI.terminalCards()` for `installedCards`, `installedCardItems`, `availableCards`, `availableCardTooltip`, `hasCard(menu, item/id)`, `countCard(menu, item/id)`, and `canInsertCard`. Legacy `terminalUpgrades()` methods and the old interface's three declared callbacks remain compatible; their queries still cover **both** card kinds. `IMyotusAPI` also exposes `terminalCards()` and `terminalModules()` as default methods.

#### Common-side slot registration

Define a module item using only the public API:

```java
public final class EnderChestModuleItem extends Item implements ITerminalModuleCard {
    public EnderChestModuleItem() {
        super(new Item.Properties().stacksTo(1));
    }
}
```

Register a unique AE2 slot semantic once, and register the provider in `FMLCommonSetupEvent.enqueueWork` on **both client and server**, after item registration:

```java
public static final SlotSemantic ENDER_CHEST = SlotSemantics.register("example_ender_chest_module", false);

// MODULE is your DeferredItem<EnderChestModuleItem>.
event.enqueueWork(() -> MyotusAPI.terminalModules().registerSlots(
        MODULE.get(), ENDER_CHEST,
        menu -> List.of(new Slot(menu.getPlayer().getEnderChestInventory(), 0, 0, 0))));
```

This example exposes the first slot of the player's persistent ender chest; it does not create a temporary inventory. No addon mixin is needed. The factory supplies native slots; Myotus creates guarded views of them, without copying their items into a second inventory. `MyotusAPI.terminalModules().slots(menu, MODULE.get())` returns an immutable list of those managed slots; `isInstalled(menu, MODULE.get())` checks live module presence. The slot list is empty before menu slot initialization or when no provider was registered.

Do not return the same slot object from multiple providers, or return a slot already added to the terminal menu. Use the native slots of the inventory/capability your addon exposes, with backing storage and persistence owned by that inventory.

Providers run once per concrete `MEStorageMenu` (including descendants), **even when the module card is absent**. Slot indices stay fixed for the lifetime of the menu. Factories must return a deterministic order/count on both sides and must not add/remove menu slots themselves. Registration order is normalized by item registry ID and frozen when the first terminal initializes. Duplicate items/semantics, reused slots and late registration are rejected.

Myotus enforces live card presence for click, shift-click, drag and extraction paths, and locks the actual item backing an open wireless terminal while keeping it visible. Native equip/unequip, binding-curse and handler extraction rules remain delegated. Authoritative contents synchronization is allowed while a module is absent; do not mistake synchronization setters for player actions. The public `api.terminal.TerminalModuleSlot` wrapper is also available for specialized native-slot views; returning one with a matching menu/card avoids double wrapping.

For capabilities with custom synchronization/layout, implement `api.terminal.ITerminalModuleProvider` instead of a lambda:

| Hook | Contract |
| --- | --- |
| `createSlots(menu)` | Required; common-side native slot creation, including absent cards |
| `beforeMenuOpen(player)` | Server-only layout synchronization before the open packet; runs regardless of installed cards |
| `isLayoutValid(menu)` | Return false to close/reopen after a topology change; never renumber live slots |
| `getCloneStack(nativeSlot)` | Optional creative clone behavior; called after generic access checks with the original native slot |

Do not reference client classes from common providers. Custom action packets still need their own server-side installation and permission checks; this API is not a custom packet protocol or persistent-storage implementation.

#### Client presentation

Register the UI after item registration, on the client setup work queue only:

```java
event.enqueueWork(() -> MyotusAPI.Client.registerTerminalModule(
        MODULE.get(),
        (screen, initialStack) -> MyotusAPI.Client.terminalModulePanel(
                (MEStorageMenu) screen.getMenu(), ENDER_CHEST)));
```

The standard `terminalModulePanel(menu, semantic)` provides a five-row scrolling native-slot panel with dragging, snapping, layering and exclusion zones. The overload accepting a client `ModConfigSpec.ConfigValue<List<Integer>>` stores the panel position in addon-owned config: an empty list resets placement, and `[x, y]` stores dialog-relative coordinates. It uses Myotus's shared animation/snap preferences.

To include addon-owned positions in Myotus's reset button, call `MyotusAPI.Client.registerTerminalModulePosition(savedPosition)` for each value in client setup's `event.enqueueWork`, independently of widget creation. Registration does not read or save config, ignores repeated registration of the same value, and rejects null. Use a CLIENT config accepting `[]` or an integer pair without a restart requirement; it must be loaded when resetting. The reset button clears and saves registered values through their owning configs. Custom widgets must reread their saved position during layout to observe resets; passing a value to `terminalModulePanel` alone does not register it for this button.

For movable GUI-only modules in NeoForge 1.21.1, extend `me.myogoo.myotus.api.client.AbstractTerminalModuleWidget` and omit slot registration. It requires no menu or slots and shares automatic placement, dragging, animation, snapping, front-to-back input, saved positions and stable exclusion zones with `TerminalEquipmentPanel`. The existing slot panel extends this base without changing its constructors or protected slot-layout hooks. Both classes are included in the `api` JAR.

```java
import me.myogoo.myotus.api.client.AbstractTerminalModuleWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class StatusModuleWidget extends AbstractTerminalModuleWidget {
    public StatusModuleWidget() {
        super(96, 28);
    }

    @Override
    protected void drawPanelBackground(GuiGraphics graphics) {
        var bounds = getBounds();
        graphics.fill(getX(), getY(), getX() + bounds.getWidth(), getY() + bounds.getHeight(), 0xff363636);
        graphics.fill(getX() + 8, getY() + 2, getX() + bounds.getWidth() - 8, getY() + 3, 0xffbbbbbb);
        graphics.drawString(Minecraft.getInstance().font, "Status", getX() + 6, getY() + 10, 0xffffffff, false);
    }
}
```

Register it with `MyotusAPI.Client.registerTerminalModule(MODULE.get(), (screen, initialStack) -> new StatusModuleWidget())`. To persist its position, use `super(width, height, savedPosition)` and register that config value with the reset API described above. The default top five pixels form the drag handle; override `dragHandleHeight()` to change it. `updateContent()` runs after movement, including while hidden. Use `renderContent(...)` and `onContentMouseDown/Up/Drag/Wheel(...)` for custom content; the base manages the outer input and render entry points. Mouse coordinates and graphics positions are relative to the terminal dialog, not the panel origin. Keep drawing inside `getBounds()` and within the assigned Z-layer interval `[0, layerHeight)`. A content click returns `true` by default to prevent click-through; return `false` only to allow native screen input, such as a registered menu slot.

Managed modules must not rely on vanilla children added through `populateScreen` to move, layer or receive input with the panel: those children belong to the whole screen. The original raw AE2 `ICompositeWidget` registration remains available for non-managed widgets using vanilla children and their own layout. In either case the factory receives the terminal's `AEBaseScreen<?>` and a copied initial card stack, and must return a fresh widget. Client registration accepts `ITerminalCard` (including legacy upgrade widgets), rejects other items and duplicate registrations. Keep client widget classes out of the item's common/server class.

The widget attaches when the synchronized card appears and detaches when it is removed. Temporary `AESubScreen` settings/configuration screens never attach terminal panels even though they share the terminal menu; returning to the terminal retains its panel state. Component-only updates and screen resizing reuse the widget instance. Query current card state through `MyotusAPI.terminalCards()` and the screen's `MEStorageMenu`; the initial stack is not a live inventory reference.

The 1.21.1 server config `myotus-server.toml` controls `[terminal].terminalUpgradeSlots` (default **5**, range **1–27**). Change it only while the server is stopped or the singleplayer world is closed, then restart; do not hot-edit it because menu slot counts must match on both sides. Five or fewer slots hide the scrollbar. With more slots, the mouse wheel or scrollbar scrolls only the upgrade column in a five-row viewport; view cells stay fixed. Scrolled-out upgrades within the configured limit remain functional.

Backing storage still holds 27 slots. Lowering the configured limit preserves cards beyond it, but they become inaccessible and inactive until the limit is raised again. These saved cards still prevent inserting duplicate card types. Existing save keys, public `PlayerUpgradeContainer.SIZE`, and slots 0–4 are unchanged. **Remove cards from slots 5–26 before downgrading to an older five-slot build**, which cannot preserve those cards.

The former built-in equipment and Curios feature modules, their recipes, and their feature-specific saved positions are owned by the separate AppliedTerminalModules addon. Myotus retains the generic terminal module API and shared panel, including addon-owned panel positions and the shared animation/snap preferences.

### Module-targeted recipe transfer (NeoForge 1.21.1)

Register a client-only `me.myogoo.myotus.api.client.ITerminalModuleRecipeTransfer` with
`MyotusAPI.Client.registerTerminalModuleRecipeTransfer(MODULE.get(), transfer)` during client setup.
This is separate from widget/slot registration; existing widget subclasses and ordinary recipe transfers are unchanged.

| Method | Responsibility |
| --- | --- |
| `supports(menu, recipe)` | Recipe compatibility only; no mutations or packets. |
| `getError(menu, recipe)` | Check current availability and ingredients; return a message to block transfer, or `null`. |
| `transfer(menu, recipe, maxTransfer)` | Revalidate and send the addon's server request; return whether a request was sent. |

Hold **Alt** while clicking JEI/EMI's recipe **+** button to use this path. Both Alt keys work with the default binding;
the hold key is rebindable in Minecraft Controls under Myotus. **Ctrl takes precedence** and retains the native
request-missing-ingredients behavior. Shift with the module hold key requests `maxTransfer`; an adapter may explicitly
support only one recipe set. The bridge does not collect outputs or start autocrafting.

Myotus chooses the last clicked compatible module on the underlying terminal screen. A sole compatible installed
module is selected automatically. If several remain without a compatible selection, the tooltip asks the player to
click a module first. Removing a card clears its selection. A selected target with missing ingredients does not fall
through to a different module or the terminal's native grid. Multiple selectable targets should expose managed module
widgets so the player can select them by clicking their panels.

The API uses Minecraft `RecipeHolder<?>`, not JEI/EMI classes. Viewer-only synthetic recipes without a backing recipe
are rejected. The card must implement `ITerminalModuleCard`; duplicate registrations are rejected. Registration must
not retain a player, menu or screen. Dry runs never invoke `transfer`. The registered adapter owns any packet and
server extraction; the server must independently validate the current container, installed card, recipe, slot rules,
permissions and item conservation. Myotus only selects and dispatches a target, and adds no shared inventory packet.

AppliedTerminalModules' crafting adapter is a working example: it delegates to the existing shared JEI/EMI preflight
and server-validated one-set crafting transfer. JEI and EMI remain optional runtime mods.

## Experience API

### Pure math and plans

All experience balances/costs are **raw vanilla XP points**, not levels, fluid amounts, or enchantment-library points. `ExperienceMath` is shared pure Java; `MyotusAPI.experience()` delegates to it and adds player/ME operations.

```java
import me.myogoo.myotus.api.MyotusAPI;
import me.myogoo.myotus.api.experience.ExperienceMath;

var xp = MyotusAPI.experience();
long level30 = xp.totalForLevel(30); // 1395
int level = xp.levelForTotal(level30); // 30
var plan = xp.plan(75, new ExperienceMath.ExperienceAmounts(30, 40, 50));
assert plan.canPay() && plan.player() == 30 && plan.fluidXp() == 40
        && plan.appliedExperiencedAmount() == 5;
```

`plan(required, amounts[, priority])` does not mutate anything. Its default order is `PLAYER`, `FLUID_XP`, `APPLIED_EXPERIENCED_AMOUNT`. Duplicate priority entries are ignored; omitted sources cannot pay. `MyoExperience` exposes `required`, `available` (all supplied balances), `spendable` (priority sources), `missing`, per-source use, `used(source)`, `usedAmounts()`, `totalUsed()`, and equivalent `enough()`/`canPay()` checks.

Other math methods are `total(...)`, `intoLevel(total)`, `toNextLevel(level)`, `vanillaAnvilCost(...)`, `apothicAnvilCost(levelCost)`, `apothicEnchantingTableCost(level, slot)`, and `apothicLibraryPointsForLevel(level)`. The latter returns library points, not XP. These are calculations, not live reads of another mod's configuration. Inputs must be non-negative; checked arithmetic can throw on overflow.

Stable identifiers: `appliedModId()` → `appex`, `appliedAeKeyId()` → `appex:experience`, `appliedAmountComponentId()` → `appex:experience_amount`, `fluidXpId()` → `fluid:xp`. An identifier is not a universal fluid conversion rule.

### Storage adapters

The default network adapter is `appliedExperiencedStorage()`, matching both the Applied Experienced AE key type and key ID at one storage unit per raw XP. No Applied Experienced classes are imported by the math API.

Fluid storage is disabled by default. Use `fluidStorage(Predicate<AEKey>, unitsPerXp)` or `taggedFluidStorage(unitsPerXp)` with the conversion from the owning integration. Fluid matchers are restricted to `AEFluidKey`.

Example for 1.20.1/1.21.1, with a caller-supplied fluid ID and configured conversion:

```java
import java.util.List;
import me.myogoo.myotus.api.MyotusAPI;
import net.minecraft.resources.ResourceLocation;

ResourceLocation fluidId = ResourceLocation.fromNamespaceAndPath("examplemod", "liquid_xp");
long unitsPerXp = 250; // Example only: replace with the owning integration's configured rate.
var xp = MyotusAPI.experience();
var fluid = xp.fluidStorage(key -> key.getId().equals(fluidId), unitsPerXp);
var adapters = List.of(xp.appliedExperiencedStorage(), fluid);
var priority = xp.defaultAnvilSourcePriority();
```

`ExperienceStorageAdapter(source, matcher, unitsPerXp)` rejects `PLAYER` and non-positive conversion rates. `toExperience(units)` floors to complete XP groups; `toStorageUnits(xp)` uses checked multiplication. In one operation, only one adapter per source is allowed and matchers must not overlap.

### Read, simulate, then mutate

| Method | Contract |
| --- | --- |
| `playerRaw(player)` | Reconstruct raw XP from level/progress |
| `stored(storage, source/adapter)` / `hasNetworkSource(...)` | Visible stored amount only; no guarantee of power or extraction permission |
| `extractable(energy, storage, actionSource, source/adapter)` | Simulate powered extraction and return raw XP |
| `planPayment(energy, storage, actionSource, player/rawPlayerXp, required, priority[, adapters])` | Non-mutating plan from currently extractable network XP |
| `canConsume(energy, storage, actionSource, player, required, priority[, adapters])` | Preflight affordability; does not reserve XP |
| `consume(energy, storage, actionSource, player, required, priority[, adapters])` | Recheck and pay; requires a server-side `ServerPlayer` |
| `canExtract(...)`, `extractExact(...)`, `extract(..., source/adapter, Actionable)` | Lower-level simulation/extraction in raw XP using a source or adapter |

`availableAnvilSourcePriority(...)` selects usable network sources for the given player selection, power, storage, action source, and optional adapters. `anvilSourcePriority(...)` only builds a priority from an explicit fluid-availability value/flag; it does not inspect a network.

For an actual server action, call `consume` and perform the paid action only on success; a separate `canConsume` is useful for UI state but is not required before `consume`:

```java
boolean paid = xp.consume(energy, storage, actionSource, player, 75, priority, adapters);
if (paid) {
    // Perform the operation whose cost was just paid.
}
```

The variables here are your existing AE2 `IEnergySource`, `MEStorage`, `IActionSource`, server player, and the adapters/priority above. Call on the appropriate server thread. Creative players pass `canConsume` without payment; `consume` still requires the server-side player before returning creative success.

Failed preflight does not mutate state. During execution, network XP is extracted first and player XP last. **There is no transaction spanning multiple AE storage adapters.** If storage changes between simulation and modulation, failure may follow an earlier partial network debit; no cross-source rollback is promised. Serialize competing mutations when using multiple network sources. A payment plan, successful simulation, or per-operation storage snapshot is not a reservation.

Sources: [ExperienceMath](../common/src/main/java/me/myogoo/myotus/api/experience/ExperienceMath.java), [ExperienceStorageAdapter](../neoforge-1-21-1/src/main/java/me/myogoo/myotus/api/experience/ExperienceStorageAdapter.java), and the loader `MyotusAPI` sources linked above.

## Commands

Myotus discovers `@MyoCommand` classes during the loader's `RegisterCommandsEvent`. `@MyoExecute` methods must be static. Context parameters (`CommandSourceStack` or `CommandContext<CommandSourceStack>`) are injected; other parameters need `@MyoArgument`.

```java
import me.myogoo.myotus.api.annotation.commands.MyoCommand;
import me.myogoo.myotus.api.annotation.commands.MyoExecute;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

@MyoCommand("example_status")
public final class ExampleStatusCommand {
    @MyoExecute
    public static int execute(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Example integration is ready"), false);
        return 1;
    }
}
```

`@MyoCommand(parent = ParentCommand.class)` attaches subcommands. `@MyoExecute("path")` adds an execution path and `@MyoAlias` declares aliases. Class-level `@MyoDebug` excludes a command outside dev mode; although the annotation can target methods, the command registrar currently filters classes only. Use unique literals and explicit permissions for mutations.

Built-in adapters cover `int`/`Integer`, `double`/`Double`, `float`/`Float`, `boolean`/`Boolean`, `String`, `Entity`, and `ServerPlayer`. Other exact parameter types need `MyotusAPI.commands().registerArgument(Value.class, adapter)` before the command tree is built. `MyoArgumentAdapter<T>` supplies `ArgumentType<?> argumentType()` and `T value(CommandContext<CommandSourceStack>, String) throws Exception`. Duplicate type registration throws; subclass matching is not automatic, and this hook does not register a custom Brigadier argument serializer with Minecraft.

`@MyoPermission(permission = MyoPermissionLevel.GAME_MASTER)` requires the corresponding command permission. Levels are `MODERATOR` (1), `GAME_MASTER` (2), `ADMIN` (3), and `OWNER` (4). Alternatively use `custom = Checker.class` where a public no-argument checker implements `MyoPermissionChecker`. Set exactly one requirement; empty/conflicting declarations fail closed. Class-level `propagate = true` applies to descendant nodes; otherwise it protects that class's executors.

Sources: [annotations](../common/src/main/java/me/myogoo/myotus/api/annotation/commands), [argument API](../neoforge-1-21-1/src/main/java/me/myogoo/myotus/api/command/argument/MyoArgumentAdapter.java), [command tests](../neoforge-1-21-1/src/test/java/me/myogoo/myotus/api/CommandsApiTest.java).

## Table recipe adapters

`IMyotusTableRecipe<I>` wraps an existing `Recipe<?>`; it does not register a recipe type or serializer. Implement `recipe()` and `tier()`. `sideLength()` defaults to `tier * 2 + 1`, and `gridSize()` squares it. `IMyotusShapedTableRecipe` adds `width()`/`height()`; `IMyotusShapelessTableRecipe` is a marker.

- `unwrap(Class<R>)` returns the underlying recipe only when the requested type matches.
- `tableType()` returns the registered recipe-type ID, not the individual recipe ID.
- `findRecipeId()` returns the recipe's ID on Forge 1.20.1 and empty by default on newer lines, where the caller should retain holder identity.
- Override `createInput(List<ItemStack>)` before using `matches`, `assemble`, or `getRemainingItems`; the default throws `UnsupportedOperationException`. Its input must match the wrapped recipe's expected type.
- `ensureFittedCraftingGrid()` defaults to `slotIngredients()`; it does not automatically pad a shaped recipe to a larger tier grid.

In 26.1.2 `slotIngredients()` expands placement slots into `Optional<Ingredient>` entries, preserving empty slots; impossible placement returns an empty list. `assemble` follows the newer no-registry-argument recipe API. Remaining items use `CraftingRecipe` when applicable and otherwise each input item's crafting remainder. See the [version table](architecture.md#version-differences-that-affect-addons) before sharing adapter code.

Sources: [Forge](../forge-1-20-1/src/main/java/me/myogoo/myotus/api/recipe/IMyotusTableRecipe.java), [1.21.1](../neoforge-1-21-1/src/main/java/me/myogoo/myotus/api/recipe/IMyotusTableRecipe.java), [26.1.2](../neoforge-26-1-2/src/main/java/me/myogoo/myotus/api/recipe/IMyotusTableRecipe.java).

## Resource conditions

`MyoModCondition` tests whether a Myotus integration is active. Use stable mod IDs or explicit aliases for `active_mod` in data files:

Active integrations can also be addressed by their exact namespace or display name, consistent with
`isLoaded(String)`. The registration guard recognizes those active identifiers too, so recipes and
GuideME conditions no longer reject them as unknown. Metadata names are recognized only while active;
use a stable mod ID/alias for predictable missing-mod behavior. A matching name is not necessarily
unique and does not guarantee that `getClass(String)` returns a class.

```json
{
  "type": "myotus:mod_condition",
  "active_mod": "ae2wtlib"
}
```

This is the condition object, not a complete recipe. Place it in the condition structure expected by your loader/datagen API. `new MyoModCondition("ae2wtlib")` is the Java equivalent. Unknown integration identifiers are false and logged; an absent/inactive known integration is also false. It is not a generic loader `mod_loaded` test.

`MyoDevModeCondition.INSTANCE` serializes as `{"type":"myotus:dev"}` and reads `Myotus.DEV_MODE`. That flag initially reflects the development environment but is mutable; it is not a permanently fixed production-build check.

Forge provides `IConditionSerializer` implementations; NeoForge provides `MapCodec` implementations. Do not copy Forge condition serialization code into a NeoForge addon. Sources: [Forge conditions](../forge-1-20-1/src/main/java/me/myogoo/myotus/api/datagen), [1.21.1 conditions](../neoforge-1-21-1/src/main/java/me/myogoo/myotus/api/datagen), [26.1.2 conditions](../neoforge-26-1-2/src/main/java/me/myogoo/myotus/api/datagen).

## Forge-only APIs

### Shared packet channel

Only Forge 1.20.1 exposes `MyotusAPI.network()` and [api.network](../forge-1-20-1/src/main/java/me/myogoo/myotus/api/network). Implement `IMyotusPacket.write(FriendlyByteBuf)` and register a matching decoder/handler with `registerServerbound` or `registerClientbound`.

Packet IDs are non-negative integers on a shared channel: coordinate unique IDs/classes and identical registration on both sides. There is no namespaced allocation API. Sending methods are `sendToServer`, `sendToPlayer`, `sendToAllClients`, and `reply`; `getPacketId` queries the registered type. Context supplies direction, nullable sender, connection, reply, and main-thread work scheduling. Server handlers must validate the sender's authority rather than trusting client payloads.

NeoForge has its own internal payload implementation but no equivalent public Myotus networking facade; use the matching NeoForge API.

### AE2WTLib terminal registration

Only Forge 1.20.1 exposes [AddTerminalEvent](../forge-1-20-1/src/main/java/me/myogoo/myotus/api/wt/AddTerminalEvent.java). Register a callback with `AddTerminalEvent.register(...)` before AE2WTLib initialization. Myotus invokes it once from the AE2WTLib initialization hook, before its upgrade registration; late registration throws. Addons must not call `run()` themselves.

The callback's `addTerminal(...)` overloads accept a terminal name, host factory, menu type, and `IUniversalWirelessTerminalItem`, with optional hotkey/translation names and quantum-bridge-card support. The item must also be a `WirelessTerminalItem`; names must be unique. The hook adds uncharged/charged creative entries and, by default, quantum bridge card support. This API directly references optional AE2WTLib classes, so isolate it behind that dependency. It is not present in the NeoForge API packages.
