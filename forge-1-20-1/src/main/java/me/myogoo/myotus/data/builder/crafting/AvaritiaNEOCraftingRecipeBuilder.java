package me.myogoo.myotus.data.builder.crafting;

import net.minecraft.resources.ResourceLocation;

public final class AvaritiaNEOCraftingRecipeBuilder extends AbstractExternalCraftingRecipeBuilder {
    private AvaritiaNEOCraftingRecipeBuilder(ResourceLocation id, boolean shaped) {
        super(id, shaped ? ExternalCraftingRecipeTypes.AVARITIA_NEO_EXTREME_SHAPED : ExternalCraftingRecipeTypes.AVARITIA_NEO_EXTREME_SHAPELESS, shaped);
    }

    public static AvaritiaNEOCraftingRecipeBuilder extremeShaped(ResourceLocation id) {
        return new AvaritiaNEOCraftingRecipeBuilder(id, true);
    }

    public static AvaritiaNEOCraftingRecipeBuilder extremeShapeless(ResourceLocation id) {
        return new AvaritiaNEOCraftingRecipeBuilder(id, false);
    }
}
