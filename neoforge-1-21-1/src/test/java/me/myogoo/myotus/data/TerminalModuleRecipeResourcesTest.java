package me.myogoo.myotus.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class TerminalModuleRecipeResourcesTest {
    @Test
    void builtInModuleItemsAndResourcesAreAbsent() {
        for (String module : List.of("armor_module", "offhand_module", "equipment_module", "curios_module")) {
            assertFalse(BuiltInRegistries.ITEM.containsKey(ResourceLocation.fromNamespaceAndPath("myotus", module)),
                    "Built-in module item must not be registered: " + module);
            for (String path : List.of(
                    "/data/myotus/recipe/crafting/" + module + ".json",
                    "/data/myotus/advancement/recipes/misc/crafting/" + module + ".json",
                    "/assets/myotus/models/item/" + module + ".json")) {
                assertNull(getClass().getResource(path),
                        "Removed resource remains; run :neoforge-1-21-1:runData: " + path);
            }
        }
        for (String language : List.of("", "_ko_kr/")) {
            assertNull(getClass().getResource("/assets/myotus/ae2guide/" + language + "feature/terminal-modules.md"));
        }
    }
}
