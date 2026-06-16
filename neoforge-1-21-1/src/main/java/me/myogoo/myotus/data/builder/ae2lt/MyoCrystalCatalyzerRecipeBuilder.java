package me.myogoo.myotus.data.builder.ae2lt;

import me.myogoo.myotus.data.recipe.JsonRecipeProvider;
import me.myogoo.myotus.data.recipe.ExternalRecipeBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public final class MyoCrystalCatalyzerRecipeBuilder {
    private final ResourceLocation id;
    private final JsonObject json = new JsonObject();

    private MyoCrystalCatalyzerRecipeBuilder(ResourceLocation id) {
        this.id = id;
        this.json.addProperty("type", "ae2lt:crystal_catalyzer");
    }

    public static MyoCrystalCatalyzerRecipeBuilder create(ResourceLocation id) {
        return new MyoCrystalCatalyzerRecipeBuilder(id);
    }

    public MyoCrystalCatalyzerRecipeBuilder conditions(JsonArray conditions) {
        this.json.add("neoforge:conditions", conditions);
        return this;
    }

    public MyoCrystalCatalyzerRecipeBuilder catalyst(Ingredient ingredient, int count) {
        this.json.add("catalyst", ExternalRecipeBuilder.ingredient(ingredient));
        this.json.addProperty("catalystCount", count);
        return this;
    }

    public MyoCrystalCatalyzerRecipeBuilder catalyst(ItemLike item, int count) {
        return catalyst(Ingredient.of(item), count);
    }

    public MyoCrystalCatalyzerRecipeBuilder catalyst(String item, int count) {
        this.json.add("catalyst", ExternalRecipeBuilder.ingredient(item));
        this.json.addProperty("catalystCount", count);
        return this;
    }

    public MyoCrystalCatalyzerRecipeBuilder output(String item, int count) {
        this.json.add("output", ExternalRecipeBuilder.stack(item, count));
        return this;
    }

    public MyoCrystalCatalyzerRecipeBuilder output(ItemLike item, int count) {
        return output(ExternalRecipeBuilder.itemId(item), count);
    }

    public MyoCrystalCatalyzerRecipeBuilder energyPerCycle(int energyPerCycle) {
        this.json.addProperty("energyPerCycle", energyPerCycle);
        return this;
    }

    public MyoCrystalCatalyzerRecipeBuilder lightningCost(int lightningCost) {
        this.json.addProperty("lightningCost", lightningCost);
        return this;
    }

    public MyoCrystalCatalyzerRecipeBuilder lightningTier(String lightningTier) {
        this.json.addProperty("lightningTier", lightningTier);
        return this;
    }

    public void save(JsonRecipeProvider.JsonRecipeOutput output) {
        output.accept(this.id, this.json);
    }
}
