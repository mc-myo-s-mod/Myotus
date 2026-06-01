package me.myogoo.myotus.api.recipe;

import net.minecraft.world.Container;

public interface IMyotusShapedTableRecipe<I extends Container> extends IMyotusTableRecipe<I> {
    int width();

    int height();
}
