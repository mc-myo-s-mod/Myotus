package me.myogoo.myotus.data.recipe.crafting;

import me.myogoo.myotus.Myotus;
import me.myogoo.myotus.data.recipe.RecipeJsonHas;
import me.myogoo.myotus.init.MyoItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public final class CraftingRecipes {
    private CraftingRecipes() {
    }

    public static void build(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MyoItems.CHARGED_ENDER_PEARL_BLOCK.get())
                .pattern("PPP")
                .pattern("PPP")
                .pattern("PPP")
                .define('P', MyoItems.CHARGED_ENDER_PEARL.get())
                .unlockedBy("has_charged_ender_pearl", RecipeJsonHas.has(MyoItems.CHARGED_ENDER_PEARL.get()))
                .save(consumer, Myotus.makeId("crafting/charged_ender_pearl_block"));
        MyoShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, MyoItems.CHARGED_ENDER_PEARL.get(), 9)
                .requires(MyoItems.CHARGED_ENDER_PEARL_BLOCK.get())
                .unlockedBy("has_charged_ender_pearl_block", RecipeJsonHas.has(MyoItems.CHARGED_ENDER_PEARL_BLOCK.get()))
                .save(consumer, Myotus.makeId("crafting/charged_ender_pearls_from_block"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MyoItems.ENDER_PEARL_BLOCK.get())
                .pattern("PPP")
                .pattern("PPP")
                .pattern("PPP")
                .define('P', Items.ENDER_PEARL)
                .unlockedBy("has_ender_pearl", RecipeJsonHas.has(Items.ENDER_PEARL))
                .save(consumer, Myotus.makeId("crafting/ender_pearl_block"));
        MyoShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.ENDER_PEARL, 9)
                .requires(MyoItems.ENDER_PEARL_BLOCK.get())
                .unlockedBy("has_ender_pearl_block", RecipeJsonHas.has(MyoItems.ENDER_PEARL_BLOCK.get()))
                .save(consumer, Myotus.makeId("crafting/ender_pearls_from_block"));
    }
}
