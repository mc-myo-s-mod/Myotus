package me.myogoo.myotus.data.recipe.crafting;

import me.myogoo.myotus.api.datagen.MyoDevModeCondition;
import me.myogoo.myotus.api.datagen.MyoModCondition;
import me.myogoo.myotus.data.recipe.ExternalRecipeBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public final class MyoShapelessRecipeBuilder extends ShapelessRecipeBuilder {
    private final List<ICondition> conditions = new ArrayList<>();

    private MyoShapelessRecipeBuilder(RecipeCategory category, ItemLike result, int count) {
        super(category, result, count);
    }

    public static MyoShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result) {
        return new MyoShapelessRecipeBuilder(category, result, 1);
    }

    public static MyoShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result, int count) {
        return new MyoShapelessRecipeBuilder(category, result, count);
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

    @Override
    public MyoShapelessRecipeBuilder requires(ItemLike item) {
        super.requires(item);
        return this;
    }

    @Override
    public MyoShapelessRecipeBuilder requires(ItemLike item, int quantity) {
        super.requires(item, quantity);
        return this;
    }

    @Override
    public MyoShapelessRecipeBuilder requires(Ingredient ingredient) {
        super.requires(ingredient);
        return this;
    }

    @Override
    public MyoShapelessRecipeBuilder requires(Ingredient ingredient, int quantity) {
        super.requires(ingredient, quantity);
        return this;
    }

    @Override
    public MyoShapelessRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        super.unlockedBy(name, criterion);
        return this;
    }

    @Override
    public void save(RecipeOutput output, ResourceLocation id) {
        RecipeOutput target = this.conditions.isEmpty()
                ? output
                : output.withConditions(this.conditions.toArray(ICondition[]::new));
        super.save(target, id);
    }
}
