package me.myogoo.myotus.data;

import me.myogoo.myotus.data.recipe.ae2.AE2Recipes;
import me.myogoo.myotus.data.recipe.ae2cs.AE2CrystalScienceRecipes;
import me.myogoo.myotus.data.recipe.crafting.CraftingRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class MyotusRecipeDataProvider extends RecipeProvider {
    public MyotusRecipeDataProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput output) {
        CraftingRecipes.build(output);
        AE2Recipes.build(output);
        AE2CrystalScienceRecipes.buildStonecutting(output);
    }
}
