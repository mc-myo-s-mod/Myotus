package me.myogoo.myotus.data.builder.crafting;

import net.minecraft.resources.ResourceLocation;

public final class ReAvaritiaCraftingRecipeBuilder extends AbstractExternalCraftingRecipeBuilder {
    private ReAvaritiaCraftingRecipeBuilder(ResourceLocation id, boolean shaped) {
        super(id, shaped ? ExternalCraftingRecipeTypes.RE_AVARITIA_SHAPED_TABLE : ExternalCraftingRecipeTypes.RE_AVARITIA_SHAPELESS_TABLE, shaped);
    }

    public static ReAvaritiaCraftingRecipeBuilder shaped(ResourceLocation id) {
        return new ReAvaritiaCraftingRecipeBuilder(id, true);
    }

    public static ReAvaritiaCraftingRecipeBuilder shapeless(ResourceLocation id) {
        return new ReAvaritiaCraftingRecipeBuilder(id, false);
    }
}
