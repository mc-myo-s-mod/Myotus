package me.myogoo.myotus.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class MyotusClientConfig {
    public static Client CLIENT = new Client();
    public static class Client {
        private final ForgeConfigSpec spec;
        public final ForgeConfigSpec.BooleanValue openSidePanel;

        Client() {
            var builder = new ForgeConfigSpec.Builder();
            builder.push("tab");
            this.openSidePanel = builder
                    .comment("Whether to open the side panel when the terminal is opened.")
                    .define("openSidePanel", false);
            builder.pop();

            this.spec = builder.build();
        }

        public ForgeConfigSpec get() {
            return spec;
        }

    }
}
