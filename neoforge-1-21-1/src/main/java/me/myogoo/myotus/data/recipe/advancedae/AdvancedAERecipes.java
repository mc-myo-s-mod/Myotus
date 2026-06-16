package me.myogoo.myotus.data.recipe.advancedae;

import me.myogoo.myotus.data.builder.advancedae.MyoAdvancedAEReactionRecipeBuilder;
import me.myogoo.myotus.data.recipe.JsonRecipeProvider;
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
        reaction("advanced_ae/reaction_chamer/aae_charged_ender_pearl", "c:ender_pearls", 64, 1_300_000, 1_000,
                MyoItems.CHARGED_ENDER_PEARL.get())
                .save(output);

        reaction("advanced_ae/reaction_chamer/aae_charged_ender_pearl_block", "c:storage_blocks/ender_pearl", 64, 13_000_000,
                10_000, MyoItems.CHARGED_ENDER_PEARL_BLOCK.get())
                .save(output);
    }

    @Override
    public String getName() {
        return "Myotus Advanced AE recipes";
    }

    private static MyoAdvancedAEReactionRecipeBuilder reaction(String path, String inputTag, int inputAmount,
                                                                int energy, int waterAmount, ItemLike outputItem) {
        return MyoAdvancedAEReactionRecipeBuilder
                .create(id(path))
                .conditions(conditions("advanced_ae"))
                .energy(energy)
                .fluid("minecraft:water", waterAmount)
                .inputTag(inputTag, inputAmount)
                .output(outputItem, 64);
    }
}
