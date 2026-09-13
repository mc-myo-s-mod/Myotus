package me.myogoo.myotus.client;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClientItemResourceTest {
    @Test
    void everyMyotusItemHasAClientDefinitionAndSharedModel() throws Exception {
        for (String item : List.of("compat_processor", "printed_compat_processor", "compat_press",
                "charged_ender_pearl", "ender_pearl_block", "charged_ender_pearl_block", "myotus_upgrade_card")) {
            String definition = "/assets/myotus/items/" + item + ".json";
            try (var stream = getClass().getResourceAsStream(definition)) {
                assertNotNull(stream, definition);
                var model = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                        .getAsJsonObject().getAsJsonObject("model");
                assertEquals("minecraft:model", model.get("type").getAsString(), item);
                assertEquals("myotus:item/" + item, model.get("model").getAsString(), item);
            }
            assertNotNull(getClass().getResource("/assets/myotus/models/item/" + item + ".json"), item);
        }
    }
}
