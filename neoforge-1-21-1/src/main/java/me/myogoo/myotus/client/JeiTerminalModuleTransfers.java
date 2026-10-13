package me.myogoo.myotus.client;

import java.util.Optional;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.AESubScreen;
import appeng.menu.me.common.MEStorageMenu;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Adapts both JEI transfer entry points without changing ordinary recipe transfers. */
public final class JeiTerminalModuleTransfers {
    private JeiTerminalModuleTransfers() {}

    public interface ParentScreenAccess {
        Screen myotus$getParentScreen();
    }

    public static void transfer(Screen screen, AbstractContainerMenu menu, IRecipeLayoutDrawable<?> layout,
            boolean maxTransfer, boolean doTransfer, CallbackInfoReturnable<Optional<IRecipeTransferError>> cir) {
        if (!TerminalModuleTransferKey.isDown()) return;
        if (screen instanceof ParentScreenAccess recipes) screen = recipes.myotus$getParentScreen();
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
