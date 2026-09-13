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
    public MyotusRecipeDataProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        CraftingRecipes.build(output, items);
        AE2Recipes.build(output, items);
        AE2CrystalScienceRecipes.buildStonecutting(output, items);
    }

    public static final class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new MyotusRecipeDataProvider(registries, output);
        }

        @Override
        public @NotNull String getName() {
            return "Myotus Recipes";
        }
    }
}
