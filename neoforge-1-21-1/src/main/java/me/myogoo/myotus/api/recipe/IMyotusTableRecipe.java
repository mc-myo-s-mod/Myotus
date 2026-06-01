package me.myogoo.myotus.api.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public interface IMyotusTableRecipe<I extends RecipeInput> {
    Recipe<?> recipe();

    default <R extends Recipe<?>> R get() {
        return (R) recipe();
    }

    default <R extends Recipe<?>> Optional<R> unwrap(Class<R> recipeClass) {
        var recipe = recipe();
        if (recipeClass.isInstance(recipe)) {
            return Optional.of(recipeClass.cast(recipe));
        }
        return Optional.empty();
    }

    default ResourceLocation recipeId() {
        return null;
    }

    default ResourceLocation tableType() {
        return BuiltInRegistries.RECIPE_TYPE.getKey(recipe().getType());
    }

    int tier();

    default int sideLength() {
        return tier() * 2 + 1;
    }

    default int gridSize() {
        return sideLength() * sideLength();
    }

    default NonNullList<Ingredient> slotIngredients() {
        return recipe().getIngredients();
    }

    default NonNullList<Ingredient> ensureFittedCraftingGrid() {
        return slotIngredients();
    }

    default I createInput(List<ItemStack> items) {
        throw new UnsupportedOperationException("This table recipe does not expose an input factory");
    }

    default boolean matches(List<ItemStack> items, Level level) {
        return typedRecipe().matches(createInput(items), level);
    }

    default ItemStack assemble(List<ItemStack> items, Level level) {
        return typedRecipe().assemble(createInput(items), level.registryAccess());
    }

    default NonNullList<ItemStack> getRemainingItems(List<ItemStack> items) {
        return typedRecipe().getRemainingItems(createInput(items));
    }

    @SuppressWarnings("unchecked")
    private Recipe<I> typedRecipe() {
        return (Recipe<I>) recipe();
    }

}
