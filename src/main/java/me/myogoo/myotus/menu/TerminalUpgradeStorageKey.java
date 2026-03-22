package me.myogoo.myotus.menu;

import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.storage.ITerminalHost;
import appeng.parts.AEBasePart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Resolves a stable storage key for a terminal host so each terminal can have
 * its own upgrade inventory.
 */
public final class TerminalUpgradeStorageKey {
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
            Integer slot = itemMenuHost.getPlayerInventorySlot();
            return "item:" + host.getClass().getName()
                    + ":" + itemId
                    + ":" + (slot != null ? slot : "floating");
        }

        return "host:" + host.getClass().getName();
    }
}
