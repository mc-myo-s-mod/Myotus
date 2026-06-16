package me.myogoo.myotus.data.recipe.extendedae;

import me.myogoo.myotus.data.builder.extendedae.MyoExtendedAECircuitCutterRecipeBuilder;
import me.myogoo.myotus.data.recipe.JsonRecipeProvider;
import me.myogoo.myotus.init.MyoItems;
import net.minecraft.data.PackOutput;

import static me.myogoo.myotus.data.recipe.ExternalRecipeBuilder.conditions;

public final class ExtendedAERecipes extends JsonRecipeProvider {
    public ExtendedAERecipes(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(JsonRecipeOutput output) {
        MyoExtendedAECircuitCutterRecipeBuilder
                .create(id("extendedae/circuit_cutter/aee_printed_compat_processor"))
                .conditions(conditions("expatternprovider"))
                .fluidInput("minecraft:water", 100)
                .itemInput(MyoItems.CHARGED_ENDER_PEARL_BLOCK.get(), 1)
                .output(MyoItems.PRINTED_COMPAT_PROCESSOR.get(), 9)
                .save(output);
    }

    @Override
    public String getName() {
        return "Myotus Extended AE recipes";
    }
}
