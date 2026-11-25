package me.myogoo.myotus.config;

import me.myogoo.myotus.api.client.TerminalWidthMode;
import net.minecraftforge.common.ForgeConfigSpec;

public class MyotusClientConfig {
    public static Client CLIENT = new Client();
    public static class Client {
        private final ForgeConfigSpec spec;
        public final ForgeConfigSpec.BooleanValue openSidePanel;
        public final ForgeConfigSpec.BooleanValue showUpgradePanelButton;
        public final ForgeConfigSpec.EnumValue<TerminalWidthMode> terminalWidthMode;

        Client() {
            var builder = new ForgeConfigSpec.Builder();
            builder.push("tab");
            this.openSidePanel = builder
                    .comment("Whether to open the upgrade terminal panel when the terminal is opened.")
                    .define("openSidePanel", false);
            this.showUpgradePanelButton = builder
                    .comment("Whether to show the upgrade panel toggle button in the terminal left toolbar.")
                    .define("showUpgradePanelButton", true);
            builder.pop();

            builder.push("terminal");
            terminalWidthMode = builder
                    .comment("COMPACT uses the native terminal style. WIDE uses its dedicated wide style when available.")
                    .defineEnum("widthMode", TerminalWidthMode.COMPACT);
            builder.pop();

            this.spec = builder.build();
        }

        public ForgeConfigSpec get() {
            return spec;
        }

        public void save() {
            spec.save();
        }

    }
}
