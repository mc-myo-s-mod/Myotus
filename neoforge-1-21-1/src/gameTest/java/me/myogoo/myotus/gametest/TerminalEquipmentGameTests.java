package me.myogoo.myotus.gametest;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.ILinkStatus;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.MEStorage;
import appeng.api.util.IConfigManager;
import appeng.menu.ISubMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.SlotSemantic;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.menu.locator.MenuLocators;
import appeng.menu.me.common.MEStorageMenu;
import appeng.util.inv.AppEngInternalInventory;
import me.myogoo.myotus.Myotus;
import me.myogoo.myotus.init.MyoItems;
import me.myogoo.myotus.menu.MyoSlotSemantics;
import me.myogoo.myotus.api.terminal.TerminalModuleSlot;
import me.myogoo.myotus.menu.TerminalUpgradeHost;
import me.myogoo.myotus.menu.TerminalUpgradeSlotFilter;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(Myotus.MODID)
@PrefixGameTestTemplate(false)
public final class TerminalEquipmentGameTests {
    private static final SlotSemantic TEST_MODULE = SlotSemantics.register("myotus_gametest_module", false);

    private TerminalEquipmentGameTests() {
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void testSlotsFollowSubclassAndRetainNativeRules(GameTestHelper helper) {
        var player = MyoExperienceGameTests.makeServerPlayer(helper);
        var menu = new TestMenu(player, new TestHost(player));
        listen(helper, menu);
        var equipment = menu.getSlots(TEST_MODULE);
        helper.assertTrue(equipment.size() == 5,
                "Each terminal must append four armor and one offhand slot");
        helper.assertTrue(equipment.getFirst().index == menu.subclassSlot.index + 1,
                "Permanent equipment slot IDs must follow the concrete menu's slots");
        int slotCount = menu.slots.size();
        listen(helper, menu);
        helper.assertTrue(menu.slots.size() == slotCount, "A second listener must not duplicate slots");
        helper.assertTrue(equipment.stream().noneMatch(Slot::isActive), "Equipment starts disabled without its module");
        install(menu, 0, MyoItems.MYOTUS_UPGRADE_CARD.get());
        helper.assertTrue(equipment.stream().allMatch(Slot::isActive), "Equipment module enables all five slots");
        helper.assertTrue(equipment.getFirst().mayPlace(new ItemStack(Items.IRON_HELMET)),
                "Helmet slot must accept a helmet");
        helper.assertTrue(!equipment.getFirst().mayPlace(new ItemStack(Items.IRON_CHESTPLATE))
                && !equipment.getFirst().mayPlace(new ItemStack(Items.STICK)),
                "Helmet slot must reject the wrong armor type and ordinary items");
        helper.assertTrue(equipment.getFirst().getMaxStackSize() == 1, "Armor retains vanilla stack limits");
        helper.assertTrue(equipment.get(4).mayPlace(new ItemStack(Items.SHIELD)),
                "Offhand slot must accept an offhand item");
        var cursed = new ItemStack(Items.IRON_HELMET);
        cursed.enchant(player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.BINDING_CURSE), 1);
        equipment.getFirst().setByPlayer(cursed);
        helper.assertTrue(!player.isCreative() && !equipment.getFirst().mayPickup(player),
                "Binding curse must deny survival unequipping through a terminal");
        menu.quickMoveStack(player, equipment.getFirst().index);
        menu.clicked(equipment.getFirst().index, 0, ClickType.PICKUP, player);
        helper.assertTrue(ItemStack.matches(cursed, equipment.getFirst().getItem()) && menu.getCarried().isEmpty(),
                "Binding curse must block shift-click and normal pickup without changing gear");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void removedModulesPreserveGearAndRejectAllEquipmentClicks(GameTestHelper helper) {
        var player = MyoExperienceGameTests.makeServerPlayer(helper);
        var menu = new TestMenu(player, new TestHost(player));
        listen(helper, menu);
        install(menu, 0, MyoItems.MYOTUS_UPGRADE_CARD.get());
        Slot helmet = menu.getSlots(TEST_MODULE).getFirst();
        Slot offhand = menu.getSlots(TEST_MODULE).get(4);
        helmet.setByPlayer(new ItemStack(Items.IRON_HELMET));
        offhand.setByPlayer(new ItemStack(Items.SHIELD));
        menu.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT).get(0).set(ItemStack.EMPTY);
        player.getAbilities().instabuild = true;
        for (Slot slot : new Slot[] {helmet, offhand}) {
            ItemStack original = slot.getItem().copy();
            helper.assertTrue(!slot.isActive() && !slot.mayPickup(player)
                    && !slot.mayPlace(new ItemStack(Items.STICK)),
                    "Removing the module disables equipment interaction");
            for (ClickType click : new ClickType[] {ClickType.PICKUP, ClickType.QUICK_MOVE,
                    ClickType.SWAP, ClickType.CLONE, ClickType.THROW, ClickType.PICKUP_ALL}) {
                menu.clicked(slot.index, click == ClickType.SWAP ? 0 : 1, click, player);
                helper.assertTrue(ItemStack.matches(original, slot.getItem()) && menu.getCarried().isEmpty(),
                        "Disabled equipment must reject " + click + " without cloning or losing gear");
            }
            menu.quickMoveStack(player, slot.index);
            helper.assertTrue(ItemStack.matches(original, slot.getItem()),
                    "Direct quick move must also reject a disabled slot");
        }
        helper.assertTrue(menu.getSlots(TEST_MODULE).stream().noneMatch(Slot::isActive),
                "Removing the single equipment module disables all five slots");
        helper.assertTrue(player.getInventory().countItem(Items.IRON_HELMET) == 1
                && player.getInventory().countItem(Items.SHIELD) == 1,
                "Module removal must preserve the original player equipment exactly once");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void sharedModuleRemovalPreservesBothArmorClickOrders(GameTestHelper helper) {
        for (boolean armorFirst : new boolean[] {true, false}) {
            var player = MyoExperienceGameTests.makeServerPlayer(helper);
            var otherPlayer = MyoExperienceGameTests.makeServerPlayer(helper);
            var host = new TestHost(player);
            var menu = new TestMenu(player, host);
            var otherMenu = new TestMenu(otherPlayer, host);
            listen(helper, menu);
            listen(helper, otherMenu);
            install(menu, 0, MyoItems.MYOTUS_UPGRADE_CARD.get());
            var originalSlots = List.copyOf(menu.slots);
            var otherOriginalSlots = List.copyOf(otherMenu.slots);
            Slot helmet = menu.getSlots(TEST_MODULE).getFirst();
            helmet.setByPlayer(new ItemStack(Items.IRON_HELMET));
            helper.assertTrue(otherMenu.getSlots(TEST_MODULE).getFirst().isActive(),
                    "Both viewers must read the same installed equipment module");

            if (armorFirst) {
                menu.clicked(helmet.index, 0, ClickType.PICKUP, player);
                helper.assertTrue(helmet.getItem().isEmpty() && menu.getCarried().is(Items.IRON_HELMET),
                        "Armor pickup before module removal must move the helmet to its owner's cursor");
            }
            otherMenu.clicked(otherMenu.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT).getFirst().index,
                    0, ClickType.PICKUP, otherPlayer);
            menu.clicked(helmet.index, 0, ClickType.PICKUP, player);
            helper.assertTrue(helmet.getItem().isEmpty() == armorFirst
                    && menu.getCarried().is(Items.IRON_HELMET) == armorFirst
                    && player.getInventory().countItem(Items.IRON_HELMET) + menu.getCarried().getCount() == 1
                    && otherPlayer.getInventory().countItem(Items.IRON_HELMET) == 0,
                    "A stale armor click must preserve exactly one helmet in either server click order");
            assertSharedModuleRemoved(helper, host, menu, otherMenu);
            assertSlotLayoutUnchanged(helper, menu, originalSlots);
            assertSlotLayoutUnchanged(helper, otherMenu, otherOriginalSlots);
            if (armorFirst) {
                menu.clicked(menu.getSlots(SlotSemantics.PLAYER_INVENTORY).getFirst().index,
                        0, ClickType.PICKUP, player);
                helper.assertTrue(menu.getCarried().isEmpty()
                        && player.getInventory().countItem(Items.IRON_HELMET) == 1 && helmet.getItem().isEmpty(),
                        "An already-picked-up helmet must remain movable to inventory after module removal");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void sharedModuleRemovalDuringArmorDragPreservesCursor(GameTestHelper helper) {
        var player = MyoExperienceGameTests.makeServerPlayer(helper);
        var otherPlayer = MyoExperienceGameTests.makeServerPlayer(helper);
        var host = new TestHost(player);
        var menu = new TestMenu(player, host);
        var otherMenu = new TestMenu(otherPlayer, host);
        listen(helper, menu);
        listen(helper, otherMenu);
        install(menu, 0, MyoItems.MYOTUS_UPGRADE_CARD.get());
        var originalSlots = List.copyOf(menu.slots);
        var otherOriginalSlots = List.copyOf(otherMenu.slots);
        Slot helmet = menu.getSlots(TEST_MODULE).getFirst();
        menu.setCarried(new ItemStack(Items.IRON_HELMET));
        menu.clicked(-999, AbstractContainerMenu.getQuickcraftMask(0, 0), ClickType.QUICK_CRAFT, player);
        menu.clicked(helmet.index, AbstractContainerMenu.getQuickcraftMask(1, 0), ClickType.QUICK_CRAFT, player);
        otherMenu.clicked(otherMenu.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT).getFirst().index,
                0, ClickType.PICKUP, otherPlayer);
        menu.clicked(-999, AbstractContainerMenu.getQuickcraftMask(2, 0), ClickType.QUICK_CRAFT, player);
        helper.assertTrue(helmet.getItem().isEmpty() && menu.getCarried().is(Items.IRON_HELMET)
                && menu.getCarried().getCount() == 1
                && player.getInventory().countItem(Items.IRON_HELMET) == 0,
                "Finishing a drag captured before removal must not insert into disabled armor or lose its cursor");
        menu.clicked(menu.getSlots(SlotSemantics.PLAYER_INVENTORY).getFirst().index,
                0, ClickType.PICKUP, player);
        helper.assertTrue(menu.getCarried().isEmpty() && player.getInventory().countItem(Items.IRON_HELMET) == 1
                && helmet.getItem().isEmpty(), "Finishing the interrupted drag must leave normal inventory clicks usable");
        assertSharedModuleRemoved(helper, host, menu, otherMenu);
        assertSlotLayoutUnchanged(helper, menu, originalSlots);
        assertSlotLayoutUnchanged(helper, otherMenu, otherOriginalSlots);
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void staleArmorClickAfterSharedModuleRemovalResetsDrag(GameTestHelper helper) {
        var player = MyoExperienceGameTests.makeServerPlayer(helper);
        var otherPlayer = MyoExperienceGameTests.makeServerPlayer(helper);
        var host = new TestHost(player);
        var menu = new TestMenu(player, host);
        var otherMenu = new TestMenu(otherPlayer, host);
        listen(helper, menu);
        listen(helper, otherMenu);
        install(menu, 0, MyoItems.MYOTUS_UPGRADE_CARD.get());
        var originalSlots = List.copyOf(menu.slots);
        var otherOriginalSlots = List.copyOf(otherMenu.slots);
        Slot helmet = menu.getSlots(TEST_MODULE).getFirst();
        menu.setCarried(new ItemStack(Items.IRON_HELMET));
        menu.clicked(-999, AbstractContainerMenu.getQuickcraftMask(0, 0), ClickType.QUICK_CRAFT, player);
        menu.clicked(helmet.index, AbstractContainerMenu.getQuickcraftMask(1, 0), ClickType.QUICK_CRAFT, player);
        otherMenu.clicked(otherMenu.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT).getFirst().index,
                0, ClickType.PICKUP, otherPlayer);
        menu.clicked(helmet.index, 0, ClickType.PICKUP, player);
        helper.assertTrue(helmet.getItem().isEmpty() && menu.getCarried().is(Items.IRON_HELMET)
                && menu.getCarried().getCount() == 1,
                "The stale disabled armor click must leave its carried helmet untouched");
        Slot inventory = menu.getSlots(SlotSemantics.PLAYER_INVENTORY).getFirst();
        menu.clicked(inventory.index, 0, ClickType.PICKUP, player);
        helper.assertTrue(menu.getCarried().isEmpty() && inventory.getItem().is(Items.IRON_HELMET)
                && player.getInventory().countItem(Items.IRON_HELMET) == 1 && helmet.getItem().isEmpty(),
                "Rejecting a stale armor click must reset the old drag, not discard the next valid inventory click");
        assertSharedModuleRemoved(helper, host, menu, otherMenu);
        assertSlotLayoutUnchanged(helper, menu, originalSlots);
        assertSlotLayoutUnchanged(helper, otherMenu, otherOriginalSlots);
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void sharedModuleRemovalPreventsPickupAllFromDisabledEquipment(GameTestHelper helper) {
        var player = MyoExperienceGameTests.makeServerPlayer(helper);
        var otherPlayer = MyoExperienceGameTests.makeServerPlayer(helper);
        var host = new TestHost(player);
        var menu = new TestMenu(player, host);
        var otherMenu = new TestMenu(otherPlayer, host);
        listen(helper, menu);
        listen(helper, otherMenu);
        install(menu, 0, MyoItems.MYOTUS_UPGRADE_CARD.get());
        var originalSlots = List.copyOf(menu.slots);
        var otherOriginalSlots = List.copyOf(otherMenu.slots);
        Slot offhand = menu.getSlots(TEST_MODULE).get(4);
        offhand.setByPlayer(new ItemStack(Items.DIAMOND, 3));
        var inventory = menu.getSlots(SlotSemantics.PLAYER_INVENTORY);
        inventory.getFirst().set(new ItemStack(Items.DIAMOND, 4));
        menu.setCarried(new ItemStack(Items.DIAMOND, 2));
        otherMenu.clicked(otherMenu.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT).getFirst().index,
                0, ClickType.PICKUP, otherPlayer);
        menu.clicked(inventory.getLast().index, 0, ClickType.PICKUP_ALL, player);
        helper.assertTrue(inventory.getFirst().getItem().isEmpty()
                && menu.getCarried().is(Items.DIAMOND) && menu.getCarried().getCount() == 6
                && offhand.getItem().is(Items.DIAMOND) && offhand.getItem().getCount() == 3
                && player.getInventory().countItem(Items.DIAMOND) + menu.getCarried().getCount() == 9
                && otherPlayer.getInventory().countItem(Items.DIAMOND) == 0,
                "Pickup-all from an ordinary slot must collect inventory items but leave disabled offhand untouched");
        assertSharedModuleRemoved(helper, host, menu, otherMenu);
        assertSlotLayoutUnchanged(helper, menu, originalSlots);
        assertSlotLayoutUnchanged(helper, otherMenu, otherOriginalSlots);
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void shiftClickMovesEquipmentToInventoryAndInventoryToMe(GameTestHelper helper) {
        var player = MyoExperienceGameTests.makeServerPlayer(helper);
        var host = new TestHost(player);
        var menu = new TestMenu(player, host);
        listen(helper, menu);
        install(menu, 0, MyoItems.MYOTUS_UPGRADE_CARD.get());
        menu.setLinkStatus(ILinkStatus.ofConnected());
        Slot helmet = menu.getSlots(TEST_MODULE).getFirst();
        Slot offhand = menu.getSlots(TEST_MODULE).get(4);
        helmet.setByPlayer(new ItemStack(Items.IRON_HELMET));
        offhand.setByPlayer(new ItemStack(Items.SHIELD));
        menu.quickMoveStack(player, helmet.index);
        menu.quickMoveStack(player, offhand.index);
        helper.assertTrue(helmet.getItem().isEmpty() && offhand.getItem().isEmpty()
                && player.getInventory().countItem(Items.IRON_HELMET) == 1
                && player.getInventory().countItem(Items.SHIELD) == 1 && host.storage.inserted == 0,
                "Equipment shift-click must move to main inventory, never into ME storage");
        Slot inventory = menu.getSlots(SlotSemantics.PLAYER_INVENTORY).getLast();
        inventory.set(new ItemStack(Items.IRON_CHESTPLATE));
        menu.quickMoveStack(player, inventory.index);
        helper.assertTrue(inventory.getItem().isEmpty() && host.storage.inserted == 1
                && AEItemKey.of(Items.IRON_CHESTPLATE).equals(host.storage.lastKey)
                && menu.getSlots(TEST_MODULE).get(1).getItem().isEmpty(),
                "Inventory shift-click must use ME insertion without auto-equipping");
        menu.setLinkStatus(ILinkStatus.ofDisconnected());
        inventory.set(new ItemStack(Items.IRON_CHESTPLATE));
        menu.quickMoveStack(player, inventory.index);
        helper.assertTrue(inventory.getItem().is(Items.IRON_CHESTPLATE)
                && menu.getSlots(TEST_MODULE).get(1).getItem().isEmpty(),
                "Disconnected network fallback must not auto-equip armor either");
        Slot refusing = menu.addRefusingEquipmentSlot();
        refusing.setByPlayer(new ItemStack(Items.IRON_HELMET));
        int helmetCount = player.getInventory().countItem(Items.IRON_HELMET);
        helper.assertTrue(refusing.mayPickup(player), "Extraction fixture must allow pickup simulation");
        menu.quickMoveStack(player, refusing.index);
        helper.assertTrue(refusing.getItem().is(Items.IRON_HELMET)
                && player.getInventory().countItem(Items.IRON_HELMET) == helmetCount,
                "A real extraction refusal after successful simulation must not duplicate equipment");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void offhandTerminalHostCannotBeMovedThroughModule(GameTestHelper helper) {
        var player = MyoExperienceGameTests.makeServerPlayer(helper);
        player.getInventory().setItem(40, new ItemStack(Items.STICK));
        var host = new TestHost(player, MenuLocators.forInventorySlot(40));
        var menu = new TestMenu(player, host);
        listen(helper, menu);
        install(menu, 0, MyoItems.MYOTUS_UPGRADE_CARD.get());
        Slot offhand = menu.getSlots(TEST_MODULE).get(4);
        helper.assertTrue(menu.isPlayerInventorySlotLocked(40) && menu.getSlots(TEST_MODULE)
                .getFirst().isActive(), "Only the hosting offhand slot is inventory-locked");
        assertHostVisibleAndLocked(helper, menu, offhand);
        helper.assertTrue(player.getInventory().getItem(40).is(Items.STICK)
                && player.getInventory().countItem(Items.STICK) == 1 && menu.getCarried().isEmpty(),
                "Offhand module cannot remove or duplicate its own terminal host");
        helper.succeed();
    }

    private static void install(MEStorageMenu menu, int index, Item module) {
        menu.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT).get(index).set(new ItemStack(module));
    }

    private static void assertHostVisibleAndLocked(GameTestHelper helper, TestMenu menu, Slot slot) {
        Player player = menu.getPlayer();
        ItemStack original = slot.getItem().copy();
        helper.assertTrue(slot.isActive() && !original.isEmpty() && slot.hasItem()
                && !slot.allowModification(player) && !slot.mayPickup(player)
                && !slot.mayPlace(original.copy()) && !slot.mayPlace(new ItemStack(Items.DIAMOND)),
                "The terminal host must remain visible and readable without allowing pickup or replacement");
        helper.assertTrue(slot.remove(1).isEmpty() && ItemStack.matches(original, slot.getItem()),
                "Direct extraction must leave the visible terminal host untouched");
        menu.quickMoveStack(player, slot.index);
        helper.assertTrue(ItemStack.matches(original, slot.getItem()) && menu.getCarried().isEmpty(),
                "Direct quick move must not remove the visible terminal host");
        ItemStack hotbar = player.getInventory().getItem(8).copy();
        ItemStack offhand = player.getInventory().getItem(40).copy();
        boolean creative = player.getAbilities().instabuild;
        player.getInventory().setItem(8, new ItemStack(Items.DIAMOND, 2));
        player.getAbilities().instabuild = true;
        try {
            for (ClickType click : new ClickType[] {ClickType.PICKUP, ClickType.QUICK_MOVE, ClickType.SWAP,
                    ClickType.CLONE, ClickType.THROW, ClickType.PICKUP_ALL}) {
                menu.clicked(slot.index, click == ClickType.SWAP ? 8 : click == ClickType.CLONE ? 2 : 0,
                        click, player);
                helper.assertTrue(ItemStack.matches(original, slot.getItem()) && menu.getCarried().isEmpty(),
                        "A visible locked terminal host must reject " + click);
            }
            menu.clicked(slot.index, 40, ClickType.SWAP, player);
            helper.assertTrue(ItemStack.matches(original, slot.getItem())
                    && ItemStack.matches(offhand, player.getInventory().getItem(40))
                    && player.getInventory().getItem(8).is(Items.DIAMOND)
                    && player.getInventory().getItem(8).getCount() == 2,
                    "Host swap attempts must leave hotbar and offhand contents unchanged");
            ItemStack carried = new ItemStack(Items.DIAMOND, 4);
            menu.setCarried(carried.copy());
            menu.clicked(slot.index, 0, ClickType.PICKUP, player);
            helper.assertTrue(ItemStack.matches(original, slot.getItem())
                    && ItemStack.matches(carried, menu.getCarried()),
                    "Replacing the terminal host must preserve both the host and carried stack");
            carried = original.copyWithCount(4);
            menu.setCarried(carried.copy());
            menu.clicked(-999, AbstractContainerMenu.getQuickcraftMask(0, 0), ClickType.QUICK_CRAFT, player);
            menu.clicked(slot.index, AbstractContainerMenu.getQuickcraftMask(1, 0), ClickType.QUICK_CRAFT, player);
            menu.clicked(-999, AbstractContainerMenu.getQuickcraftMask(2, 0), ClickType.QUICK_CRAFT, player);
            helper.assertTrue(ItemStack.matches(original, slot.getItem())
                    && ItemStack.matches(carried, menu.getCarried()),
                    "Quick-craft must not merge into or replace the locked terminal host");
        } finally {
            menu.setCarried(ItemStack.EMPTY);
            player.getInventory().setItem(8, hotbar);
            player.getAbilities().instabuild = creative;
        }
    }

    private static void assertSharedModuleRemoved(GameTestHelper helper, TestHost host,
            TestMenu menu, TestMenu otherMenu) {
        helper.assertTrue(host.upgrades.getStackInSlot(0).isEmpty()
                && menu.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT).getFirst().getItem().isEmpty()
                && otherMenu.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT).getFirst().getItem().isEmpty()
                && otherMenu.getCarried().is(MyoItems.MYOTUS_UPGRADE_CARD.get())
                && otherMenu.getCarried().getCount() == 1
                && menu.getPlayer().getInventory().countItem(MyoItems.MYOTUS_UPGRADE_CARD.get()) == 0
                && otherMenu.getPlayer().getInventory().countItem(MyoItems.MYOTUS_UPGRADE_CARD.get()) == 0,
                "Module removal by the other viewer must move exactly one shared module to that viewer's cursor");
        helper.assertTrue(menu.getSlots(TEST_MODULE).stream().noneMatch(Slot::isActive)
                && otherMenu.getSlots(TEST_MODULE).stream().noneMatch(Slot::isActive),
                "Removing the shared module must immediately disable equipment for both viewers");
    }

    private static void assertSlotLayoutUnchanged(GameTestHelper helper, TestMenu menu, List<Slot> originalSlots) {
        helper.assertTrue(menu.slots.equals(originalSlots), "Shared module removal must not replace or reorder slots");
        for (int i = 0; i < originalSlots.size(); i++) {
            helper.assertTrue(originalSlots.get(i).index == i,
                    "Shared module removal must preserve every existing slot's packet index");
        }
    }

    private static void listen(GameTestHelper helper, MEStorageMenu menu) {
        try {
            menu.addSlotListener(new ContainerListener() {
                @Override
                public void slotChanged(AbstractContainerMenu container, int slot, ItemStack stack) {
                }

                @Override
                public void dataChanged(AbstractContainerMenu container, int id, int value) {
                }
            });
        } catch (RuntimeException error) {
            helper.assertTrue(error.getMessage() != null
                    && error.getMessage().contains("may not be sent to the client"),
                    "Unexpected embedded-player menu initialization failure: " + error);
        }
    }

    private static final class TestMenu extends MEStorageMenu {
        private final Slot subclassSlot;

        private TestMenu(ServerPlayer player, TestHost host) {
            super(MEStorageMenu.TYPE, 70, player.getInventory(), host);
            subclassSlot = addSlot(new Slot(new SimpleContainer(1), 0, -10000, -10000) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
            // Only this test menu owns these slots; no built-in item or global provider is registered.
            for (int index : new int[] {5, 6, 7, 8, 45}) {
                addSlot(new TerminalModuleSlot(this, player.inventoryMenu.getSlot(index),
                        MyoItems.MYOTUS_UPGRADE_CARD.get()), TEST_MODULE);
            }
        }

        private Slot addRefusingEquipmentSlot() {
            Slot refusing = new Slot(getPlayer().getInventory(), 39, -10000, -10000) {
                @Override
                public ItemStack safeTake(int amount, int limit, Player player) {
                    return ItemStack.EMPTY;
                }
            };
            return addSlot(new TerminalModuleSlot(this, refusing, MyoItems.MYOTUS_UPGRADE_CARD.get()));
        }
    }

    private static final class TestHost extends ItemMenuHost<Item>
            implements ITerminalHost, TerminalUpgradeHost, IEnergySource {
        private final InsertStorage storage = new InsertStorage();
        private final AppEngInternalInventory upgrades = new AppEngInternalInventory(null, 3, 1,
                TerminalUpgradeSlotFilter.INSTANCE);
        private final IConfigManager config = IConfigManager.builder(() -> {}).build();

        private TestHost(Player player) {
            this(player, MenuLocators.forStack(new ItemStack(Items.STICK)));
        }

        private TestHost(Player player, ItemMenuHostLocator locator) {
            super(Items.STICK, player, locator);
        }

        @Override
        public AppEngInternalInventory myotus$getUpgradeInventory() {
            return upgrades;
        }

        @Override
        public MEStorage getInventory() {
            return storage;
        }

        @Override
        public ILinkStatus getLinkStatus() {
            return ILinkStatus.ofDisconnected();
        }

        @Override
        public IConfigManager getConfigManager() {
            return config;
        }

        @Override
        public void returnToMainMenu(Player player, ISubMenu subMenu) {
        }

        @Override
        public ItemStack getMainMenuIcon() {
            return ItemStack.EMPTY;
        }

        @Override
        public double extractAEPower(double amount, Actionable mode, PowerMultiplier multiplier) {
            return amount;
        }
    }

    private static final class InsertStorage implements MEStorage {
        private long inserted;
        private AEKey lastKey;

        @Override
        public long insert(AEKey key, long amount, Actionable mode, IActionSource source) {
            if (mode == Actionable.MODULATE) {
                inserted += amount;
                lastKey = key;
            }
            return amount;
        }

        @Override
        public Component getDescription() {
            return Component.literal("Equipment GameTest storage");
        }
    }

}
