package me.myogoo.myotus.mixin.jei;

import java.util.Optional;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.AESubScreen;
import appeng.menu.me.common.MEStorageMenu;
import me.myogoo.myotus.client.TerminalModuleRecipeTransfers;
import me.myogoo.myotus.client.TerminalModuleTransferKey;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Route both JEI's dry run and click before any native/category-specific transfer handler. */
@Pseudo
@Mixin(targets = "mezz.jei.common.transfer.RecipeTransferUtil", remap = false)
public abstract class RecipeTransferUtilMixin {
    @Inject(method = "transferRecipe(Lmezz/jei/api/recipe/transfer/IRecipeTransferManager;Lnet/minecraft/world/inventory/AbstractContainerMenu;Lmezz/jei/api/gui/IRecipeLayoutDrawable;Lnet/minecraft/world/entity/player/Player;ZZ)Ljava/util/Optional;",
            at = @At("HEAD"), cancellable = true)
    private static void myotus$transferToModule(IRecipeTransferManager manager, AbstractContainerMenu menu,
            IRecipeLayoutDrawable<?> layout, Player player, boolean maxTransfer, boolean doTransfer,
            CallbackInfoReturnable<Optional<IRecipeTransferError>> cir) {
        if (!TerminalModuleTransferKey.isDown()) return;
        var screen = Minecraft.getInstance().screen;
        if (screen instanceof RecipesGuiAccessor recipes) screen = recipes.myotus$getParentScreen();
        if (!(screen instanceof AEBaseScreen<?> terminal) || screen instanceof AESubScreen<?, ?>
                || !(menu instanceof MEStorageMenu) || terminal.getMenu() != menu) return;
        var recipe = layout.getRecipe() instanceof RecipeHolder<?> holder ? holder : null;
        var result = TerminalModuleRecipeTransfers.route(terminal, recipe, maxTransfer, doTransfer);
        if (doTransfer && result.sent()) {
            cir.setReturnValue(Optional.empty());
        } else {
            cir.setReturnValue(Optional.of(new IRecipeTransferError() {
                @Override
                public Type getType() { return result.canTransfer() ? Type.COSMETIC : Type.USER_FACING; }

                @Override
                public void getTooltip(ITooltipBuilder tooltip) { tooltip.add(result.tooltip()); }
            }));
        }
    }
}
