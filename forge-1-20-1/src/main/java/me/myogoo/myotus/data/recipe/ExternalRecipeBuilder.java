package me.myogoo.myotus.data.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.myogoo.myotus.api.annotation.MyoMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.annotation.Annotation;

public final class ExternalRecipeBuilder {
    private ExternalRecipeBuilder() {
    }

    public static JsonArray conditions(String modId) {
        JsonArray conditions = new JsonArray();
        conditions.add(condition("forge:mod_loaded", "modid", modId));
        return conditions;
    }

    public static JsonArray devConditions() {
        JsonArray conditions = new JsonArray();
        conditions.add(devCondition());
        return conditions;
    }

    public static JsonObject devCondition() {
        JsonObject dev = new JsonObject();
        dev.addProperty("type", "myotus:dev");
        return dev;
    }

    public static String myoConditionId(Class<? extends Annotation> annotationClass) {
        MyoMod myoMod = annotationClass.getAnnotation(MyoMod.class);
        if (myoMod == null) {
            throw new IllegalArgumentException(annotationClass.getName() + " is not annotated with @MyoMod");
        }
        String alias = myoMod.alias();
        return alias == null || alias.isBlank() ? myoMod.value() : alias;
    }

    public static String myoConditionId(Annotation annotation) {
        return myoConditionId(annotation.annotationType());
    }

    public static JsonArray myoConditions(Class<? extends Annotation> annotationClass) {
        JsonArray conditions = new JsonArray();
        conditions.add(myoCondition(annotationClass));
        return conditions;
    }

    public static JsonArray myoConditions(Annotation annotation) {
        JsonArray conditions = new JsonArray();
        conditions.add(myoCondition(annotation));
        return conditions;
    }

    public static JsonArray myoConditions(String activeMod) {
        JsonArray conditions = new JsonArray();
        conditions.add(myoCondition(activeMod));
        return conditions;
    }

    public static JsonObject myoCondition(Class<? extends Annotation> annotationClass) {
        return myoCondition(myoConditionId(annotationClass));
    }

    public static JsonObject myoCondition(Annotation annotation) {
        return myoCondition(myoConditionId(annotation));
    }

    public static JsonObject myoCondition(String activeMod) {
        JsonObject condition = new JsonObject();
        condition.addProperty("type", "myotus:mod_condition");
        condition.addProperty("active_mod", activeMod);
        return condition;
    }

    public static JsonObject item(String item) {
        JsonObject json = new JsonObject();
        json.addProperty("item", item);
        return json;
    }

    public static JsonObject item(ItemLike item) {
        return item(itemId(item));
    }

    public static JsonElement ingredient(Ingredient ingredient) {
        return ingredient.toJson();
    }

    public static JsonElement ingredient(String item) {
        return item(item);
    }

    public static JsonElement ingredient(ItemLike item) {
        return ingredient(Ingredient.of(item));
    }

    public static String itemId(ItemLike item) {
        return ForgeRegistries.ITEMS.getKey(item.asItem()).toString();
    }

    public static JsonObject tag(String tag) {
        JsonObject json = new JsonObject();
        json.addProperty("tag", tag);
        return json;
    }

    public static JsonObject fluid(String fluid) {
        JsonObject json = new JsonObject();
        json.addProperty("fluid", fluid);
        return json;
    }

    public static JsonObject fluidStack(String fluid, int amount) {
        JsonObject stack = new JsonObject();
        stack.addProperty("Amount", amount);
        stack.addProperty("FluidName", fluid);
        JsonObject json = new JsonObject();
        json.add("fluidStack", stack);
        return json;
    }

    public static JsonObject stack(String item, int count) {
        JsonObject json = new JsonObject();
        json.addProperty("count", count);
        json.addProperty("item", item);
        return json;
    }

    public static JsonObject stack(ItemLike item, int count) {
        return stack(itemId(item), count);
    }

    public static JsonObject aeStack(String id, int amount) {
        JsonObject json = new JsonObject();
        json.addProperty("#", amount);
        json.addProperty("#c", "ae2:i");
        json.addProperty("id", id);
        return json;
    }

    public static JsonObject aeStack(ItemLike item, int amount) {
        return aeStack(itemId(item), amount);
    }

    public static CountedIngredient counted(JsonElement ingredient, int amount) {
        return new CountedIngredient(ingredient, amount);
    }

    public static RecipeJsonBuilder shaped(String type, String result, int count) {
        return new RecipeJsonBuilder(type, result, count);
    }

    public static RecipeJsonBuilder shapeless(String type, String result, int count) {
        return new RecipeJsonBuilder(type, result, count).ingredients();
    }

    private static JsonObject condition(String type, String key, String value) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        json.addProperty(key, value);
        return json;
    }

    public record CountedIngredient(JsonElement ingredient, int amount) {
        public JsonObject toAmountJson() {
            JsonObject json = new JsonObject();
            json.addProperty("amount", amount);
            json.add("ingredient", ingredient);
            return json;
        }
    }

    public static final class RecipeJsonBuilder {
        private final JsonObject recipe = new JsonObject();

        private RecipeJsonBuilder(String type, String result, int count) {
            recipe.addProperty("type", type);
            recipe.add("result", stack(result, count));
        }

        public RecipeJsonBuilder conditions(JsonArray conditions) {
            recipe.add("conditions", conditions);
            return this;
        }

        public RecipeJsonBuilder pattern(String row) {
            JsonArray pattern = array("pattern");
            pattern.add(row);
            return this;
        }

        public RecipeJsonBuilder define(char key, JsonObject ingredient) {
            JsonObject keyJson = object("key");
            keyJson.add(String.valueOf(key), ingredient);
            return this;
        }

        public RecipeJsonBuilder requires(JsonElement ingredient) {
            JsonArray ingredients = array("ingredients");
            ingredients.add(ingredient);
            return this;
        }

        public void save(JsonRecipeProvider.JsonRecipeOutput output, ResourceLocation id) {
            output.accept(id, recipe);
        }

        private RecipeJsonBuilder ingredients() {
            array("ingredients");
            return this;
        }

        private JsonArray array(String key) {
            if (!recipe.has(key)) {
                recipe.add(key, new JsonArray());
            }
            return recipe.getAsJsonArray(key);
        }

        private JsonObject object(String key) {
            if (!recipe.has(key)) {
                recipe.add(key, new JsonObject());
            }
            return recipe.getAsJsonObject(key);
        }
    }

}
