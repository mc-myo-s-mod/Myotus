package me.myogoo.myotus.menu;

import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.storage.ITerminalHost;
import appeng.parts.AEBasePart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.UUID;

/**
 * Resolves a stable storage key for a terminal host so each terminal can have
 * its own upgrade inventory.
 */
public final class TerminalUpgradeStorageKey {
    private static final String STACK_UUID_TAG = "myotus_terminal_storage_uuid";
    private static final ResourceLocation WUT_ITEM_ID = ResourceLocation.fromNamespaceAndPath("ae2wtlib",
            "wireless_universal_terminal");

    private TerminalUpgradeStorageKey() {
    }

    public static String of(ITerminalHost host) {
        if (host instanceof AEBasePart part) {
            BlockEntity blockEntity = part.getBlockEntity();
            BlockPos pos = blockEntity.getBlockPos();
            String dimension = blockEntity.getLevel() != null
                    ? blockEntity.getLevel().dimension().location().toString()
                    : "unknown";
            return "part:" + host.getClass().getName()
                    + ":" + dimension
                    + ":" + pos.getX() + "," + pos.getY() + "," + pos.getZ()
                    + ":" + part.getSide().getName();
        }

        if (host instanceof ItemMenuHost<?> itemMenuHost) {
            ItemStack stack = itemMenuHost.getItemStack();
            if (isWut(stack)) {
                return toWutStorageKey(getOrCreateStackUuid(stack).toString());
            }

            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            return toItemStorageKey(host, itemId, getOrCreateStackUuid(stack).toString());
        }

        return "host:" + host.getClass().getName();
    }

    private static UUID getOrCreateStackUuid(ItemStack stack) {
        CompoundTag tag = copyCustomData(stack);
        if (tag.hasUUID(STACK_UUID_TAG)) {
            return tag.getUUID(STACK_UUID_TAG);
        }

        UUID uuid = UUID.randomUUID();
        tag.putUUID(STACK_UUID_TAG, uuid);
        setCustomData(stack, tag);
        return uuid;
    }

    private static CompoundTag copyCustomData(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static void setCustomData(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static String toItemStorageKey(ITerminalHost host, ResourceLocation itemId, String storageId) {
        return "item:" + host.getClass().getName() + ":" + itemId + ":" + storageId;
    }

    private static String toWutStorageKey(String storageId) {
        return "item:" + WUT_ITEM_ID + ":" + storageId;
    }

    private static boolean isWut(ItemStack stack) {
        return !stack.isEmpty() && WUT_ITEM_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }
}
