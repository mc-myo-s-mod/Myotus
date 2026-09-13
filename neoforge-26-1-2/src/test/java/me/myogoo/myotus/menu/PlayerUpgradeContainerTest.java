package me.myogoo.myotus.menu;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerUpgradeContainerTest {
    @Test
    void preservesUnopenedTerminalsWithoutSharingOrCopyingOtherModData() {
        var keys = List.of(
                "item:ae2wtlib:wireless_universal_terminal:12345678-abcd-1234-abcd-123456789012",
                "item:example.WirelessHost:example:wireless_terminal:87654321-abcd-1234-abcd-123456789012",
                "host:example.TerminalHost",
                "part:example.TerminalPart:minecraft:overworld:-3,64,8:north",
                "part:example.TerminalPart:unknown:3,-64,8:up");
        CompoundTag original = new CompoundTag();
        CompoundTag target = new CompoundTag();
        for (String key : keys) {
            original.put(key, inventory(4));
        }
        original.put("othermod:inventory", inventory(0));
        original.putString("othermod:state", "old");
        target.putString("othermod:state", "keep");
        CompoundTag persisted = new CompoundTag();
        persisted.putString("othermod:state", "persisted");
        target.put("PlayerPersisted", persisted);

        PlayerUpgradeContainer.copyUpgrades(original, target);

        for (String key : keys) {
            assertEquals(original.get(key), target.get(key));
            assertNotSame(original.get(key), target.get(key));
            CompoundTag copiedItem = (CompoundTag) ((ListTag) target.get(key)).get(0);
            CompoundTag originalItem = (CompoundTag) ((ListTag) original.get(key)).get(0);
            assertNotSame(originalItem, copiedItem);
            copiedItem.putString("changed", "only the new player");
            assertFalse(originalItem.contains("changed"));
        }
        assertFalse(target.contains("othermod:inventory"));
        assertEquals(StringTag.valueOf("keep"), target.get("othermod:state"));
        assertEquals(persisted, target.get("PlayerPersisted"));
    }

    @Test
    void rejectsUnrelatedKeysAndNonInventoryPayloads() {
        CompoundTag original = new CompoundTag();
        original.put("item:unrelated:data", inventory(0));
        original.put("host:example.NotAList", new CompoundTag());
        original.put("host:example.NegativeSlot", inventory(-1));
        original.put("host:example.OutOfRangeSlot", inventory(PlayerUpgradeContainer.SIZE));
        ListTag missingSlot = inventory(0);
        ((CompoundTag) missingSlot.get(0)).remove("Slot");
        original.put("host:example.MissingSlot", missingSlot);
        ListTag missingItem = inventory(0);
        ((CompoundTag) missingItem.get(0)).remove("id");
        original.put("host:example.MissingItem", missingItem);
        ListTag oversized = inventory(0);
        for (int i = 0; i < PlayerUpgradeContainer.SIZE; i++) {
            oversized.add(((CompoundTag) oversized.get(0)).copy());
        }
        original.put("host:example.Oversized", oversized);
        CompoundTag target = new CompoundTag();

        PlayerUpgradeContainer.copyUpgrades(original, target);

        assertTrue(target.isEmpty());
    }

    @Test
    void preservesEmptySerializedInventory() {
        CompoundTag original = new CompoundTag();
        original.put("host:example.EmptyTerminal", new ListTag());
        CompoundTag target = new CompoundTag();

        PlayerUpgradeContainer.copyUpgrades(original, target);

        assertEquals(original, target);
    }

    private static ListTag inventory(int slot) {
        CompoundTag item = new CompoundTag();
        item.putInt("Slot", slot);
        item.putString("id", "missing_addon:terminal_upgrade");
        item.putInt("count", 1);
        ListTag inventory = new ListTag();
        inventory.add(item);
        return inventory;
    }
}
