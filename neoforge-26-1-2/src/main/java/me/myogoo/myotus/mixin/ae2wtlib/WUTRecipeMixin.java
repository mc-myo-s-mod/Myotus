package me.myogoo.myotus.mixin.ae2wtlib;

import me.myogoo.myotus.menu.WirelessTerminalUpgradeTransfer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = { "de.mari_023.ae2wtlib.wut.recipe.Combine", "de.mari_023.ae2wtlib.wut.recipe.Upgrade" }, remap = false)
public abstract class WUTRecipeMixin {
    @Inject(method = "assemble(Lnet/minecraft/world/item/crafting/CraftingInput;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN"), require = 1)
    private void myotus$preserveUpgradeSources(CraftingInput input, CallbackInfoReturnable<ItemStack> cir) {
        WirelessTerminalUpgradeTransfer.prepareResult(cir.getReturnValue(), input.items());
    }
}
