package me.myogoo.myotus.data.recipe.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.myogoo.myotus.data.recipe.ExternalRecipeBuilder;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

import java.lang.annotation.Annotation;
import java.util.function.Consumer;

public final class ShapedRecipeBuilder extends net.minecraft.data.recipes.ShapedRecipeBuilder {
    private JsonArray conditions;

    private ShapedRecipeBuilder(RecipeCategory category, ItemLike result, int count) {
        super(category, result, count);
    }

    public static ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result) {
        return new ShapedRecipeBuilder(category, result, 1);
    }

    public static ShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result, int count) {
        return new ShapedRecipeBuilder(category, result, count);
    }

    public ShapedRecipeBuilder dev() {
        this.ensureConditions();
        this.conditions.add(ExternalRecipeBuilder.devCondition());
        return this;
    }

    public ShapedRecipeBuilder myoCondition(Class<? extends Annotation> annotationClass) {
        this.ensureConditions();
        this.conditions.add(ExternalRecipeBuilder.myoCondition(annotationClass));
        return this;
    }

    public ShapedRecipeBuilder myoCondition(Annotation annotation) {
        this.ensureConditions();
        this.conditions.add(ExternalRecipeBuilder.myoCondition(annotation));
        return this;
    }

    public ShapedRecipeBuilder myoCondition(String activeMod) {
        this.ensureConditions();
        this.conditions.add(ExternalRecipeBuilder.myoCondition(activeMod));
        return this;
    }

    private void ensureConditions() {
        if (this.conditions == null) {
            this.conditions = new JsonArray();
        }
    }

    @Override
    public ShapedRecipeBuilder define(Character symbol, ItemLike item) {
        super.define(symbol, item);
        return this;
    }

    @Override
    public ShapedRecipeBuilder define(Character symbol, Ingredient ingredient) {
        super.define(symbol, ingredient);
        return this;
    }

    @Override
    public ShapedRecipeBuilder pattern(String pattern) {
        super.pattern(pattern);
        return this;
    }

    @Override
    public ShapedRecipeBuilder unlockedBy(String name, CriterionTriggerInstance criterion) {
        super.unlockedBy(name, criterion);
        return this;
    }

    @Override
    public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
        if (this.conditions == null || this.conditions.isEmpty()) {
            super.save(consumer, id);
            return;
        }
        super.save(recipe -> consumer.accept(new ConditionalFinishedRecipe(recipe, this.conditions)), id);
    }

    private record ConditionalFinishedRecipe(FinishedRecipe original, JsonArray conditions) implements FinishedRecipe {
        @Override
        public void serializeRecipeData(JsonObject json) {
            this.original.serializeRecipeData(json);
        }

        @Override
        public JsonObject serializeRecipe() {
            JsonObject conditional = new JsonObject();
            conditional.addProperty("type", "forge:conditional");
            JsonArray recipes = new JsonArray();
            JsonObject entry = new JsonObject();
            entry.add("conditions", this.conditions);
            entry.add("recipe", this.original.serializeRecipe());
            recipes.add(entry);
            conditional.add("recipes", recipes);
            return conditional;
        }

        @Override
        public ResourceLocation getId() {
            return this.original.getId();
        }

        @Override
        public RecipeSerializer<?> getType() {
            return this.original.getType();
        }

        @Override
        public JsonObject serializeAdvancement() {
            return this.original.serializeAdvancement();
        }

        @Override
        public ResourceLocation getAdvancementId() {
            return this.original.getAdvancementId();
        }
    }
}
