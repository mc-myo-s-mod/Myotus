package me.myogoo.myotus.api.recipe;

import net.minecraft.world.item.crafting.RecipeInput;

public interface IMyotusShapedTableRecipe<I extends RecipeInput> extends IMyotusTableRecipe<I> {
    int width();
    int height();
}
