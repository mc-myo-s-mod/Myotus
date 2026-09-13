package me.myogoo.myotus.data.builder.crafting;

import net.minecraft.resources.Identifier;

public final class ExtendedCraftingRecipeBuilder extends AbstractExternalCraftingRecipeBuilder {
    private ExtendedCraftingRecipeBuilder(Identifier id, boolean shaped) {
        super(id, shaped ? ExternalCraftingRecipeTypes.EXTENDEDCRAFTING_SHAPED_TABLE : ExternalCraftingRecipeTypes.EXTENDEDCRAFTING_SHAPELESS_TABLE, shaped);
    }

    public static ExtendedCraftingRecipeBuilder shaped(Identifier id) {
        return new ExtendedCraftingRecipeBuilder(id, true);
    }

    public static ExtendedCraftingRecipeBuilder shapeless(Identifier id) {
        return new ExtendedCraftingRecipeBuilder(id, false);
    }
}
