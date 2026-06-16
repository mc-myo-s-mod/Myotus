package me.myogoo.myotus.data;

import me.myogoo.myotus.data.recipe.ae2.AE2Recipes;
import me.myogoo.myotus.data.recipe.crafting.CraftingRecipes;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class MyotusRecipeDataProvider extends RecipeProvider {
    public MyotusRecipeDataProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> consumer) {
        CraftingRecipes.build(consumer);
        AE2Recipes.build(consumer);
    }
}
