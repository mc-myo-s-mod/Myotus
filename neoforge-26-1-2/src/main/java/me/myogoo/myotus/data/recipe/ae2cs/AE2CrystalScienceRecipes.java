package me.myogoo.myotus.data.recipe.ae2cs;

import me.myogoo.myotus.data.builder.ae2cs.MyoCircuitEtcherRecipeBuilder;
import me.myogoo.myotus.data.builder.ae2cs.MyoCrystalAggregatorRecipeBuilder;
import me.myogoo.myotus.Myotus;
import me.myogoo.myotus.data.recipe.JsonRecipeProvider;
import me.myogoo.myotus.data.recipe.crafting.MyoStonecuttingRecipeBuilder;
import me.myogoo.myotus.data.tag.MyotusTags;
import appeng.core.definitions.AEItems;
import me.myogoo.myotus.init.MyoItems;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

import static me.myogoo.myotus.data.recipe.ExternalRecipeBuilder.conditions;

public final class AE2CrystalScienceRecipes extends JsonRecipeProvider {
    public AE2CrystalScienceRecipes(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(JsonRecipeOutput output) {
        MyoCrystalAggregatorRecipeBuilder
                .create(id("ae2cs/aggregator/ae2cs_charged_ender_pearl"))
                .conditions(conditions("ae2cs"))
                .energy(400_000)
                .input_aTag("c:ender_pearls", 16)
                .result(MyoItems.CHARGED_ENDER_PEARL, 16)
                .save(output);

        MyoCrystalAggregatorRecipeBuilder
                .create(id("ae2cs/aggregator/ae2cs_charged_ender_pearl_block"))
                .conditions(conditions("ae2cs"))
                .energy(13_000_000)
                .input_aTag("c:storage_blocks/ender_pearl", 64)
                .result(MyoItems.CHARGED_ENDER_PEARL_BLOCK, 64)
                .save(output);

        MyoCrystalAggregatorRecipeBuilder
                .create(id("ae2cs/aggregator/ae2cs_compat_processor"))
                .conditions(conditions("ae2cs"))
                .energy(51_200)
                .input_a(MyoItems.PRINTED_COMPAT_PROCESSOR, 32)
                .input_b("c:dusts/redstone", 32)
                .input_c(AEItems.SILICON_PRINT.asItem(), 32)
                .result(MyoItems.COMPAT_PROCESSOR, 32)
                .save(output);

        MyoCircuitEtcherRecipeBuilder
                .create(id("ae2cs/circuit_etcher/ae2cs_compat_processor"))
                .conditions(conditions("ae2cs"))
                .energy(57_600)
                .input_a(MyoItems.CHARGED_ENDER_PEARL_BLOCK, 1)
                .input_b("c:storage_blocks/redstone", 1)
                .input_cTag("c:storage_blocks/silicon", 1)
                .result(MyoItems.COMPAT_PROCESSOR, 9)
                .save(output);

    }

    public static void buildStonecutting(RecipeOutput output, HolderGetter<Item> items) {
        MyoStonecuttingRecipeBuilder
                .stonecutting(Ingredient.of(items.getOrThrow(MyotusTags.Items.AE2CS_BLANK_PRINT_PRESSES)),
                        RecipeCategory.MISC, MyoItems.COMPAT_PRESS.get())
                .modLoaded("ae2cs")
                .unlockedBy("has_blank_print_press", InventoryChangeTrigger.TriggerInstance.hasItems(
                        ItemPredicate.Builder.item().of(items, MyotusTags.Items.AE2CS_BLANK_PRINT_PRESSES).build()))
                .save(output, Myotus.makeId("ae2cs/stonecutting/blank_pattern"));
    }

    @Override
    public String getName() {
        return "Myotus AE2 Crystal Science recipes";
    }
}
