package me.myogoo.myotus.data.builder.crafting;

import net.minecraft.resources.Identifier;

public final class AvaritiaNEOCraftingRecipeBuilder extends AbstractExternalCraftingRecipeBuilder {
    private AvaritiaNEOCraftingRecipeBuilder(Identifier id, boolean shaped) {
        super(id, shaped ? ExternalCraftingRecipeTypes.AVARITIA_NEO_EXTREME_SHAPED : ExternalCraftingRecipeTypes.AVARITIA_NEO_EXTREME_SHAPELESS, shaped);
    }

    public static AvaritiaNEOCraftingRecipeBuilder extremeShaped(Identifier id) {
        return new AvaritiaNEOCraftingRecipeBuilder(id, true);
    }

    public static AvaritiaNEOCraftingRecipeBuilder extremeShapeless(Identifier id) {
        return new AvaritiaNEOCraftingRecipeBuilder(id, false);
    }
}
