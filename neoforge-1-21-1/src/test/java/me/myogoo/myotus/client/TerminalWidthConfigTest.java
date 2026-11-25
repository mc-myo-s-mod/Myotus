package me.myogoo.myotus.client;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import me.myogoo.myotus.api.client.TerminalWidthMode;
import me.myogoo.myotus.config.MyotusConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TerminalWidthConfigTest {
    @Test
    void settingSavesAndReloadsBothModes(@TempDir Path directory) throws Exception {
        var constructor = MyotusConfig.Client.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        var client = constructor.newInstance();
        var setting = client.terminalWidthMode;
        assertEquals(TerminalWidthMode.COMPACT, setting.getDefault());
        assertFalse(setting.getSpec().test("UNKNOWN"));

        Path file = directory.resolve("width-client.toml");
        var configs = new ConfigTracker();
        var container = mock(ModContainer.class);
        when(container.getModId()).thenReturn("myotus_width_test");
        configs.registerConfig(ModConfig.Type.CLIENT, client.get(), container, "width-client.toml");
        configs.loadConfigs(ModConfig.Type.CLIENT, directory);
        try {
            for (var mode : TerminalWidthMode.values()) {
                setting.set(mode);
                client.save();
                try (var reloaded = CommentedFileConfig.of(file, TomlFormat.instance())) {
                    reloaded.load();
                    assertEquals(mode.name(), reloaded.get(setting.getPath()));
                }
            }
        } finally {
            configs.unloadConfigs(ModConfig.Type.CLIENT);
        }
    }
}
