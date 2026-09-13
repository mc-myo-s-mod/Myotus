package me.myogoo.myotus.init;

import me.myogoo.myotus.config.MyotusConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;

public class MyoConfig {
    public static void initialize(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, MyotusConfig.CLIENT.get(), "myotus-client.toml");
    }
}
