package me.myogoo.myotus.init;

import me.myogoo.myotus.config.MyotusClientConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class MyoConfig {
    public static void initialize() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, MyotusClientConfig.CLIENT.get(),
                "myotus-client.toml");
    }
}
