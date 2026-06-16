package me.myogoo.myotus.data.builder.extendedae;

import me.myogoo.myotus.data.recipe.JsonRecipeProvider;
import me.myogoo.myotus.data.recipe.ExternalRecipeBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public final class MyoExtendedAECircuitCutterRecipeBuilder {
    private final ResourceLocation id;
    private final JsonObject json = new JsonObject();

    private MyoExtendedAECircuitCutterRecipeBuilder(ResourceLocation id) {
        this.id = id;
        this.json.addProperty("type", "expatternprovider:circuit_cutter");
    }

    public static MyoExtendedAECircuitCutterRecipeBuilder create(ResourceLocation id) {
        return new MyoExtendedAECircuitCutterRecipeBuilder(id);
    }

    public MyoExtendedAECircuitCutterRecipeBuilder conditions(JsonArray conditions) {
        this.json.add("conditions", conditions);
        return this;
    }

    public MyoExtendedAECircuitCutterRecipeBuilder fluidInput(String fluid, int amount) {
        this.json.add("fluid_input", ExternalRecipeBuilder.counted(ExternalRecipeBuilder.fluid(fluid), amount).toAmountJson());
        return this;
    }

    public MyoExtendedAECircuitCutterRecipeBuilder itemInput(Ingredient ingredient, int amount) {
        this.json.add("item_input", ExternalRecipeBuilder.counted(ExternalRecipeBuilder.ingredient(ingredient), amount).toAmountJson());
        return this;
    }

    public MyoExtendedAECircuitCutterRecipeBuilder itemInput(ItemLike item, int amount) {
        return itemInput(Ingredient.of(item), amount);
    }

    public MyoExtendedAECircuitCutterRecipeBuilder itemInput(String item, int amount) {
        this.json.add("item_input", ExternalRecipeBuilder.counted(ExternalRecipeBuilder.ingredient(item), amount).toAmountJson());
        return this;
    }

    public MyoExtendedAECircuitCutterRecipeBuilder output(String item, int count) {
        this.json.add("output", ExternalRecipeBuilder.stack(item, count));
        return this;
    }

    public MyoExtendedAECircuitCutterRecipeBuilder output(ItemLike item, int count) {
        return output(ExternalRecipeBuilder.itemId(item), count);
    }

    public void save(JsonRecipeProvider.JsonRecipeOutput output) {
        output.accept(this.id, this.json);
    }
}
