package me.myogoo.myotus.mixin.jei;

import java.util.Optional;

import me.myogoo.myotus.client.JeiTerminalModuleTransfers;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "mezz.jei.common.transfer.RecipeTransferService", remap = false)
public abstract class RecipeTransferServiceMixin {
    @Inject(method = "transferRecipe(Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;Lmezz/jei/api/gui/IRecipeLayoutDrawable;Lnet/minecraft/world/entity/player/Player;ZZ)Ljava/util/Optional;",
            at = @At("HEAD"), cancellable = true, require = 1)
    private void myotus$transferToModule(AbstractContainerScreen<?> screen, IRecipeLayoutDrawable<?> layout,
            Player player, boolean maxTransfer, boolean doTransfer,
            CallbackInfoReturnable<Optional<IRecipeTransferError>> cir) {
        JeiTerminalModuleTransfers.transfer(screen, screen.getMenu(), layout, maxTransfer, doTransfer, cir);
    }
}
