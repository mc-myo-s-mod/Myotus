package me.myogoo.myotus.data.recipe.crafting;

import me.myogoo.myotus.Myotus;
import me.myogoo.myotus.data.recipe.RecipeJsonHas;
import me.myogoo.myotus.init.MyoItems;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class CraftingRecipes {
    private CraftingRecipes() {
    }

    public static void build(RecipeOutput output, HolderGetter<Item> items) {
        MyoShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, MyoItems.CHARGED_ENDER_PEARL_BLOCK.get())
                .pattern("PPP")
                .pattern("PPP")
                .pattern("PPP")
                .define('P', MyoItems.CHARGED_ENDER_PEARL.get())
                .unlockedBy("has_charged_ender_pearl", RecipeJsonHas.has(items, MyoItems.CHARGED_ENDER_PEARL.get()))
                .save(output, Myotus.makeId("crafting/charged_ender_pearl_block"));

        MyoShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, MyoItems.CHARGED_ENDER_PEARL.get(), 9)
                .requires(MyoItems.CHARGED_ENDER_PEARL_BLOCK.get())
                .unlockedBy("has_charged_ender_pearl_block", RecipeJsonHas.has(items, MyoItems.CHARGED_ENDER_PEARL_BLOCK.get()))
                .save(output, Myotus.makeId("crafting/charged_ender_pearls_from_block"));

        MyoShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, MyoItems.ENDER_PEARL_BLOCK.get())
                .pattern("PPP")
                .pattern("PPP")
                .pattern("PPP")
                .define('P', Items.ENDER_PEARL)
                .unlockedBy("has_ender_pearl", RecipeJsonHas.has(items, Items.ENDER_PEARL))
                .save(output, Myotus.makeId("crafting/ender_pearl_block"));

        MyoShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, Items.ENDER_PEARL, 9)
                .requires(MyoItems.ENDER_PEARL_BLOCK.get())
                .unlockedBy("has_ender_pearl_block", RecipeJsonHas.has(items, MyoItems.ENDER_PEARL_BLOCK.get()))
                .save(output, Myotus.makeId("crafting/ender_pearls_from_block"));

        MyoShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, Items.OBSIDIAN)
                .requires(Items.WATER_BUCKET)
                .requires(Items.LAVA_BUCKET)
                .dev()
                .unlockedBy("has_water_bucket", RecipeJsonHas.has(items, Items.WATER_BUCKET))
                .save(output, Myotus.makeId("crafting/dev_fluid_bucket_test"));
    }
}
