package me.myogoo.myotus.data.builder.crafting;

import me.myogoo.myotus.data.recipe.JsonRecipeProvider;
import me.myogoo.myotus.data.recipe.ExternalRecipeBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractExternalCraftingRecipeBuilder {
    private final ResourceLocation id;
    private final String type;
    private final boolean shaped;
    private final JsonArray conditions = new JsonArray();
    private final JsonArray pattern = new JsonArray();
    private final Map<Character, JsonElement> key = new LinkedHashMap<>();
    private final JsonArray ingredients = new JsonArray();
    private final Map<String, Number> numericProperties = new LinkedHashMap<>();
    private final Map<String, String> stringProperties = new LinkedHashMap<>();
    private String result;
    private int count = 1;

    protected AbstractExternalCraftingRecipeBuilder(ResourceLocation id, String type, boolean shaped) {
        this.id = id;
        this.type = type;
        this.shaped = shaped;
    }

    public AbstractExternalCraftingRecipeBuilder dev() {
        return this.conditions(ExternalRecipeBuilder.devConditions());
    }

    public AbstractExternalCraftingRecipeBuilder conditions(JsonArray conditions) {
        conditions.forEach(this.conditions::add);
        return this;
    }

    public AbstractExternalCraftingRecipeBuilder myoCondition(Class<? extends Annotation> annotationClass) {
        this.conditions.add(ExternalRecipeBuilder.myoCondition(annotationClass));
        return this;
    }

    public AbstractExternalCraftingRecipeBuilder myoCondition(Annotation annotation) {
        this.conditions.add(ExternalRecipeBuilder.myoCondition(annotation));
        return this;
    }

    public AbstractExternalCraftingRecipeBuilder myoCondition(String activeMod) {
        this.conditions.add(ExternalRecipeBuilder.myoCondition(activeMod));
        return this;
    }

    public AbstractExternalCraftingRecipeBuilder pattern(String row) {
        this.pattern.add(row);
        return this;
    }

    public AbstractExternalCraftingRecipeBuilder define(char key, Ingredient ingredient) {
        this.key.put(key, ExternalRecipeBuilder.ingredient(ingredient));
        return this;
    }

    public AbstractExternalCraftingRecipeBuilder define(char key, ItemLike item) {
        return define(key, Ingredient.of(item));
    }

    public AbstractExternalCraftingRecipeBuilder define(char key, String item) {
        this.key.put(key, ExternalRecipeBuilder.ingredient(item));
        return this;
    }

    public AbstractExternalCraftingRecipeBuilder requires(Ingredient ingredient) {
        this.ingredients.add(ExternalRecipeBuilder.ingredient(ingredient));
        return this;
    }

    public AbstractExternalCraftingRecipeBuilder requires(ItemLike item) {
        return requires(Ingredient.of(item));
    }

    public AbstractExternalCraftingRecipeBuilder requires(String item) {
        this.ingredients.add(ExternalRecipeBuilder.ingredient(item));
        return this;
    }

    public AbstractExternalCraftingRecipeBuilder tier(int tier) {
        this.numericProperties.put("tier", tier);
        return this;
    }

    public AbstractExternalCraftingRecipeBuilder category(String category) {
        this.stringProperties.put("category", category);
        return this;
    }

    public AbstractExternalCraftingRecipeBuilder result(String item, int count) {
        this.result = item;
        this.count = count;
        return this;
    }

    public AbstractExternalCraftingRecipeBuilder result(ItemLike item, int count) {
        return result(ExternalRecipeBuilder.itemId(item), count);
    }

    public void save(JsonRecipeProvider.JsonRecipeOutput output) {
        JsonObject json = new JsonObject();
        if (!conditions.isEmpty()) {
            json.add("neoforge:conditions", conditions);
        }
        json.addProperty("type", type);
        if (shaped) {
            json.add("pattern", pattern);
            JsonObject keyJson = new JsonObject();
            key.forEach((symbol, ingredient) -> keyJson.add(String.valueOf(symbol), ingredient));
            json.add("key", keyJson);
        } else {
            json.add("ingredients", ingredients);
        }
        json.add("result", ExternalRecipeBuilder.stack(result, count));
        stringProperties.forEach(json::addProperty);
        numericProperties.forEach(json::addProperty);
        output.accept(id, json);
    }
}
