package me.myogoo.myotus.init;

import me.myogoo.myotus.config.MyotusClientConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;

public class MyoConfig {
    public static void initialize(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, MyotusClientConfig.CLIENT.get(), "myotus-client.toml");
    }
}
