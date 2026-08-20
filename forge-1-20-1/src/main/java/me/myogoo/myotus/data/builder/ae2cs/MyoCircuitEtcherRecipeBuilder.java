package me.myogoo.myotus.data.builder.ae2cs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.myogoo.myotus.data.recipe.ExternalRecipeBuilder;
import me.myogoo.myotus.data.recipe.JsonRecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public final class MyoCircuitEtcherRecipeBuilder {
    private final ResourceLocation id;
    private final JsonObject json = new JsonObject();

    private MyoCircuitEtcherRecipeBuilder(ResourceLocation id) {
        this.id = id;
        this.json.addProperty("type", "ae2cs:circuit_etcher_recipe_serializer");
    }

    public static MyoCircuitEtcherRecipeBuilder create(ResourceLocation id) {
        return new MyoCircuitEtcherRecipeBuilder(id);
    }

    public MyoCircuitEtcherRecipeBuilder conditions(JsonArray conditions) {
        this.json.add("conditions", conditions);
        return this;
    }

    public MyoCircuitEtcherRecipeBuilder energy(int energyCost) {
        this.json.addProperty("energy_cost", energyCost);
        return this;
    }

    public MyoCircuitEtcherRecipeBuilder input_a(Ingredient ingredient, int count) {
        this.json.add("input_a", countedIngredient(ExternalRecipeBuilder.ingredient(ingredient), count));
        return this;
    }

    public MyoCircuitEtcherRecipeBuilder input_a(ItemLike item, int count) {
        this.json.add("input_a", countedIngredient(ExternalRecipeBuilder.item(item), count));
        return this;
    }

    public MyoCircuitEtcherRecipeBuilder input_a(String item, int count) {
        this.json.add("input_a", countedIngredient(ExternalRecipeBuilder.item(item), count));
        return this;
    }

    public MyoCircuitEtcherRecipeBuilder input_aTag(String tag, int count) {
        this.json.add("input_a", countedIngredient(ExternalRecipeBuilder.tag(tag), count));
        return this;
    }

    public MyoCircuitEtcherRecipeBuilder input_bTag(String tag, int count) {
        this.json.add("input_b", countedIngredient(ExternalRecipeBuilder.tag(tag), count));
        return this;
    }

    public MyoCircuitEtcherRecipeBuilder input_c(Ingredient ingredient, int count) {
        this.json.add("input_c", countedIngredient(ExternalRecipeBuilder.ingredient(ingredient), count));
        return this;
    }

    public MyoCircuitEtcherRecipeBuilder input_c(ItemLike item, int count) {
        this.json.add("input_c", countedIngredient(ExternalRecipeBuilder.item(item), count));
        return this;
    }

    public MyoCircuitEtcherRecipeBuilder input_c(String item, int count) {
        this.json.add("input_c", countedIngredient(ExternalRecipeBuilder.item(item), count));
        return this;
    }

    public MyoCircuitEtcherRecipeBuilder input_cTag(String tag, int count) {
        this.json.add("input_c", countedIngredient(ExternalRecipeBuilder.tag(tag), count));
        return this;
    }

    public MyoCircuitEtcherRecipeBuilder result(String item, int count) {
        this.json.add("result", ExternalRecipeBuilder.stack(item, count));
        return this;
    }

    public MyoCircuitEtcherRecipeBuilder result(ItemLike item, int count) {
        return result(ExternalRecipeBuilder.itemId(item), count);
    }

    public void save(JsonRecipeProvider.JsonRecipeOutput output) {
        output.accept(this.id, this.json);
    }

    private static JsonObject countedIngredient(JsonElement ingredient, int count) {
        JsonObject json = new JsonObject();
        json.addProperty("count", count);
        json.add("ingredient", ingredient);
        return json;
    }
}
