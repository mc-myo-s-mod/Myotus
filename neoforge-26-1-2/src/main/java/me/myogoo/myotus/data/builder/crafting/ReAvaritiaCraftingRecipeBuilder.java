package me.myogoo.myotus.data.builder.crafting;

import net.minecraft.resources.Identifier;

public final class ReAvaritiaCraftingRecipeBuilder extends AbstractExternalCraftingRecipeBuilder {
    private ReAvaritiaCraftingRecipeBuilder(Identifier id, boolean shaped) {
        super(id, shaped ? ExternalCraftingRecipeTypes.RE_AVARITIA_SHAPED_TABLE : ExternalCraftingRecipeTypes.RE_AVARITIA_SHAPELESS_TABLE, shaped);
    }

    public static ReAvaritiaCraftingRecipeBuilder shaped(Identifier id) {
        return new ReAvaritiaCraftingRecipeBuilder(id, true);
    }

    public static ReAvaritiaCraftingRecipeBuilder shapeless(Identifier id) {
        return new ReAvaritiaCraftingRecipeBuilder(id, false);
    }
}
