package me.myogoo.myotus.mixin.emi;

import java.util.List;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.AESubScreen;
import appeng.menu.me.common.MEStorageMenu;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.widget.RecipeButtonWidget;
import me.myogoo.myotus.client.TerminalModuleRecipeTransfers;
import me.myogoo.myotus.client.TerminalModuleTransferKey;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The normal EMI handler and its cached canFill state remain untouched when the modifier is released. */
@Pseudo
@Mixin(targets = "dev.emi.emi.api.widget.RecipeFillButtonWidget", remap = false)
public abstract class RecipeFillButtonWidgetMixin extends RecipeButtonWidget {
    @Unique private TerminalModuleRecipeTransfers.Result myotus$preview;
    @Unique private AEBaseScreen<?> myotus$previewScreen;
    @Unique private long myotus$previewAt;

    protected RecipeFillButtonWidgetMixin(int x, int y, int u, int v, EmiRecipe recipe) {
        super(x, y, u, v, recipe);
    }

    @Unique
    private @Nullable AEBaseScreen<?> myotus$targetScreen() {
        if (!TerminalModuleTransferKey.isDown()) return null;
        var screen = EmiApi.getHandledScreen();
        return screen instanceof AEBaseScreen<?> terminal && !(screen instanceof AESubScreen<?, ?>)
                && screen.getMenu() instanceof MEStorageMenu ? terminal : null;
    }

    @Unique
    private TerminalModuleRecipeTransfers.Result myotus$preview(AEBaseScreen<?> screen) {
        long now = Util.getMillis();
        // A target may inspect the entire ME inventory. Preview once per tick, not for every draw/tooltip.
        if (myotus$preview == null || myotus$previewScreen != screen || now - myotus$previewAt >= 50) {
            myotus$preview = TerminalModuleRecipeTransfers.route(screen, recipe.getBackingRecipe(), false, false);
            myotus$previewScreen = screen;
            myotus$previewAt = now;
        }
        return myotus$preview;
    }

    @Inject(method = "getTextureOffset", at = @At("HEAD"), cancellable = true)
    private void myotus$targetAppearance(int mouseX, int mouseY, CallbackInfoReturnable<Integer> cir) {
        var screen = myotus$targetScreen();
        if (screen != null) cir.setReturnValue(myotus$preview(screen).canTransfer()
                ? super.getTextureOffset(mouseX, mouseY) : 24);
    }

    @Inject(method = "getTooltip", at = @At("HEAD"), cancellable = true)
    private void myotus$targetTooltip(int mouseX, int mouseY,
            CallbackInfoReturnable<List<ClientTooltipComponent>> cir) {
        var screen = myotus$targetScreen();
        if (screen != null) cir.setReturnValue(List.of(ClientTooltipComponent.create(
                myotus$preview(screen).tooltip().getVisualOrderText())));
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void myotus$targetClick(int mouseX, int mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        var screen = myotus$targetScreen();
        if (screen == null) return;
        // Revalidate on every click; a preview is never authorization to send a request.
        var result = TerminalModuleRecipeTransfers.route(screen, recipe.getBackingRecipe(), Screen.hasShiftDown(), true);
        if (result.sent()) {
            playButtonSound();
            Minecraft.getInstance().setScreen(screen);
        }
        cir.setReturnValue(true);
    }
}
