package me.myogoo.myotus.data.builder.ae2cs;

import me.myogoo.myotus.data.recipe.JsonRecipeProvider;
import me.myogoo.myotus.data.recipe.ExternalRecipeBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public final class MyoCrystalAggregatorRecipeBuilder {
    private final ResourceLocation id;
    private final JsonObject json = new JsonObject();

    private MyoCrystalAggregatorRecipeBuilder(ResourceLocation id) {
        this.id = id;
        this.json.addProperty("type", "ae2cs:crystal_aggregator_recipe_serializer");
    }

    public static MyoCrystalAggregatorRecipeBuilder create(ResourceLocation id) {
        return new MyoCrystalAggregatorRecipeBuilder(id);
    }

    public MyoCrystalAggregatorRecipeBuilder conditions(JsonArray conditions) {
        this.json.add("neoforge:conditions", conditions);
        return this;
    }

    public MyoCrystalAggregatorRecipeBuilder energy(int energyCost) {
        this.json.addProperty("energy_cost", energyCost);
        return this;
    }

    public MyoCrystalAggregatorRecipeBuilder input_a(Ingredient ingredient, int count) {
        this.json.add("input_a", countedIngredient(ingredient, count));
        return this;
    }

    public MyoCrystalAggregatorRecipeBuilder input_a(ItemLike item, int count) {
        return input_a(Ingredient.of(item), count);
    }

    public MyoCrystalAggregatorRecipeBuilder input_a(String item, int count) {
        this.json.add("input_a", countedItem(item, count));
        return this;
    }

    public MyoCrystalAggregatorRecipeBuilder input_b(String tag, int count) {
        this.json.add("input_b", countedTag(tag, count));
        return this;
    }

    public MyoCrystalAggregatorRecipeBuilder input_c(Ingredient ingredient, int count) {
        this.json.add("input_c", countedIngredient(ingredient, count));
        return this;
    }

    public MyoCrystalAggregatorRecipeBuilder input_c(ItemLike item, int count) {
        return input_c(Ingredient.of(item), count);
    }

    public MyoCrystalAggregatorRecipeBuilder input_c(String item, int count) {
        this.json.add("input_c", countedItem(item, count));
        return this;
    }

    public MyoCrystalAggregatorRecipeBuilder result(String item, int count) {
        this.json.add("result", ExternalRecipeBuilder.stack(item, count));
        return this;
    }

    public MyoCrystalAggregatorRecipeBuilder result(ItemLike item, int count) {
        return result(ExternalRecipeBuilder.itemId(item), count);
    }

    public void save(JsonRecipeProvider.JsonRecipeOutput output) {
        output.accept(this.id, this.json);
    }

    private static JsonObject countedIngredient(Ingredient ingredient, int count) {
        return ExternalRecipeBuilder.counted(ExternalRecipeBuilder.ingredient(ingredient), count).toCountJson();
    }

    private static JsonObject countedItem(String item, int count) {
        return ExternalRecipeBuilder.counted(ExternalRecipeBuilder.ingredient(item), count).toCountJson();
    }

    private static JsonObject countedTag(String tag, int count) {
        return ExternalRecipeBuilder.counted(ExternalRecipeBuilder.tag(tag), count).toCountJson();
    }
}
