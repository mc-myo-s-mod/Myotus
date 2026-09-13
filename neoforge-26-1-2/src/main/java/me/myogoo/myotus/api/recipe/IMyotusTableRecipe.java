package me.myogoo.myotus.api.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.List;
import java.util.Optional;

public interface IMyotusTableRecipe<I extends RecipeInput> {
    Recipe<?> recipe();

    default <R extends Recipe<?>> Optional<R> unwrap(Class<R> recipeClass) {
        var recipe = recipe();
        if (recipeClass.isInstance(recipe)) {
            return Optional.of(recipeClass.cast(recipe));
        }
        return Optional.empty();
    }

    /** Returns empty because Minecraft 1.21 recipe ids are owned by {@code RecipeHolder}. */
    default Optional<Identifier> findRecipeId() {
        return Optional.empty();
    }

    default Identifier tableType() {
        return BuiltInRegistries.RECIPE_TYPE.getKey(recipe().getType());
    }

    int tier();

    default int sideLength() {
        return tier() * 2 + 1;
    }

    default int gridSize() {
        return sideLength() * sideLength();
    }

    default NonNullList<Optional<Ingredient>> slotIngredients() {
        var placement = recipe().placementInfo();
        if (placement.isImpossibleToPlace()) {
            return NonNullList.create();
        }

        return SlotIngredientLayout.expand(placement.slotsToIngredientIndex(), placement.ingredients());
    }

    default NonNullList<Optional<Ingredient>> ensureFittedCraftingGrid() {
        return slotIngredients();
    }

    default I createInput(List<ItemStack> items) {
        throw new UnsupportedOperationException("This table recipe does not expose an input factory");
    }

    default boolean matches(List<ItemStack> items, Level level) {
        return typedRecipe().matches(createInput(items), level);
    }

    default ItemStack assemble(List<ItemStack> items, Level level) {
        return typedRecipe().assemble(createInput(items));
    }

    default NonNullList<ItemStack> getRemainingItems(List<ItemStack> items) {
        var input = createInput(items);
        if (recipe() instanceof CraftingRecipe craftingRecipe && input instanceof CraftingInput craftingInput) {
            return craftingRecipe.getRemainingItems(craftingInput);
        }

        var remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStackTemplate remainder = input.getItem(slot).getCraftingRemainder();
            remaining.set(slot, remainder != null ? remainder.create() : ItemStack.EMPTY);
        }
        return remaining;
    }

    @SuppressWarnings("unchecked")
    private Recipe<I> typedRecipe() {
        return (Recipe<I>) recipe();
    }

}

final class SlotIngredientLayout {
    private SlotIngredientLayout() {
    }

    static <T> NonNullList<Optional<T>> expand(IntList slotsToValue, List<T> values) {
        var result = NonNullList.<Optional<T>>withSize(slotsToValue.size(), Optional.empty());
        for (int slot = 0; slot < slotsToValue.size(); slot++) {
            int valueIndex = slotsToValue.getInt(slot);
            if (valueIndex != -1) {
                result.set(slot, Optional.of(values.get(valueIndex)));
            }
        }
        return result;
    }
}
