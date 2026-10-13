package me.myogoo.myotus.mixin.jei;

import me.myogoo.myotus.client.JeiTerminalModuleTransfers;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(targets = "mezz.jei.gui.recipes.RecipesGui", remap = false)
public abstract class RecipesGuiMixin implements JeiTerminalModuleTransfers.ParentScreenAccess {
    @Shadow private Screen parentScreen;

    @Override
    public Screen myotus$getParentScreen() {
        return parentScreen;
    }
}
