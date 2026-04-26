package me.myogoo.myotus.menu;

import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.storage.ITerminalHost;
import appeng.parts.AEBasePart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Resolves a stable storage key for a terminal host so each terminal can have
 * its own upgrade inventory.
 */
public final class TerminalUpgradeStorageKey {
    private static final String STACK_UUID_TAG = "myotus_terminal_storage_uuid";
    private static final String MERGED_TERMINAL_STORAGE_TAG = "myotus_merged_terminal_storage";
    private static final String MERGED_TERMINAL_ITEM_TAG = "item";
    private static final String MERGED_TERMINAL_UUID_TAG = "uuid";
    private static final String CURRENT_TERMINAL_TAG = "currentTerminal";
    private static final ResourceLocation WUT_ITEM_ID = new ResourceLocation("ae2wtlib",
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

        if (host instanceof ItemMenuHost itemMenuHost) {
            ItemStack stack = itemMenuHost.getItemStack();
            String mergedTerminalStorageKey = getMergedTerminalStorageKey(host, stack);
            if (mergedTerminalStorageKey != null) {
                return mergedTerminalStorageKey;
            }

            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            return toItemStorageKey(host, itemId, getOrCreateStackUuid(stack).toString());
        }

        return "host:" + host.getClass().getName();
    }

    @Nullable
    public static UUID getStackUuid(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.hasUUID(STACK_UUID_TAG)
                ? tag.getUUID(STACK_UUID_TAG)
                : null;
    }

    public static void rememberMergedTerminalStorage(ItemStack result, ItemStack mergedTerminal,
            String terminalName, @Nullable UUID originalWutStorageUuid) {
        if (result.isEmpty()) {
            return;
        }

        UUID mergedTerminalUuid = getStackUuid(mergedTerminal);
        CompoundTag resultTag = result.getOrCreateTag();
        if (mergedTerminalUuid != null) {
            CompoundTag mergedTerminals = resultTag.getCompound(MERGED_TERMINAL_STORAGE_TAG);
            CompoundTag mergedTerminalInfo = new CompoundTag();
            mergedTerminalInfo.putString(MERGED_TERMINAL_ITEM_TAG,
                    BuiltInRegistries.ITEM.getKey(mergedTerminal.getItem()).toString());
            mergedTerminalInfo.putUUID(MERGED_TERMINAL_UUID_TAG, mergedTerminalUuid);
            mergedTerminals.put(terminalName, mergedTerminalInfo);
            resultTag.put(MERGED_TERMINAL_STORAGE_TAG, mergedTerminals);
        }

        if (originalWutStorageUuid != null) {
            resultTag.putUUID(STACK_UUID_TAG, originalWutStorageUuid);
        }
    }

    private static UUID getOrCreateStackUuid(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.hasUUID(STACK_UUID_TAG)) {
            return tag.getUUID(STACK_UUID_TAG);
        }

        UUID uuid = UUID.randomUUID();
        tag.putUUID(STACK_UUID_TAG, uuid);
        return uuid;
    }

    @Nullable
    private static String getMergedTerminalStorageKey(ITerminalHost host, ItemStack stack) {
        if (stack.isEmpty() || !WUT_ITEM_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()))) {
            return null;
        }

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(CURRENT_TERMINAL_TAG, CompoundTag.TAG_STRING)) {
            return null;
        }

        CompoundTag mergedTerminals = tag.getCompound(MERGED_TERMINAL_STORAGE_TAG);
        String currentTerminal = tag.getString(CURRENT_TERMINAL_TAG);
        if (!mergedTerminals.contains(currentTerminal, CompoundTag.TAG_COMPOUND)) {
            return null;
        }

        CompoundTag mergedTerminalInfo = mergedTerminals.getCompound(currentTerminal);
        if (!mergedTerminalInfo.hasUUID(MERGED_TERMINAL_UUID_TAG)) {
            return null;
        }

        ResourceLocation mergedItemId = ResourceLocation.tryParse(
                mergedTerminalInfo.getString(MERGED_TERMINAL_ITEM_TAG));
        if (mergedItemId == null) {
            return null;
        }

        return toItemStorageKey(host, mergedItemId,
                mergedTerminalInfo.getUUID(MERGED_TERMINAL_UUID_TAG).toString());
    }

    private static String toItemStorageKey(ITerminalHost host, ResourceLocation itemId, String storageId) {
        return "item:" + host.getClass().getName() + ":" + itemId + ":" + storageId;
    }
}
