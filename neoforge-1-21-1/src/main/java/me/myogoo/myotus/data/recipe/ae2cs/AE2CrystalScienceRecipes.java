package me.myogoo.myotus.data.recipe.ae2cs;

import me.myogoo.myotus.data.builder.ae2cs.MyoCircuitEtcherRecipeBuilder;
import me.myogoo.myotus.data.builder.ae2cs.MyoCrystalAggregatorRecipeBuilder;
import me.myogoo.myotus.Myotus;
import me.myogoo.myotus.data.recipe.JsonRecipeProvider;
import me.myogoo.myotus.data.recipe.RecipeJsonHas;
import me.myogoo.myotus.data.recipe.crafting.MyoStonecuttingRecipeBuilder;
import appeng.core.definitions.AEItems;
import me.myogoo.myotus.init.MyoItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.crafting.Ingredient;

import static me.myogoo.myotus.data.recipe.ExternalRecipeBuilder.conditions;

public final class AE2CrystalScienceRecipes extends JsonRecipeProvider {
    public AE2CrystalScienceRecipes(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(JsonRecipeOutput output) {
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

    public static void buildStonecutting(RecipeOutput output) {
        MyoStonecuttingRecipeBuilder
                .stonecutting(Ingredient.of(AEItems.BLANK_PATTERN.asItem()), RecipeCategory.MISC, MyoItems.COMPAT_PRESS.get())
                .modLoaded("ae2cs")
                .unlockedBy("has_blank_pattern", RecipeJsonHas.has(AEItems.BLANK_PATTERN.asItem()))
                .save(output, Myotus.makeId("ae2cs/stonecutting/blank_pattern"));
    }

    @Override
    public String getName() {
        return "Myotus AE2 Crystal Science recipes";
    }
}
