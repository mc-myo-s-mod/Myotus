package me.myogoo.myotus.client;

import java.util.List;
import java.util.Set;
import java.nio.file.Path;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import me.myogoo.myotus.api.MyotusAPI;
import me.myogoo.myotus.config.MyotusConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TerminalPanelPositionConfigTest {
    @Test
    void movementOptionsDefaultToTrueAndRoundTripThroughToml(@TempDir Path directory) {
        Path file = directory.resolve("myotus-client.toml");
        for (var setting : List.of(MyotusConfig.CLIENT.animatePanelMovement, MyotusConfig.CLIENT.snapPanelMovement)) {
            assertTrue(setting.getDefault());
            assertTrue(setting.getSpec().test(true));
            assertTrue(setting.getSpec().test(false));
            for (boolean enabled : List.of(true, false)) {
                try (var config = CommentedFileConfig.builder(file, TomlFormat.instance()).sync().build()) {
                    config.set(setting.getPath(), enabled);
                    config.save();
                }
                try (var config = CommentedFileConfig.of(file, TomlFormat.instance())) {
                    config.load();
                    Object deserialized = config.get(setting.getPath());
                    assertEquals(enabled, deserialized);
                    assertTrue(setting.getSpec().test(deserialized));
                }
            }
        }
    }

    @Test
    void resetClearsRegisteredPositionsAndSavesAddonConfig(@TempDir Path directory)
            throws ReflectiveOperationException {
        var spec = MyotusConfig.CLIENT.get();
        var addonBuilder = new ModConfigSpec.Builder();
        var addonValues = List.of("equipment", "curios", "crafting").stream()
                .map(name -> addonBuilder.define(name, List.<Integer>of(),
                        value -> value instanceof List<?> position && (position.isEmpty()
                                || position.size() == 2 && position.stream().allMatch(Integer.class::isInstance))))
                .toList();
        var addonOption = addonBuilder.define("otherOption", true);
        var addonSpec = addonBuilder.build();
        var addonPositions = addonValues.stream().map(value -> spy(value)).toList();
        var addonConfigs = new ConfigTracker();
        var addonContainer = mock(ModContainer.class);
        when(addonContainer.getModId()).thenReturn("myotus_addon_position_test");
        addonConfigs.registerConfig(ModConfig.Type.CLIENT, addonSpec, addonContainer, "addon-client.toml");
        // Restore the process-wide registry after exercising the public API.
        var registryField = MyotusConfig.Client.class.getDeclaredField("additionalPanelPositions");
        registryField.setAccessible(true);
        var registeredPositions = (Set<?>) registryField.get(MyotusConfig.CLIENT);
        var configs = new ConfigTracker();
        boolean configWasLoaded = spec.isLoaded();
        if (!configWasLoaded) {
            var container = mock(ModContainer.class);
            when(container.getModId()).thenReturn("myotus_position_test");
            configs.registerConfig(ModConfig.Type.SERVER, spec, container);
            configs.loadDefaultServerConfigs();
        }
        boolean showViewCells = MyotusConfig.CLIENT.showViewCellSlots.get();
        boolean snapMovement = MyotusConfig.CLIENT.snapPanelMovement.get();
        Path addonFile = directory.resolve("addon-client.toml");
        try {
            assertThrows(NullPointerException.class,
                    () -> MyotusAPI.Client.registerTerminalModulePosition(null));
            for (var position : addonPositions) {
                MyotusAPI.Client.registerTerminalModulePosition(position);
                MyotusAPI.Client.registerTerminalModulePosition(position);
            }
            assertFalse(addonSpec.isLoaded());
            assertFalse(java.nio.file.Files.exists(addonFile));
            addonConfigs.loadConfigs(ModConfig.Type.CLIENT, directory);
            for (var position : addonPositions) {
                position.set(List.of(-200, 80));
            }
            addonOption.set(false);
            addonSpec.save();
            MyotusConfig.CLIENT.resetPanelPositions();
            assertEquals(showViewCells, MyotusConfig.CLIENT.showViewCellSlots.get());
            assertEquals(snapMovement, MyotusConfig.CLIENT.snapPanelMovement.get());
            for (var position : addonPositions) {
                assertEquals(List.of(), position.get());
                verify(position).save();
            }
            try (var reloaded = CommentedFileConfig.of(addonFile, TomlFormat.instance())) {
                reloaded.load();
                for (var position : addonPositions) {
                    assertEquals(List.of(), reloaded.get(position.getPath()));
                }
                assertFalse(reloaded.<Boolean>get(addonOption.getPath()));
            }
        } finally {
            registeredPositions.removeAll(addonPositions);
            addonConfigs.unloadConfigs(ModConfig.Type.CLIENT);
            if (!configWasLoaded) {
                configs.unloadConfigs(ModConfig.Type.SERVER);
            }
        }
    }
}
