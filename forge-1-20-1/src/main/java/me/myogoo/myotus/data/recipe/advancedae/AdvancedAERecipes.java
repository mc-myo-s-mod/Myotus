package me.myogoo.myotus.data.recipe.advancedae;

import me.myogoo.myotus.data.builder.advancedae.MyoAdvancedAEReactionRecipeBuilder;
import me.myogoo.myotus.data.recipe.JsonRecipeProvider;
import appeng.core.definitions.AEItems;
import me.myogoo.myotus.init.MyoItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.ItemLike;

import static me.myogoo.myotus.data.recipe.ExternalRecipeBuilder.conditions;

public final class AdvancedAERecipes extends JsonRecipeProvider {
    public AdvancedAERecipes(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(JsonRecipeOutput output) {
        reaction("advanced_ae/reaction_chamer/aae_charged_ender_pearl", "forge:ender_pearls", 64, 1_300_000, 1_000,
                MyoItems.CHARGED_ENDER_PEARL.get(), 64)
                .save(output);

        reaction("advanced_ae/reaction_chamer/aae_charged_ender_pearl_block", "forge:storage_blocks/ender_pearl", 64,
                13_000_000, 10_000, MyoItems.CHARGED_ENDER_PEARL_BLOCK.get(), 64)
                .save(output);

        MyoAdvancedAEReactionRecipeBuilder
                .create(id("advanced_ae/reaction_chamer/aae_compat_processor"))
                .conditions(conditions("advanced_ae"))
                .energy(20_000)
                .fluid("minecraft:water", 100)
                .inputItem(MyoItems.PRINTED_COMPAT_PROCESSOR.get(), 4)
                .inputItem(AEItems.SILICON_PRINT.asItem(), 4)
                .inputTag("forge:dusts/redstone", 4)
                .output(MyoItems.COMPAT_PROCESSOR.get(), 4)
                .save(output);
    }

    @Override
    public String getName() {
        return "Myotus Advanced AE recipes";
    }

    private static MyoAdvancedAEReactionRecipeBuilder reaction(String path, String inputTag, int inputAmount,
                                                                int energy, int waterAmount, ItemLike outputItem,
                                                                int outputAmount) {
        return MyoAdvancedAEReactionRecipeBuilder
                .create(id(path))
                .conditions(conditions("advanced_ae"))
                .energy(energy)
                .fluid("minecraft:water", waterAmount)
                .inputTag(inputTag, inputAmount)
                .output(outputItem, outputAmount);
    }
}
