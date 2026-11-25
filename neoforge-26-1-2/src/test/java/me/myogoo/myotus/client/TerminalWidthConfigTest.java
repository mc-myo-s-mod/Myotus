package me.myogoo.myotus.client;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import me.myogoo.myotus.api.client.TerminalWidthMode;
import me.myogoo.myotus.config.MyotusConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TerminalWidthConfigTest {
    @Test
    void settingSavesAndReloadsBothModes(@TempDir Path directory) throws Exception {
        var constructor = MyotusConfig.Client.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        var client = constructor.newInstance();
        var setting = client.terminalWidthMode;
        assertEquals(TerminalWidthMode.COMPACT, setting.getDefault());
        assertFalse(setting.getSpec().test("UNKNOWN"));
        assertTrue(setting.getSpec().test(TerminalWidthMode.COMPACT));
        assertTrue(setting.getSpec().test(TerminalWidthMode.WIDE));

        Path file = directory.resolve("width-client.toml");
        try (var config = CommentedFileConfig.builder(file, TomlFormat.instance()).sync().build()) {
            client.get().correct(config);
            assertEquals(TerminalWidthMode.COMPACT, config.getEnum(setting.getPath(), TerminalWidthMode.class));
            for (var mode : TerminalWidthMode.values()) {
                config.set(setting.getPath(), mode.name());
                config.save();
                try (var reloaded = CommentedFileConfig.of(file, TomlFormat.instance())) {
                    reloaded.load();
                    assertEquals(mode, reloaded.getEnum(setting.getPath(), TerminalWidthMode.class));
                    assertTrue(setting.getSpec().test(mode));
                }
            }
        }
    }
}
