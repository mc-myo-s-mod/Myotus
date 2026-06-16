package me.myogoo.myotus.data.builder.extendedae;

import me.myogoo.myotus.data.recipe.JsonRecipeProvider;
import me.myogoo.myotus.data.recipe.ExternalRecipeBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public final class MyoExtendedAECrystalAssemblerRecipeBuilder {
    private final ResourceLocation id;
    private final JsonObject json = new JsonObject();
    private final JsonArray inputItems = new JsonArray();

    private MyoExtendedAECrystalAssemblerRecipeBuilder(ResourceLocation id) {
        this.id = id;
        this.json.addProperty("type", "extendedae:crystal_assembler");
    }

    public static MyoExtendedAECrystalAssemblerRecipeBuilder create(ResourceLocation id) {
        return new MyoExtendedAECrystalAssemblerRecipeBuilder(id);
    }

    public MyoExtendedAECrystalAssemblerRecipeBuilder conditions(JsonArray conditions) {
        this.json.add("neoforge:conditions", conditions);
        return this;
    }

    public MyoExtendedAECrystalAssemblerRecipeBuilder inputItem(Ingredient ingredient, int amount) {
        this.inputItems.add(ExternalRecipeBuilder.counted(ExternalRecipeBuilder.ingredient(ingredient), amount).toAmountJson());
        return this;
    }

    public MyoExtendedAECrystalAssemblerRecipeBuilder inputItem(ItemLike item, int amount) {
        return inputItem(Ingredient.of(item), amount);
    }

    public MyoExtendedAECrystalAssemblerRecipeBuilder inputItem(String item, int amount) {
        this.inputItems.add(ExternalRecipeBuilder.counted(ExternalRecipeBuilder.ingredient(item), amount).toAmountJson());
        return this;
    }

    public MyoExtendedAECrystalAssemblerRecipeBuilder inputTag(String tag, int amount) {
        this.inputItems.add(ExternalRecipeBuilder.counted(ExternalRecipeBuilder.tag(tag), amount).toAmountJson());
        return this;
    }

    public MyoExtendedAECrystalAssemblerRecipeBuilder output(String item, int count) {
        this.json.add("output", ExternalRecipeBuilder.stack(item, count));
        return this;
    }

    public MyoExtendedAECrystalAssemblerRecipeBuilder output(ItemLike item, int count) {
        return output(ExternalRecipeBuilder.itemId(item), count);
    }

    public void save(JsonRecipeProvider.JsonRecipeOutput output) {
        this.json.add("input_items", this.inputItems);
        output.accept(this.id, this.json);
    }
}
