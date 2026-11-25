package me.myogoo.myotus.mixin.inventorysorter;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(targets = "cpw.mods.inventorysorter.ContainerContext", remap = false)
public interface InventorySorterContextAccessor {
    @Accessor(value = "player", remap = false)
    ServerPlayer myotus$getPlayer();
}
