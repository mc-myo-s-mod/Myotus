package me.myogoo.myotus.data.recipe.crafting;

import me.myogoo.myotus.api.datagen.MyoDevModeCondition;
import me.myogoo.myotus.api.datagen.MyoModCondition;
import me.myogoo.myotus.data.recipe.ExternalRecipeBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public final class MyoStonecuttingRecipeBuilder extends SingleItemRecipeBuilder {
    private final List<ICondition> conditions = new ArrayList<>();

    private MyoStonecuttingRecipeBuilder(Ingredient ingredient, RecipeCategory category, ItemLike result, int count) {
        super(category, StonecutterRecipe::new, ingredient, result, count);
    }

    public static MyoStonecuttingRecipeBuilder stonecutting(Ingredient ingredient, RecipeCategory category, ItemLike result) {
        return new MyoStonecuttingRecipeBuilder(ingredient, category, result, 1);
    }

    public static MyoStonecuttingRecipeBuilder stonecutting(Ingredient ingredient, RecipeCategory category, ItemLike result, int count) {
        return new MyoStonecuttingRecipeBuilder(ingredient, category, result, count);
    }

    public MyoStonecuttingRecipeBuilder dev() {
        this.conditions.add(MyoDevModeCondition.INSTANCE);
        return this;
    }

    public MyoStonecuttingRecipeBuilder myoCondition(Class<? extends Annotation> annotationClass) {
        this.conditions.add(new MyoModCondition(ExternalRecipeBuilder.myoConditionId(annotationClass)));
        return this;
    }

    public MyoStonecuttingRecipeBuilder myoCondition(Annotation annotation) {
        this.conditions.add(new MyoModCondition(ExternalRecipeBuilder.myoConditionId(annotation)));
        return this;
    }

    public MyoStonecuttingRecipeBuilder myoCondition(String activeMod) {
        this.conditions.add(new MyoModCondition(activeMod));
        return this;
    }

    public MyoStonecuttingRecipeBuilder modLoaded(String modId) {
        this.conditions.add(new ModLoadedCondition(modId));
        return this;
    }

    @Override
    public MyoStonecuttingRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        super.unlockedBy(name, criterion);
        return this;
    }

    @Override
    public MyoStonecuttingRecipeBuilder group(String group) {
        super.group(group);
        return this;
    }

    public void save(RecipeOutput output, Identifier id) {
        save(output, ResourceKey.create(Registries.RECIPE, id));
    }

    @Override
    public void save(RecipeOutput output, ResourceKey<Recipe<?>> id) {
        RecipeOutput target = this.conditions.isEmpty()
                ? output
                : output.withConditions(this.conditions.toArray(ICondition[]::new));
        super.save(target, id);
    }
}
