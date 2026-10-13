package me.myogoo.myotus.client;

import java.util.Optional;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.AESubScreen;
import appeng.menu.me.common.MEStorageMenu;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.junit.jupiter.api.Test;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JeiTerminalModuleTransfersTest {
    @Test
    void ordinaryTransfersWrongMenusAndSubscreensKeepJeiInControl() {
        var menu = mock(MEStorageMenu.class);
        var screen = mock(AEBaseScreen.class);
        var layout = mock(IRecipeLayoutDrawable.class);
        when(screen.getMenu()).thenReturn(menu);
        try (var key = mockStatic(TerminalModuleTransferKey.class);
                var router = mockStatic(TerminalModuleRecipeTransfers.class)) {
            var callback = callback();
            JeiTerminalModuleTransfers.transfer(screen, menu, layout, false, true, callback);
            assertFalse(callback.isCancelled());
            key.when(TerminalModuleTransferKey::isDown).thenReturn(true);
            JeiTerminalModuleTransfers.transfer(screen, mock(MEStorageMenu.class), layout, false, true, callback);
            JeiTerminalModuleTransfers.transfer(mock(AESubScreen.class), menu, layout, false, true, callback);
            assertFalse(callback.isCancelled());
            router.verifyNoInteractions();
        }
    }

    @Test
    void previewClickAndFailureRetainFlagsAndJeiErrorSemantics() {
        var menu = mock(MEStorageMenu.class);
        var screen = mock(AEBaseScreen.class);
        var layout = mock(IRecipeLayoutDrawable.class);
        var recipe = mock(RecipeHolder.class);
        var card = mock(Item.class);
        when(screen.getMenu()).thenReturn(menu);
        when(layout.getRecipe()).thenReturn(recipe);
        try (var key = mockStatic(TerminalModuleTransferKey.class);
                var router = mockStatic(TerminalModuleRecipeTransfers.class)) {
            key.when(TerminalModuleTransferKey::isDown).thenReturn(true);
            router.when(() -> TerminalModuleRecipeTransfers.route(screen, recipe, true, false))
                    .thenReturn(new TerminalModuleRecipeTransfers.Result(card, null, false));
            var preview = callback();
            var recipeScreen = mock(Screen.class,
                    withSettings().extraInterfaces(JeiTerminalModuleTransfers.ParentScreenAccess.class));
            when(((JeiTerminalModuleTransfers.ParentScreenAccess) recipeScreen).myotus$getParentScreen()).thenReturn(screen);
            JeiTerminalModuleTransfers.transfer(recipeScreen, menu, layout, true, false, preview);
            assertTrue(preview.isCancelled());
            assertEquals(IRecipeTransferError.Type.COSMETIC, preview.getReturnValue().orElseThrow().getType());

            router.when(() -> TerminalModuleRecipeTransfers.route(screen, recipe, true, true))
                    .thenReturn(new TerminalModuleRecipeTransfers.Result(card, null, true));
            var click = callback();
            JeiTerminalModuleTransfers.transfer(screen, menu, layout, true, true, click);
            assertTrue(click.isCancelled());
            assertTrue(click.getReturnValue().isEmpty());

            var error = Component.literal("Unavailable module");
            router.when(() -> TerminalModuleRecipeTransfers.route(screen, recipe, true, true))
                    .thenReturn(new TerminalModuleRecipeTransfers.Result(card, error, false));
            var failed = callback();
            JeiTerminalModuleTransfers.transfer(screen, menu, layout, true, true, failed);
            var transferError = failed.getReturnValue().orElseThrow();
            assertEquals(IRecipeTransferError.Type.USER_FACING, transferError.getType());
            var tooltip = mock(ITooltipBuilder.class);
            transferError.getTooltip(tooltip);
            verify(tooltip).add(error);
        }
    }

    private static CallbackInfoReturnable<Optional<IRecipeTransferError>> callback() {
        return new CallbackInfoReturnable<>("jeiTransfer", true);
    }

}
