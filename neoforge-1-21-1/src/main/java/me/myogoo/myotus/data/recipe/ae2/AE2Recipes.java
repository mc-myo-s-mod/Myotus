package me.myogoo.myotus.data.recipe.ae2;

import me.myogoo.myotus.Myotus;
import appeng.core.definitions.AEItems;
import appeng.recipes.handlers.ChargerRecipeBuilder;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipeBuilder;
import me.myogoo.myotus.data.tag.MyotusTags;
import me.myogoo.myotus.init.MyoItems;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;

public final class AE2Recipes {
    private AE2Recipes() {
    }

    public static void build(RecipeOutput output) {
        ChargerRecipeBuilder.charge(output, Myotus.makeId("ae2/charger/charged_ender_pearl"), Tags.Items.ENDER_PEARLS, MyoItems.CHARGED_ENDER_PEARL.get());
        ChargerRecipeBuilder.charge(output, Myotus.makeId("ae2/charger/charged_ender_pearl_block"), MyotusTags.Items.STORAGE_BLOCKS_ENDER_PEARL, MyoItems.CHARGED_ENDER_PEARL_BLOCK.get());
        ChargerRecipeBuilder.charge(output, Myotus.makeId("ae2/charger/compat_press"), AEItems.ENGINEERING_PROCESSOR_PRESS.asItem(), MyoItems.COMPAT_PRESS.get());
        InscriberRecipeBuilder.inscribe(MyoItems.CHARGED_ENDER_PEARL.get(), MyoItems.PRINTED_COMPAT_PROCESSOR.get(), 1)
                .setTop(Ingredient.of(MyoItems.COMPAT_PRESS.get()))
                .setMode(InscriberProcessType.INSCRIBE)
                .save(output, Myotus.makeId("ae2/inscriber/printed_compat_processor"));
        InscriberRecipeBuilder.inscribe(Tags.Items.DUSTS_REDSTONE, MyoItems.COMPAT_PROCESSOR.get(), 1)
                .setTop(Ingredient.of(MyoItems.PRINTED_COMPAT_PROCESSOR.get()))
                .setBottom(Ingredient.of(AEItems.SILICON_PRINT.asItem()))
                .setMode(InscriberProcessType.PRESS)
                .save(output, Myotus.makeId("ae2/inscriber/compat_processor"));
    }
}
