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
        this.json.addProperty("type", "extendedae:circuit_cutter");
    }

    public static MyoExtendedAECircuitCutterRecipeBuilder create(ResourceLocation id) {
        return new MyoExtendedAECircuitCutterRecipeBuilder(id);
    }

    public MyoExtendedAECircuitCutterRecipeBuilder conditions(JsonArray conditions) {
        this.json.add("neoforge:conditions", conditions);
        return this;
    }

    public MyoExtendedAECircuitCutterRecipeBuilder input(Ingredient ingredient) {
        JsonObject input = new JsonObject();
        input.add("ingredient", ExternalRecipeBuilder.ingredient(ingredient));
        this.json.add("input", input);
        return this;
    }

    public MyoExtendedAECircuitCutterRecipeBuilder input(ItemLike item) {
        return input(Ingredient.of(item));
    }

    public MyoExtendedAECircuitCutterRecipeBuilder input(String item) {
        JsonObject input = new JsonObject();
        input.add("ingredient", ExternalRecipeBuilder.ingredient(item));
        this.json.add("input", input);
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
