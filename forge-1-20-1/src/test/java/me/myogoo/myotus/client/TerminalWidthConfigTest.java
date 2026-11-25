package me.myogoo.myotus.client;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import me.myogoo.myotus.api.client.TerminalWidthMode;
import me.myogoo.myotus.config.MyotusClientConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TerminalWidthConfigTest {
    @Test
    void settingSavesAndReloadsBothModes(@TempDir Path directory) throws Exception {
        var constructor = MyotusClientConfig.Client.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        var client = constructor.newInstance();
        var setting = client.terminalWidthMode;
        assertEquals(TerminalWidthMode.COMPACT, setting.getDefault());

        Path file = directory.resolve("width-client.toml");
        try (var config = CommentedFileConfig.builder(file, TomlFormat.instance()).sync().build()) {
            config.load();
            client.get().setConfig(config);
            for (var mode : TerminalWidthMode.values()) {
                setting.set(mode);
                client.save();
                try (var reloaded = CommentedFileConfig.of(file, TomlFormat.instance())) {
                    reloaded.load();
                    assertEquals(mode.name(), reloaded.get(setting.getPath()));
                }
            }
        }
    }
}
