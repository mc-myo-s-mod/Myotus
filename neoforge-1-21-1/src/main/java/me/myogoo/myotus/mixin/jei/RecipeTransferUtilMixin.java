package me.myogoo.myotus.mixin.jei;

import java.util.Optional;

import me.myogoo.myotus.client.JeiTerminalModuleTransfers;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Route both JEI's dry run and click before any native/category-specific transfer handler. */
@Pseudo
@Mixin(targets = "mezz.jei.common.transfer.RecipeTransferUtil", remap = false)
public abstract class RecipeTransferUtilMixin {
    // Newer JEI moves this method to RecipeTransferService, whose mixin requires its target.
    @Inject(method = "transferRecipe(Lmezz/jei/api/recipe/transfer/IRecipeTransferManager;Lnet/minecraft/world/inventory/AbstractContainerMenu;Lmezz/jei/api/gui/IRecipeLayoutDrawable;Lnet/minecraft/world/entity/player/Player;ZZ)Ljava/util/Optional;",
            at = @At("HEAD"), cancellable = true, require = 0)
    private static void myotus$transferToModule(IRecipeTransferManager manager, AbstractContainerMenu menu,
            IRecipeLayoutDrawable<?> layout, Player player, boolean maxTransfer, boolean doTransfer,
            CallbackInfoReturnable<Optional<IRecipeTransferError>> cir) {
        JeiTerminalModuleTransfers.transfer(Minecraft.getInstance().screen, menu, layout, maxTransfer, doTransfer, cir);
    }
}
