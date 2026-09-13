package me.myogoo.myotus.data.recipe.crafting;

import me.myogoo.myotus.api.datagen.MyoDevModeCondition;
import me.myogoo.myotus.api.datagen.MyoModCondition;
import me.myogoo.myotus.data.recipe.ExternalRecipeBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.SmithingTrimRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public final class MyoSmithingTrimRecipeBuilder extends SmithingTrimRecipeBuilder {
    private final List<ICondition> conditions = new ArrayList<>();

    private MyoSmithingTrimRecipeBuilder(Ingredient template, Ingredient base, Ingredient addition,
                                         Holder<TrimPattern> pattern, RecipeCategory category) {
        super(category, template, base, addition, pattern);
    }

    public static MyoSmithingTrimRecipeBuilder smithingTrim(Ingredient template, Ingredient base, Ingredient addition,
                                                            Holder<TrimPattern> pattern, RecipeCategory category) {
        return new MyoSmithingTrimRecipeBuilder(template, base, addition, pattern, category);
    }

    public MyoSmithingTrimRecipeBuilder dev() {
        this.conditions.add(MyoDevModeCondition.INSTANCE);
        return this;
    }

    public MyoSmithingTrimRecipeBuilder myoCondition(Class<? extends Annotation> annotationClass) {
        this.conditions.add(new MyoModCondition(ExternalRecipeBuilder.myoConditionId(annotationClass)));
        return this;
    }

    public MyoSmithingTrimRecipeBuilder myoCondition(Annotation annotation) {
        this.conditions.add(new MyoModCondition(ExternalRecipeBuilder.myoConditionId(annotation)));
        return this;
    }

    public MyoSmithingTrimRecipeBuilder myoCondition(String activeMod) {
        this.conditions.add(new MyoModCondition(activeMod));
        return this;
    }

    @Override
    public MyoSmithingTrimRecipeBuilder unlocks(String name, Criterion<?> criterion) {
        super.unlocks(name, criterion);
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
