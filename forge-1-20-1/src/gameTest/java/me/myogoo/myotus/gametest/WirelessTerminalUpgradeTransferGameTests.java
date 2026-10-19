package me.myogoo.myotus.gametest;

import appeng.util.inv.PlayerInternalInventory;
import me.myogoo.myotus.Myotus;
import me.myogoo.myotus.init.MyoItems;
import me.myogoo.myotus.menu.PlayerUpgradeContainer;
import me.myogoo.myotus.menu.WirelessTerminalUpgradeTransfer;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Myotus.MODID)
@PrefixGameTestTemplate(false)
public final class WirelessTerminalUpgradeTransferGameTests {
    private static final String UUID_TAG = "myotus_terminal_storage_uuid";
    private static final String SOURCES_TAG = "myotus_terminal_upgrade_sources";
    private static final String FIRST = "12345678-abcd-1234-abcd-123456789012";
    private static final String SECOND = "87654321-abcd-1234-abcd-123456789012";
    private static final String SOURCE = "item:example.WirelessHost:example:terminal:" + FIRST;
    private static final String DESTINATION = "item:ae2wtlib:wireless_universal_terminal:" + SECOND;

    @GameTestGenerator
    public static List<TestFunction> migrationTests() {
        if (!ModList.get().isLoaded("ae2wtlib")) return List.of();
        return List.of(
                migrationTest("duplicateIsReturnedAndConsumedSourceCannotBeImportedAgain",
                        WirelessTerminalUpgradeTransferGameTests::duplicateIsReturnedAndConsumedSourceCannotBeImportedAgain),
                migrationTest("fullInventoryRetainsExactRemainderAndUnknownItemBytesAcrossClone",
                        WirelessTerminalUpgradeTransferGameTests::fullInventoryRetainsExactRemainderAndUnknownItemBytesAcrossClone),
                migrationTest("fullDestinationAndPlayerInventoryLeaveSourceUntouched",
                        WirelessTerminalUpgradeTransferGameTests::fullDestinationAndPlayerInventoryLeaveSourceUntouched),
                migrationTest("currentUuidRecoversLegacyKeyButOtherPlayersAndOtherUuidsStayUntouched",
                        WirelessTerminalUpgradeTransferGameTests::currentUuidRecoversLegacyKeyButOtherPlayersAndOtherUuidsStayUntouched),
                migrationTest("upgradingAnUnopenedLegacyWutStillFindsItsOriginalGenericKey",
                        WirelessTerminalUpgradeTransferGameTests::upgradingAnUnopenedLegacyWutStillFindsItsOriginalGenericKey),
                migrationTest("undecodableDestinationCannotBeOverwrittenByMigration",
                        WirelessTerminalUpgradeTransferGameTests::undecodableDestinationCannotBeOverwrittenByMigration));
    }

