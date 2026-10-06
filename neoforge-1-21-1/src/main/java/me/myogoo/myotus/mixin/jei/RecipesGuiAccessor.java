package me.myogoo.myotus.mixin.jei;

import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(targets = "mezz.jei.gui.recipes.RecipesGui", remap = false)
public interface RecipesGuiAccessor {
    @Accessor("parentScreen")
    Screen myotus$getParentScreen();
}
