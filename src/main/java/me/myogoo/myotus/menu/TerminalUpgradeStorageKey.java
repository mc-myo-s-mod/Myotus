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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves a stable storage key for a terminal host so each terminal can have
 * its own upgrade inventory.
 */
public final class TerminalUpgradeStorageKey {
    private static final String STACK_UUID_TAG = "myotus_terminal_storage_uuid";

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
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(itemMenuHost.getItem());
            return "item:" + host.getClass().getName()
                    + ":" + itemId
                    + ":" + getOrCreateStackUuid(itemMenuHost.getItemStack());
        }

        return "host:" + host.getClass().getName();
    }

    public static List<String> legacyKeysOf(ITerminalHost host) {
        if (host instanceof ItemMenuHost<?> itemMenuHost) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(itemMenuHost.getItem());
            List<String> legacyKeys = new ArrayList<>();
            Integer slot = itemMenuHost.getPlayerInventorySlot();
            if (slot != null) {
                legacyKeys.add("item:" + host.getClass().getName() + ":" + itemId + ":" + slot);
            }
            legacyKeys.add("item:" + host.getClass().getName() + ":" + itemId + ":" + describeLegacyItemStack(itemMenuHost.getItemStack()));
            return legacyKeys;
        }
        return List.of();
    }

    private static UUID getOrCreateStackUuid(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.hasUUID(STACK_UUID_TAG)) {
            return tag.getUUID(STACK_UUID_TAG);
        }

        UUID uuid = UUID.randomUUID();
        tag.putUUID(STACK_UUID_TAG, uuid);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return uuid;
    }

    private static UUID describeLegacyItemStack(ItemStack stack) {
        return UUID.nameUUIDFromBytes(stack.toString().getBytes(StandardCharsets.UTF_8));
    }
}
