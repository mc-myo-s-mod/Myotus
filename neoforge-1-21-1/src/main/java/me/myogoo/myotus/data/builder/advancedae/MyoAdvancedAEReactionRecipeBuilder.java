package me.myogoo.myotus.data.builder.advancedae;

import me.myogoo.myotus.data.recipe.JsonRecipeProvider;
import me.myogoo.myotus.data.recipe.ExternalRecipeBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public final class MyoAdvancedAEReactionRecipeBuilder {
    private final ResourceLocation id;
    private final JsonObject json = new JsonObject();
    private final JsonArray inputItems = new JsonArray();

    private MyoAdvancedAEReactionRecipeBuilder(ResourceLocation id) {
        this.id = id;
        this.json.addProperty("type", "advanced_ae:reaction");
    }

    public static MyoAdvancedAEReactionRecipeBuilder create(ResourceLocation id) {
        return new MyoAdvancedAEReactionRecipeBuilder(id);
    }

    public MyoAdvancedAEReactionRecipeBuilder conditions(JsonArray conditions) {
        this.json.add("neoforge:conditions", conditions);
        return this;
    }

    public MyoAdvancedAEReactionRecipeBuilder energy(int inputEnergy) {
        this.json.addProperty("input_energy", inputEnergy);
        return this;
    }

    public MyoAdvancedAEReactionRecipeBuilder fluid(String fluid, int amount) {
        this.json.add("input_fluid", ExternalRecipeBuilder.counted(ExternalRecipeBuilder.fluid(fluid), amount).toAmountJson());
        return this;
    }

    public MyoAdvancedAEReactionRecipeBuilder inputItem(Ingredient ingredient, int amount) {
        this.inputItems.add(ExternalRecipeBuilder.counted(ExternalRecipeBuilder.ingredient(ingredient), amount).toAmountJson());
        return this;
    }

    public MyoAdvancedAEReactionRecipeBuilder inputItem(ItemLike item, int amount) {
        return inputItem(Ingredient.of(item), amount);
    }

    public MyoAdvancedAEReactionRecipeBuilder inputItem(String item, int amount) {
        this.inputItems.add(ExternalRecipeBuilder.counted(ExternalRecipeBuilder.ingredient(item), amount).toAmountJson());
        return this;
    }

    public MyoAdvancedAEReactionRecipeBuilder inputTag(String tag, int amount) {
        this.inputItems.add(ExternalRecipeBuilder.counted(ExternalRecipeBuilder.tag(tag), amount).toAmountJson());
        return this;
    }

    public MyoAdvancedAEReactionRecipeBuilder output(String item, int amount) {
        this.json.add("output", ExternalRecipeBuilder.aeStack(item, amount));
        return this;
    }

    public MyoAdvancedAEReactionRecipeBuilder output(ItemLike item, int amount) {
        return output(ExternalRecipeBuilder.itemId(item), amount);
    }

    public void save(JsonRecipeProvider.JsonRecipeOutput output) {
        this.json.add("input_items", this.inputItems);
        output.accept(this.id, this.json);
    }
}
