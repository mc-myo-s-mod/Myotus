package me.myogoo.myotus.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class MyotusConfig {
    public static final Client CLIENT = new Client();

    private MyotusConfig() {
    }

    public static final class Client {
        private final ModConfigSpec spec;
        public final ModConfigSpec.BooleanValue openSidePanel;
        public final ModConfigSpec.BooleanValue showUpgradePanelButton;
        public final ModConfigSpec.BooleanValue showViewCellSlots;

        private Client() {
            var builder = new ModConfigSpec.Builder();
            builder.push("tab");
            this.openSidePanel = builder
                    .comment("Whether to open the upgrade terminal panel when the terminal is opened.")
                    .define("openSidePanel", false);
            this.showUpgradePanelButton = builder
                    .comment("Whether to show the upgrade panel toggle button in the terminal left toolbar.")
                    .define("showUpgradePanelButton", true);
            this.showViewCellSlots = builder
                    .comment("Whether terminals should expose view cell slots.")
                    .define("showViewCellSlots", true);
            builder.pop();

            this.spec = builder.build();
        }

        public ModConfigSpec get() {
            return spec;
        }

        public void save() {
            spec.save();
        }
    }
}
