package me.myogoo.myotus.data.builder.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.myogoo.myotus.data.recipe.ExternalRecipeBuilder;
import me.myogoo.myotus.data.recipe.JsonRecipeProvider.JsonRecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.crafting.Ingredient;

public final class MyoExternalSmithingTransformRecipeBuilder {
    private final ResourceLocation id;
    private final JsonElement template;
    private final JsonElement base;
    private final JsonElement addition;
    private final String result;
    private JsonArray conditions;

    private MyoExternalSmithingTransformRecipeBuilder(ResourceLocation id, JsonElement template, JsonElement base, JsonElement addition, String result) {
        this.id = id;
        this.template = template;
        this.base = base;
        this.addition = addition;
        this.result = result;
    }

    public static MyoExternalSmithingTransformRecipeBuilder smithing(ResourceLocation id, Ingredient template, Ingredient base, Ingredient addition, String result) {
        return new MyoExternalSmithingTransformRecipeBuilder(id, ExternalRecipeBuilder.ingredient(template), ExternalRecipeBuilder.ingredient(base), ExternalRecipeBuilder.ingredient(addition), result);
    }

    public static MyoExternalSmithingTransformRecipeBuilder smithing(ResourceLocation id, ItemLike template, ItemLike base, ItemLike addition, String result) {
        return smithing(id, Ingredient.of(template), Ingredient.of(base), Ingredient.of(addition), result);
    }

    public static MyoExternalSmithingTransformRecipeBuilder smithing(ResourceLocation id, String template, String base, String addition, String result) {
        return new MyoExternalSmithingTransformRecipeBuilder(id, ExternalRecipeBuilder.ingredient(template), ExternalRecipeBuilder.ingredient(base), ExternalRecipeBuilder.ingredient(addition), result);
    }

    public MyoExternalSmithingTransformRecipeBuilder conditions(JsonArray conditions) {
        this.conditions = conditions;
        return this;
    }

    public MyoExternalSmithingTransformRecipeBuilder modLoaded(String modId) {
        return conditions(ExternalRecipeBuilder.conditions(modId));
    }

    public void save(JsonRecipeOutput output) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:smithing_transform");
        recipe.add("template", this.template);
        recipe.add("base", this.base);
        recipe.add("addition", this.addition);
        recipe.add("result", ExternalRecipeBuilder.item(this.result));

        JsonObject conditional = new JsonObject();
        conditional.addProperty("type", "forge:conditional");
        JsonArray recipes = new JsonArray();
        JsonObject entry = new JsonObject();
        entry.add("conditions", this.conditions == null ? new JsonArray() : this.conditions);
        entry.add("recipe", recipe);
        recipes.add(entry);
        conditional.add("recipes", recipes);
        output.accept(this.id, conditional);
    }
}
