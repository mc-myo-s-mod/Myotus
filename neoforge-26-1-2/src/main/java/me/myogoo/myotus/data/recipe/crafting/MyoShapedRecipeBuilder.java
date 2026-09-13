package me.myogoo.myotus.data.recipe.crafting;

import me.myogoo.myotus.api.datagen.MyoDevModeCondition;
import me.myogoo.myotus.api.datagen.MyoModCondition;
import me.myogoo.myotus.data.recipe.ExternalRecipeBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public final class MyoShapedRecipeBuilder {
    private final ShapedRecipeBuilder delegate;
    private final List<ICondition> conditions = new ArrayList<>();

    private MyoShapedRecipeBuilder(HolderGetter<Item> items, RecipeCategory category, ItemLike result, int count) {
        this.delegate = ShapedRecipeBuilder.shaped(items, category, result, count);
    }

    public static MyoShapedRecipeBuilder shaped(HolderGetter<Item> items, RecipeCategory category, ItemLike result) {
        return new MyoShapedRecipeBuilder(items, category, result, 1);
    }

    public static MyoShapedRecipeBuilder shaped(HolderGetter<Item> items, RecipeCategory category, ItemLike result, int count) {
        return new MyoShapedRecipeBuilder(items, category, result, count);
    }

    public MyoShapedRecipeBuilder dev() {
        this.conditions.add(MyoDevModeCondition.INSTANCE);
        return this;
    }

    public MyoShapedRecipeBuilder myoCondition(Class<? extends Annotation> annotationClass) {
        this.conditions.add(new MyoModCondition(ExternalRecipeBuilder.myoConditionId(annotationClass)));
        return this;
    }

    public MyoShapedRecipeBuilder myoCondition(Annotation annotation) {
        this.conditions.add(new MyoModCondition(ExternalRecipeBuilder.myoConditionId(annotation)));
        return this;
    }

    public MyoShapedRecipeBuilder myoCondition(String activeMod) {
        this.conditions.add(new MyoModCondition(activeMod));
        return this;
    }

    public MyoShapedRecipeBuilder define(Character symbol, ItemLike item) {
        this.delegate.define(symbol, item);
        return this;
    }

    public MyoShapedRecipeBuilder define(Character symbol, TagKey<Item> tag) {
        this.delegate.define(symbol, tag);
        return this;
    }

    public MyoShapedRecipeBuilder define(Character symbol, Ingredient ingredient) {
        this.delegate.define(symbol, ingredient);
        return this;
    }

    public MyoShapedRecipeBuilder pattern(String pattern) {
        this.delegate.pattern(pattern);
        return this;
    }

    public MyoShapedRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.delegate.unlockedBy(name, criterion);
        return this;
    }

    public void save(RecipeOutput output, Identifier id) {
        save(output, ResourceKey.create(Registries.RECIPE, id));
    }

    public void save(RecipeOutput output, ResourceKey<Recipe<?>> id) {
        RecipeOutput target = this.conditions.isEmpty()
                ? output
                : output.withConditions(this.conditions.toArray(ICondition[]::new));
        this.delegate.save(target, id);
    }
}
