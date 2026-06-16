package me.myogoo.myotus.data.recipe.ae2lt;

import me.myogoo.myotus.data.builder.ae2lt.MyoCrystalCatalyzerRecipeBuilder;
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

//        MyoCrystalCatalyzerRecipeBuilder
//                .create(id("ae2lt/crystal_catalyzer/ae2lt_charged_ender_pearl"))
//                .conditions(conditions("ae2lt"))
//                .catalyst(MyoItems.ENDER_PEARL_BLOCK.get(), 1)
//                .output(MyoItems.CHARGED_ENDER_PEARL.get(), 1)
//                .energyPerCycle(100_000)
//                .lightningCost(1)
//                .lightningTier("high_voltage")
//                .save(output);
    }

    @Override
    public String getName() {
        return "Myotus AE2 Lightning Tech recipes";
    }
}
