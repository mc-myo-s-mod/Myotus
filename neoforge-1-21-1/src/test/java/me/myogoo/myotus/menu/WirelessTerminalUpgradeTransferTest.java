package me.myogoo.myotus.menu;

import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.PlayerInternalInventory;
import me.myogoo.myotus.api.ITerminalCard;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class WirelessTerminalUpgradeTransferTest {
    private static final String FIRST = "12345678-abcd-1234-abcd-123456789012";
    private static final String SECOND = "87654321-abcd-1234-abcd-123456789012";
    private static final String SOURCE = "item:example.WirelessHost:example:terminal:" + FIRST;
    private static final String DESTINATION = "item:ae2wtlib:wireless_universal_terminal:" + SECOND;
    private static final Set<String> SOURCES = Set.of("example:terminal:" + FIRST);

    @Test
    void previewsAreDeterministicAndNeverChangeInputs() {
        ItemStack input = terminal(Items.APPLE, FIRST);
        ItemStack second = terminal(Items.BOOK, SECOND);
        ItemStack unopened = new ItemStack(Items.STICK);
        CompoundTag before = data(input);
        ItemStack result = new ItemStack(Items.DIAMOND);
        CompoundTag resultData = new CompoundTag();
        resultData.putString("othermod", "keep");
        result.set(DataComponents.CUSTOM_DATA, CustomData.of(resultData));
        WirelessTerminalUpgradeTransfer.prepareResult(result, List.of(input, second, unopened));
        ItemStack preview = new ItemStack(Items.DIAMOND);
        preview.set(DataComponents.CUSTOM_DATA, CustomData.of(resultData));
        WirelessTerminalUpgradeTransfer.prepareResult(preview, List.of(second, input, input, unopened));
        assertEquals(data(result), data(preview));
        assertEquals(before, data(input));
        assertFalse(data(unopened).contains(TerminalUpgradeStorageKey.STACK_UUID_TAG));
        assertEquals("keep", data(result).getString("othermod"));
        assertNotEquals(UUID.fromString(FIRST), data(result).getUUID(TerminalUpgradeStorageKey.STACK_UUID_TAG));
    }

    @Test
    void subsequentCraftRetainsAncestorsWithoutAddingMalformedSources() {
        ItemStack input = terminal(Items.APPLE, FIRST);
        CompoundTag inputData = data(input);
        ListTag ancestors = new ListTag();
        ancestors.add(StringTag.valueOf("example:old_terminal:" + SECOND));
        ancestors.add(StringTag.valueOf("not a storage identity"));
        inputData.put(WirelessTerminalUpgradeTransfer.SOURCES_TAG, ancestors);
        input.set(DataComponents.CUSTOM_DATA, CustomData.of(inputData));
        ItemStack result = new ItemStack(Items.DIAMOND);
        WirelessTerminalUpgradeTransfer.prepareResult(result, List.of(input));
        ListTag saved = data(result).getList(WirelessTerminalUpgradeTransfer.SOURCES_TAG, Tag.TAG_STRING);
        assertEquals(2, saved.size());
        assertTrue(saved.contains(StringTag.valueOf("example:old_terminal:" + SECOND)));
    }

    @Test
    void duplicateIsReturnedAndConsumedSourceCannotBeImportedAgain() {
        Item card = card();
        CompoundTag persistent = sourceItems(2);
        var destination = new AppEngInternalInventory(null, 27, 1, TerminalUpgradeSlotFilter.INSTANCE);
        AtomicInteger returned = new AtomicInteger();
        AtomicInteger saves = new AtomicInteger();
        Function<ItemStack, ItemStack> giveBack = stack -> {
            returned.addAndGet(stack.getCount());
            return ItemStack.EMPTY;
        };
        assertFalse(transfer(persistent, destination, tag -> stack(card, tag.getInt("count")), giveBack,
                saves::incrementAndGet));
        assertEquals(1, destination.getStackInSlot(0).getCount());
        assertEquals(1, returned.get());
        assertFalse(persistent.contains(SOURCE));
        assertFalse(transfer(persistent, destination, tag -> stack(card, 2), giveBack, saves::incrementAndGet));
        assertEquals(1, returned.get());
        assertEquals(1, saves.get());
    }

    @Test
    void fullInventoryRetainsExactRemainderAndUnknownItemBytesAcrossClone() {
        Item card = card();
        CompoundTag persistent = sourceItems(3);
        ListTag entries = persistent.getList(SOURCE, Tag.TAG_COMPOUND);
        CompoundTag unknown = item("missing:card", 1, 1);
        unknown.putString("opaque", "preserve this");
        entries.add(unknown);
        var destination = new AppEngInternalInventory(null, 27, 1, TerminalUpgradeSlotFilter.INSTANCE);
        Function<CompoundTag, ItemStack> decode = tag -> tag.getString("id").equals("missing:card")
                ? ItemStack.EMPTY : stack(card, tag.getInt("count"));
        assertTrue(transfer(persistent, destination, decode, Function.identity(), () -> {}));
        ListTag remaining = persistent.getList(SOURCE, Tag.TAG_COMPOUND);
        assertEquals(2, remaining.getCompound(0).getInt("count"));
        assertEquals(unknown, remaining.getCompound(1));
        assertTrue(destination.getStackInSlot(1).isEmpty());
        CompoundTag cloned = new CompoundTag();
        PlayerUpgradeContainer.copyUpgrades(persistent, cloned);
        assertEquals(persistent, cloned);
        AtomicInteger returned = new AtomicInteger();
        assertTrue(transfer(cloned, destination, decode, stack -> {
            returned.addAndGet(stack.getCount());
            return ItemStack.EMPTY;
        }, () -> {}));
        assertEquals(2, returned.get());
        assertEquals(unknown, cloned.getList(SOURCE, Tag.TAG_COMPOUND).getCompound(0));
    }

    @Test
    void onlyVisibleSlotsAreFilledAndDestinationIsNeverDeleted() {
        CompoundTag persistent = sourceItems(1);
        persistent.put(DESTINATION, persistent.get(SOURCE).copy());
        CompoundTag before = persistent.copy();
        var destination = new AppEngInternalInventory(null, 27, 1, TerminalUpgradeSlotFilter.INSTANCE);
        assertTrue(WirelessTerminalUpgradeTransfer.transfer(persistent, DESTINATION, SOURCES, SECOND,
                destination, 0, tag -> stack(card(), 1), Function.identity(), () -> fail("Nothing moved")));
        assertEquals(before, persistent);
        assertTrue(destination.isEmpty());
    }

    @Test
    void currentUuidRecoversLegacyKeyButOtherPlayersAndOtherUuidsStayUntouched() {
        CompoundTag persistent = sourceItems(1);
        persistent.put("item:example.WirelessHost:example:terminal:" + SECOND,
                persistent.get(SOURCE).copy());
        CompoundTag otherPlayer = persistent.copy();
        var destination = new AppEngInternalInventory(null, 27, 1, TerminalUpgradeSlotFilter.INSTANCE);
        assertFalse(WirelessTerminalUpgradeTransfer.transfer(persistent, DESTINATION, Set.of(), FIRST,
                destination, 5, tag -> stack(card(), 1), Function.identity(), () -> {}));
        assertFalse(persistent.contains(SOURCE));
        assertEquals(2, otherPlayer.size());
        assertEquals(1, persistent.size());
    }

    @Test
    void fullMainInventoryReturnsRemainderWithoutDroppingOrDiscardingIt() {
        Inventory inventory = mock(Inventory.class);
        when(inventory.getItem(anyInt())).thenReturn(new ItemStack(Items.COBBLESTONE, 64));
        ItemStack remainder = new PlayerInternalInventory(inventory).addItems(new ItemStack(Items.APPLE, 2));
        assertEquals(2, remainder.getCount());
        verify(inventory, never()).setItem(anyInt(), any());
    }

    @Test
    void upgradingAnUnopenedLegacyWutStillFindsItsOriginalGenericKey() {
        CompoundTag persistent = sourceItems(1);
        var destination = new AppEngInternalInventory(null, 27, 1, TerminalUpgradeSlotFilter.INSTANCE);
        assertFalse(WirelessTerminalUpgradeTransfer.transfer(persistent, DESTINATION,
                Set.of("ae2wtlib:wireless_universal_terminal:" + FIRST), SECOND, destination,
                5, tag -> stack(card(), 1), Function.identity(), () -> {}));
        assertFalse(persistent.contains(SOURCE));
        assertEquals(1, destination.getStackInSlot(0).getCount());
    }

    @Test
    void undecodableDestinationCannotBeOverwrittenByMigration() {
        CompoundTag persistent = sourceItems(1);
        ListTag missing = new ListTag();
        missing.add(item("missing:card", 1, 0));
        persistent.put(DESTINATION, missing);
        CompoundTag before = persistent.copy();
        var destination = new AppEngInternalInventory(null, 27, 1, TerminalUpgradeSlotFilter.INSTANCE);
        assertTrue(transfer(persistent, destination,
                tag -> tag.getString("id").equals("missing:card") ? ItemStack.EMPTY : stack(card(), 1),
                stack -> { fail("Must not return anything"); return stack; }, () -> fail("Must not save")));
        assertEquals(before, persistent);
        assertTrue(destination.isEmpty());
    }

    private static boolean transfer(CompoundTag data, AppEngInternalInventory destination,
            Function<CompoundTag, ItemStack> decode, Function<ItemStack, ItemStack> returnToPlayer, Runnable save) {
        return WirelessTerminalUpgradeTransfer.transfer(data, DESTINATION, SOURCES, SECOND, destination,
                5, decode, returnToPlayer, save);
    }

    private static CompoundTag sourceItems(int count) {
        CompoundTag data = new CompoundTag();
        ListTag entries = new ListTag();
        entries.add(item("example:card", count, 0));
        data.put(SOURCE, entries);
        return data;
    }

    private static CompoundTag item(String id, int count, int slot) {
        CompoundTag item = new CompoundTag();
        item.putString("id", id);
        item.putInt("count", count);
        item.putInt("Slot", slot);
        return item;
    }

    private static Item card() {
        Item item = mock(Item.class, withSettings().extraInterfaces(ITerminalCard.class));
        when(((ITerminalCard) item).isAvailable()).thenReturn(true);
        return item;
    }

    private static ItemStack stack(Item item, int initialCount) {
        ItemStack stack = mock(ItemStack.class);
        AtomicInteger count = new AtomicInteger(initialCount);
        when(stack.getItem()).thenReturn(item);
        when(stack.is(item)).thenReturn(true);
        when(stack.getCount()).thenAnswer(call -> count.get());
        when(stack.isEmpty()).thenAnswer(call -> count.get() == 0);
        when(stack.getMaxStackSize()).thenReturn(64);
        when(stack.copy()).thenAnswer(call -> stack(item, count.get()));
        doAnswer(call -> { count.set(call.getArgument(0)); return null; }).when(stack).setCount(anyInt());
        doAnswer(call -> { count.addAndGet(-((int) call.getArgument(0))); return null; }).when(stack).shrink(anyInt());
        return stack;
    }

    private static ItemStack terminal(Item item, String uuid) {
        ItemStack stack = new ItemStack(item);
        CompoundTag data = new CompoundTag();
        data.putUUID(TerminalUpgradeStorageKey.STACK_UUID_TAG, UUID.fromString(uuid));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        return stack;
    }

    private static CompoundTag data(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }
}
