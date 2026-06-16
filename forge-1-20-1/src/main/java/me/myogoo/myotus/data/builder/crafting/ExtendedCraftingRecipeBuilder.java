package me.myogoo.myotus.data.builder.crafting;

import net.minecraft.resources.ResourceLocation;

public final class ExtendedCraftingRecipeBuilder extends AbstractExternalCraftingRecipeBuilder {
    private ExtendedCraftingRecipeBuilder(ResourceLocation id, boolean shaped) {
        super(id, shaped ? ExternalCraftingRecipeTypes.EXTENDEDCRAFTING_SHAPED_TABLE : ExternalCraftingRecipeTypes.EXTENDEDCRAFTING_SHAPELESS_TABLE, shaped);
    }

    public static ExtendedCraftingRecipeBuilder shaped(ResourceLocation id) {
        return new ExtendedCraftingRecipeBuilder(id, true);
    }

    public static ExtendedCraftingRecipeBuilder shapeless(ResourceLocation id) {
        return new ExtendedCraftingRecipeBuilder(id, false);
    }
}
