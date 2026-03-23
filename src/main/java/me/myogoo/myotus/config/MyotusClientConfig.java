package me.myogoo.myotus.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class MyotusClientConfig {
    public static Client CLIENT = new Client();
    public static class Client {
        private final ModConfigSpec spec;
        public final ModConfigSpec.BooleanValue openSidePanel;

        Client() {
            var builder = new ModConfigSpec.Builder();
            builder.push("tab");
            this.openSidePanel = builder
                    .comment("Whether to open the side panel when the terminal is opened.")
                    .define("openSidePanel", false);
            builder.pop();

            this.spec = builder.build();
        }

        public ModConfigSpec get() {
            return spec;
        }

    }
}
