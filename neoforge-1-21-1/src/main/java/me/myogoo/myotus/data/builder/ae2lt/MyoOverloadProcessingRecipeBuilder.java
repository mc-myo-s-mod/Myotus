package me.myogoo.myotus.data.builder.ae2lt;

import me.myogoo.myotus.data.recipe.JsonRecipeProvider;
import me.myogoo.myotus.data.recipe.ExternalRecipeBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public final class MyoOverloadProcessingRecipeBuilder {
    private final ResourceLocation id;
    private final JsonObject json = new JsonObject();
    private final JsonArray inputs = new JsonArray();
    private final JsonArray results = new JsonArray();

    private MyoOverloadProcessingRecipeBuilder(ResourceLocation id) {
        this.id = id;
        this.json.addProperty("type", "ae2lt:overload_processing");
    }

    public static MyoOverloadProcessingRecipeBuilder create(ResourceLocation id) {
        return new MyoOverloadProcessingRecipeBuilder(id);
    }

    public MyoOverloadProcessingRecipeBuilder conditions(JsonArray conditions) {
        this.json.add("neoforge:conditions", conditions);
        return this;
    }

    public MyoOverloadProcessingRecipeBuilder priority(int priority) {
        this.json.addProperty("priority", priority);
        return this;
    }

    public MyoOverloadProcessingRecipeBuilder inputItem(Ingredient ingredient, int count) {
        this.inputs.add(ExternalRecipeBuilder.counted(ExternalRecipeBuilder.ingredient(ingredient), count).toCountJson());
        return this;
    }

    public MyoOverloadProcessingRecipeBuilder inputItem(ItemLike item, int count) {
        return inputItem(Ingredient.of(item), count);
    }

    public MyoOverloadProcessingRecipeBuilder inputItem(String item, int count) {
        this.inputs.add(ExternalRecipeBuilder.counted(ExternalRecipeBuilder.ingredient(item), count).toCountJson());
        return this;
    }

    public MyoOverloadProcessingRecipeBuilder inputTag(String tag, int count) {
        this.inputs.add(ExternalRecipeBuilder.counted(ExternalRecipeBuilder.tag(tag), count).toCountJson());
        return this;
    }

    public MyoOverloadProcessingRecipeBuilder result(String item, int count) {
        this.results.add(ExternalRecipeBuilder.stack(item, count));
        return this;
    }

    public MyoOverloadProcessingRecipeBuilder result(ItemLike item, int count) {
        return result(ExternalRecipeBuilder.itemId(item), count);
    }

    public MyoOverloadProcessingRecipeBuilder totalEnergy(int totalEnergy) {
        this.json.addProperty("totalEnergy", totalEnergy);
        return this;
    }

    public MyoOverloadProcessingRecipeBuilder lightningCost(int lightningCost) {
        this.json.addProperty("lightningCost", lightningCost);
        return this;
    }

    public MyoOverloadProcessingRecipeBuilder lightningTier(String lightningTier) {
        this.json.addProperty("lightningTier", lightningTier);
        return this;
    }

    public void save(JsonRecipeProvider.JsonRecipeOutput output) {
        this.json.add("inputs", this.inputs);
        this.json.add("results", this.results);
        output.accept(this.id, this.json);
    }
}
