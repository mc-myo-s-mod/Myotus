package me.myogoo.myotus.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class MyotusClientConfig {
    public static Client CLIENT = new Client();
    public static class Client {
        private final ForgeConfigSpec spec;
        public final ForgeConfigSpec.BooleanValue openSidePanel;
        public final ForgeConfigSpec.BooleanValue showUpgradePanelButton;

        Client() {
            var builder = new ForgeConfigSpec.Builder();
            builder.push("tab");
            this.openSidePanel = builder
                    .comment("Whether to open the side panel when the terminal is opened.")
                    .define("openSidePanel", false);
            this.showUpgradePanelButton = builder
                    .comment("Whether to show the upgrade panel toggle button in the terminal left toolbar.")
                    .define("showUpgradePanelButton", true);
            builder.pop();

            this.spec = builder.build();
        }

        public ForgeConfigSpec get() {
            return spec;
        }

    }
}
