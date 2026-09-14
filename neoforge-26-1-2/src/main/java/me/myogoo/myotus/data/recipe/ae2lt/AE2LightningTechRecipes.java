package me.myogoo.myotus.data.recipe.ae2lt;

import me.myogoo.myotus.data.builder.ae2lt.MyoOverloadProcessingRecipeBuilder;
import me.myogoo.myotus.data.recipe.JsonRecipeProvider;
import me.myogoo.myotus.init.MyoItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;

import static me.myogoo.myotus.data.recipe.ExternalRecipeBuilder.conditions;

public final class AE2LightningTechRecipes extends JsonRecipeProvider {
    public AE2LightningTechRecipes(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(JsonRecipeOutput output) {
        MyoOverloadProcessingRecipeBuilder
                .create(id("ae2lt/overload_processing/ae2lt_charged_ender_pearl"))
                .conditions(conditions("ae2lt"))
                .priority(0)
                .inputTag("c:ender_pearls", 16)
                .inputFluid("minecraft:water", 250)
                .result(MyoItems.CHARGED_ENDER_PEARL, 16)
                .totalEnergy(400_000)
                .lightningCost(1)
                .lightningTier("high_voltage")
                .save(output);

        MyoOverloadProcessingRecipeBuilder
                .create(id("ae2lt/overload_processing/ae2lt_charged_ender_pearl_block"))
                .conditions(conditions("ae2lt"))
                .priority(0)
                .inputTag("c:storage_blocks/ender_pearl", 64)
                .inputFluid("minecraft:water", 10_000)
                .result(MyoItems.CHARGED_ENDER_PEARL_BLOCK, 64)
                .totalEnergy(13_000_000)
                .lightningCost(1)
                .lightningTier("high_voltage")
                .save(output);

        MyoOverloadProcessingRecipeBuilder
                .create(id("ae2lt/overload_processing/ae2lt_compat_processor"))
                .conditions(conditions("ae2lt"))
                .priority(0)
                .inputItem(MyoItems.CHARGED_ENDER_PEARL_BLOCK, 4)
                .inputItem(Items.REDSTONE_BLOCK, 4)
                .inputTag("c:storage_blocks/silicon", 4)
                .result(MyoItems.COMPAT_PROCESSOR, 36)
                .totalEnergy(400_000)
                .lightningCost(1)
                .lightningTier("high_voltage")
                .save(output);
    }

    @Override
    public String getName() {
        return "Myotus AE2 Lightning Tech recipes";
    }
}
