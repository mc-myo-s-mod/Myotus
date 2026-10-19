package me.myogoo.myotus.mixin.ae2wtlib;

import java.util.ArrayList;

import me.myogoo.myotus.menu.WirelessTerminalUpgradeTransfer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = { "de.mari_023.ae2wtlib.wut.recipe.Combine", "de.mari_023.ae2wtlib.wut.recipe.Upgrade" }, remap = false)
public abstract class WUTRecipeMixin {
    @Inject(method = "assemble(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN"), require = 1)
    private void myotus$preserveUpgradeSources(CraftingContainer input, RegistryAccess registries,
            CallbackInfoReturnable<ItemStack> cir) {
        var inputs = new ArrayList<ItemStack>();
        for (int slot = 0; slot < input.getContainerSize(); slot++) inputs.add(input.getItem(slot));
        WirelessTerminalUpgradeTransfer.prepareResult(cir.getReturnValue(), inputs);
    }
}
