package me.myogoo.myotus.data.recipe.crafting;

import me.myogoo.myotus.data.builder.crafting.AvaritiaNEOCraftingRecipeBuilder;
import me.myogoo.myotus.data.builder.crafting.ExtendedCraftingRecipeBuilder;
import me.myogoo.myotus.data.builder.crafting.ReAvaritiaCraftingRecipeBuilder;
import me.myogoo.myotus.data.recipe.JsonRecipeProvider;
import me.myogoo.myotus.init.MyoItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;

public final class ExternalCraftingRecipes extends JsonRecipeProvider {
    public ExternalCraftingRecipes(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(JsonRecipeOutput output) {
        ExtendedCraftingRecipeBuilder.shaped(id("crafting/extendedcrafting/dev_diamond"))
                .dev()
                .myoCondition("extendedcrafting")
                .pattern(" E ")
                .pattern("RCR")
                .pattern(" P ")
                .define('E', Items.ENDER_PEARL)
                .define('R', Items.REDSTONE)
                .define('C', MyoItems.CHARGED_ENDER_PEARL.get())
                .define('P', Items.PAPER)
                .tier(1)
                .result(Items.DIAMOND, 1)
                .save(output);

        AvaritiaNEOCraftingRecipeBuilder.extremeShaped(id("crafting/avaritia-neo/dev_emerald"))
                .dev()
                .myoCondition("avaritia-neo")
                .pattern("RGR")
                .pattern("GCG")
                .pattern("RGR")
                .define('R', Items.REDSTONE)
                .define('G', Items.GOLD_INGOT)
                .define('C', MyoItems.CHARGED_ENDER_PEARL.get())
                .result(Items.EMERALD, 1)
                .save(output);

        ReAvaritiaCraftingRecipeBuilder.shaped(id("crafting/re-avaritia/dev_charged_ender_pearl"))
                .dev()
                .myoCondition("re-avaritia")
                .pattern("LPL")
                .pattern("PEP")
                .pattern("LPL")
                .define('L', Items.LAPIS_LAZULI)
                .define('P', Items.ENDER_PEARL)
                .define('E', MyoItems.ENDER_PEARL_BLOCK.get())
                .tier(1)
                .result(MyoItems.CHARGED_ENDER_PEARL.get(), 1)
                .save(output);
    }

    @Override
    public String getName() {
        return "Myotus external crafting recipes";
    }
}
