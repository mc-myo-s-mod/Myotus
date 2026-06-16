package me.myogoo.myotus.data.recipe.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.myogoo.myotus.data.recipe.ExternalRecipeBuilder;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

import java.lang.annotation.Annotation;
import java.util.function.Consumer;

public final class MyoStonecuttingRecipeBuilder extends SingleItemRecipeBuilder {
    private JsonArray conditions;

    private MyoStonecuttingRecipeBuilder(Ingredient ingredient, RecipeCategory category, ItemLike result, int count) {
        super(category, RecipeSerializer.STONECUTTER, ingredient, result, count);
    }

    public static MyoStonecuttingRecipeBuilder stonecutting(Ingredient ingredient, RecipeCategory category, ItemLike result) {
        return new MyoStonecuttingRecipeBuilder(ingredient, category, result, 1);
    }

    public static MyoStonecuttingRecipeBuilder stonecutting(Ingredient ingredient, RecipeCategory category, ItemLike result, int count) {
        return new MyoStonecuttingRecipeBuilder(ingredient, category, result, count);
    }

    public MyoStonecuttingRecipeBuilder dev() {
        this.ensureConditions();
        this.conditions.add(ExternalRecipeBuilder.devCondition());
        return this;
    }

    public MyoStonecuttingRecipeBuilder myoCondition(Class<? extends Annotation> annotationClass) {
        this.ensureConditions();
        this.conditions.add(ExternalRecipeBuilder.myoCondition(annotationClass));
        return this;
    }

    public MyoStonecuttingRecipeBuilder myoCondition(Annotation annotation) {
        this.ensureConditions();
        this.conditions.add(ExternalRecipeBuilder.myoCondition(annotation));
        return this;
    }

    public MyoStonecuttingRecipeBuilder myoCondition(String activeMod) {
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
    public MyoStonecuttingRecipeBuilder unlockedBy(String name, CriterionTriggerInstance criterion) {
        super.unlockedBy(name, criterion);
        return this;
    }

    @Override
    public MyoStonecuttingRecipeBuilder group(String group) {
        super.group(group);
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
