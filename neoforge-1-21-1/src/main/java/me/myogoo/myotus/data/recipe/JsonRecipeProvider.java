package me.myogoo.myotus.data.recipe;

import com.google.gson.JsonObject;
import me.myogoo.myotus.Myotus;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class JsonRecipeProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    protected JsonRecipeProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
    }

    protected abstract void buildRecipes(JsonRecipeOutput output);

    @Override
    public final @NotNull CompletableFuture<?> run(@NotNull CachedOutput output) {
        Map<ResourceLocation, JsonObject> recipes = new LinkedHashMap<>();
        this.buildRecipes((id, recipe) -> recipes.put(id, recipe));

        return CompletableFuture.allOf(recipes.entrySet().stream()
                .map(entry -> DataProvider.saveStable(output, entry.getValue(), this.pathProvider.json(entry.getKey())))
                .toArray(CompletableFuture[]::new));
    }

    protected static ResourceLocation id(String path) {
        return Myotus.makeId(path);
    }

    @FunctionalInterface
    public interface JsonRecipeOutput {
        void accept(ResourceLocation id, JsonObject recipe);
    }
}