    private static TestFunction migrationTest(String name, Consumer<GameTestHelper> test) {
        return new TestFunction("defaultBatch", "wirelessterminalupgradetransfergametests." + name,
                "myotus:empty", Rotation.NONE, 20, 0, true, test);
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void previewsAreDeterministicAndNeverChangeInputs(GameTestHelper helper) {
        ItemStack input = terminal(Items.APPLE, FIRST);
        ItemStack second = terminal(Items.BOOK, SECOND);
        ItemStack unopened = new ItemStack(Items.STICK);
        CompoundTag before = data(input);
        ItemStack result = new ItemStack(Items.DIAMOND);
        result.getOrCreateTag().putString("othermod", "keep");
        ItemStack preview = result.copy();
        WirelessTerminalUpgradeTransfer.prepareResult(result, List.of(input, second, unopened));
        WirelessTerminalUpgradeTransfer.prepareResult(preview, List.of(second, input, input, unopened));
        helper.assertTrue(data(result).equals(data(preview)), "Repeated previews must be deterministic");
        helper.assertTrue(before.equals(data(input)), "Preview must not mutate its input");
        helper.assertFalse(data(unopened).contains(UUID_TAG), "Preview must not assign input UUIDs");
        helper.assertTrue("keep".equals(data(result).getString("othermod")), "Unrelated NBT must survive");
        helper.assertFalse(UUID.fromString(FIRST).equals(data(result).getUUID(UUID_TAG)),
                "Result must have a distinct stable identity");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void subsequentCraftRetainsAncestorsWithoutAddingMalformedSources(GameTestHelper helper) {
        ItemStack input = terminal(Items.APPLE, FIRST);
        ListTag ancestors = new ListTag();
        ancestors.add(StringTag.valueOf("example:old_terminal:" + SECOND));
        ancestors.add(StringTag.valueOf("not a storage identity"));
        input.getOrCreateTag().put(SOURCES_TAG, ancestors);
        ItemStack result = new ItemStack(Items.DIAMOND);
        WirelessTerminalUpgradeTransfer.prepareResult(result, List.of(input));
        ListTag saved = data(result).getList(SOURCES_TAG, Tag.TAG_STRING);
        helper.assertTrue(saved.size() == 2, "Only the input and valid ancestor may be retained");
        helper.assertTrue(saved.contains(StringTag.valueOf("example:old_terminal:" + SECOND)),
                "The original ancestor must survive another craft");
        helper.succeed();
    }

    private static void duplicateIsReturnedAndConsumedSourceCannotBeImportedAgain(GameTestHelper helper) {
        var player = playerWithSource(helper, 2);
        ItemStack result = wut(SECOND, "example:terminal:" + FIRST);
        var destination = open(player, result);
        helper.assertTrue(destination.getStackInSlot(0).is(card()), "One card must be installed");
        helper.assertTrue(player.getInventory().countItem(card()) == 1, "One duplicate must be returned");
        helper.assertFalse(player.getPersistentData().contains(SOURCE), "Consumed source must be removed");
        CompoundTag saved = player.getPersistentData().copy();
        open(player, result);
        helper.assertTrue(saved.equals(player.getPersistentData()), "Reopening must not rewrite consumed sources");
        helper.assertTrue(player.getInventory().countItem(card()) == 1, "Reopening must not return another card");
        helper.succeed();
    }

    private static void fullInventoryRetainsExactRemainderAndUnknownItemBytesAcrossClone(GameTestHelper helper) {
        var player = playerWithSource(helper, 3);
        fillInventory(player.getInventory());
        CompoundTag unknown = unknownCard();
        player.getPersistentData().getList(SOURCE, Tag.TAG_COMPOUND).add(unknown.copy());
        ItemStack result = wut(SECOND, "example:terminal:" + FIRST);
        var destination = open(player, result);
        ListTag remaining = player.getPersistentData().getList(SOURCE, Tag.TAG_COMPOUND);
        helper.assertTrue(remaining.getCompound(0).getByte("Count") == 2, "Only the installed card is consumed");
        helper.assertTrue(unknown.equals(remaining.getCompound(1)), "Unknown item NBT must be retained exactly");
        helper.assertTrue(destination.getStackInSlot(1).isEmpty(), "Duplicate cards cannot occupy another slot");
        var cloned = MyoExperienceGameTests.makeMenuPlayer(helper);
        PlayerUpgradeContainer.onPlayerClone(new PlayerEvent.Clone(cloned, player, true));
        helper.assertTrue(player.getPersistentData().equals(cloned.getPersistentData()),
                "Clone must retain destination and pending source data");
        open(cloned, result);
        helper.assertTrue(cloned.getInventory().countItem(card()) == 2, "Pending duplicates return when space exists");
        ListTag unresolved = cloned.getPersistentData().getList(SOURCE, Tag.TAG_COMPOUND);
        helper.assertTrue(unresolved.size() == 1 && unknown.equals(unresolved.getCompound(0)),
                "Unknown data must remain after known remainder is returned");
        helper.succeed();
    }

    private static void fullDestinationAndPlayerInventoryLeaveSourceUntouched(GameTestHelper helper) {
        var player = playerWithSource(helper, 1);
        fillInventory(player.getInventory());
        var savedDestination = new PlayerUpgradeContainer(player, DESTINATION);
        for (int slot = 0; slot < PlayerUpgradeContainer.SIZE; slot++) {
            savedDestination.setItemDirect(slot, new ItemStack(Items.DIAMOND));
        }
        CompoundTag before = player.getPersistentData().copy();
        open(player, wut(SECOND, "example:terminal:" + FIRST));
        helper.assertTrue(before.equals(player.getPersistentData()),
                "A full destination and player inventory must leave source and destination unchanged");
        helper.succeed();
    }

    private static void currentUuidRecoversLegacyKeyButOtherPlayersAndOtherUuidsStayUntouched(GameTestHelper helper) {
        var player = playerWithSource(helper, 1);
        String unrelatedKey = "item:example.WirelessHost:example:terminal:" + SECOND;
        player.getPersistentData().put(unrelatedKey, player.getPersistentData().get(SOURCE).copy());
        var otherPlayer = playerWithSource(helper, 1);
        CompoundTag beforeOther = otherPlayer.getPersistentData().copy();
        var destination = open(player, wut(FIRST));
        helper.assertTrue(destination.getStackInSlot(0).is(card()), "Legacy matching UUID must migrate");
        helper.assertFalse(player.getPersistentData().contains(SOURCE), "Only matching source is consumed");
        helper.assertTrue(player.getPersistentData().contains(unrelatedKey), "Other UUID must remain untouched");
        helper.assertTrue(beforeOther.equals(otherPlayer.getPersistentData()), "Other player's data must not change");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void fullMainInventoryReturnsRemainderWithoutDroppingOrDiscardingIt(GameTestHelper helper) {
        var player = helper.makeMockPlayer();
        Inventory inventory = player.getInventory();
        fillInventory(inventory);
        for (boolean creative : new boolean[] { false, true }) {
            player.getAbilities().instabuild = creative;
            ItemStack remainder = new PlayerInternalInventory(inventory).addItems(new ItemStack(Items.APPLE, 2));
            helper.assertTrue(remainder.getCount() == 2, "Full inventory must return the whole remainder");
            for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
                helper.assertTrue(ItemStack.matches(new ItemStack(Items.COBBLESTONE, 64), inventory.getItem(slot)),
                        "Full inventory contents must remain unchanged");
            }
        }
        helper.succeed();
    }

    private static void upgradingAnUnopenedLegacyWutStillFindsItsOriginalGenericKey(GameTestHelper helper) {
        var player = playerWithSource(helper, 1);
        var destination = open(player, wut(SECOND, "ae2wtlib:wireless_universal_terminal:" + FIRST));
        helper.assertFalse(player.getPersistentData().contains(SOURCE), "Ancestor WUT UUID must find legacy source");
        helper.assertTrue(destination.getStackInSlot(0).is(card()), "Legacy source card must be installed");
        helper.succeed();
    }

    private static void undecodableDestinationCannotBeOverwrittenByMigration(GameTestHelper helper) {
        var player = playerWithSource(helper, 1);
        ListTag missing = new ListTag();
        missing.add(unknownCard());
        player.getPersistentData().put(DESTINATION, missing);
        CompoundTag before = player.getPersistentData().copy();
        var destination = open(player, wut(SECOND, "example:terminal:" + FIRST));
        helper.assertTrue(before.equals(player.getPersistentData()), "Unknown destination data cannot be overwritten");
        helper.assertTrue(destination.isEmpty(), "Migration must not insert into an undecodable destination");
        helper.assertTrue(player.getInventory().countItem(card()) == 0, "Blocked migration must not return cards");
        helper.succeed();
    }

    private static ServerPlayer playerWithSource(GameTestHelper helper, int count) {
        var player = MyoExperienceGameTests.makeMenuPlayer(helper);
        ListTag entries = new ListTag();
        CompoundTag entry = new ItemStack(card(), count).save(new CompoundTag());
        entry.putInt("Slot", 0);
        entries.add(entry);
        player.getPersistentData().put(SOURCE, entries);
        return player;
    }

    private static ItemStack wut(String uuid, String... sources) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("ae2wtlib", "wireless_universal_terminal"));
        ItemStack stack = terminal(item, uuid);
        ListTag savedSources = new ListTag();
        for (String source : sources) savedSources.add(StringTag.valueOf(source));
        stack.getOrCreateTag().put(SOURCES_TAG, savedSources);
        return stack;
    }

    private static PlayerUpgradeContainer open(ServerPlayer player, ItemStack stack) {
        return new PlayerUpgradeContainer(player, new MyoExperienceGameTests.TestTerminalHost(player, stack));
    }

    private static CompoundTag unknownCard() {
        CompoundTag item = new CompoundTag();
        item.putString("id", "missing:card");
        item.putByte("Count", (byte) 1);
        item.putInt("Slot", 1);
        item.putString("opaque", "preserve this");
        return item;
    }

    private static void fillInventory(Inventory inventory) {
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            inventory.setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
        }
    }

    private static Item card() {
        return MyoItems.MYOTUS_UPGRADE_CARD.get();
    }

    private static ItemStack terminal(Item item, String uuid) {
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTag().putUUID(UUID_TAG, UUID.fromString(uuid));
        return stack;
    }

    private static CompoundTag data(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().copy() : new CompoundTag();
    }
}
