package me.myogoo.myotus.data.recipe.crafting;

import me.myogoo.myotus.api.datagen.MyoDevModeCondition;
import me.myogoo.myotus.api.datagen.MyoModCondition;
import me.myogoo.myotus.data.recipe.ExternalRecipeBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
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

public final class MyoShapelessRecipeBuilder {
    private final ShapelessRecipeBuilder delegate;
    private final List<ICondition> conditions = new ArrayList<>();

    private MyoShapelessRecipeBuilder(HolderGetter<Item> items, RecipeCategory category, ItemLike result, int count) {
        this.delegate = ShapelessRecipeBuilder.shapeless(items, category, result, count);
    }

    public static MyoShapelessRecipeBuilder shapeless(HolderGetter<Item> items, RecipeCategory category, ItemLike result) {
        return new MyoShapelessRecipeBuilder(items, category, result, 1);
    }

    public static MyoShapelessRecipeBuilder shapeless(HolderGetter<Item> items, RecipeCategory category, ItemLike result, int count) {
        return new MyoShapelessRecipeBuilder(items, category, result, count);
    }

    public MyoShapelessRecipeBuilder dev() {
        this.conditions.add(MyoDevModeCondition.INSTANCE);
        return this;
    }

    public MyoShapelessRecipeBuilder myoCondition(Class<? extends Annotation> annotationClass) {
        this.conditions.add(new MyoModCondition(ExternalRecipeBuilder.myoConditionId(annotationClass)));
        return this;
    }

    public MyoShapelessRecipeBuilder myoCondition(Annotation annotation) {
        this.conditions.add(new MyoModCondition(ExternalRecipeBuilder.myoConditionId(annotation)));
        return this;
    }

    public MyoShapelessRecipeBuilder myoCondition(String activeMod) {
        this.conditions.add(new MyoModCondition(activeMod));
        return this;
    }

    public MyoShapelessRecipeBuilder requires(ItemLike item) {
        this.delegate.requires(item);
        return this;
    }

    public MyoShapelessRecipeBuilder requires(ItemLike item, int quantity) {
        this.delegate.requires(item, quantity);
        return this;
    }

    public MyoShapelessRecipeBuilder requires(TagKey<Item> tag) {
        this.delegate.requires(tag);
        return this;
    }

    public MyoShapelessRecipeBuilder requires(Ingredient ingredient) {
        this.delegate.requires(ingredient);
        return this;
    }

    public MyoShapelessRecipeBuilder requires(Ingredient ingredient, int quantity) {
        this.delegate.requires(ingredient, quantity);
        return this;
    }

    public MyoShapelessRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
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
