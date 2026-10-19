package me.myogoo.myotus.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class MyotusConfig {
    public static final Client CLIENT = new Client();
    public static final Server SERVER = new Server();

    private MyotusConfig() {
    }

    public static ExperienceFluidRates getExperienceFluidRates() {
        return SERVER.spec.isLoaded()
                ? ExperienceFluidRates.parse(SERVER.defaultFluidMbPerExperience.get(),
                        SERVER.fluidMbPerExperienceOverrides.get())
                : ExperienceFluidRates.defaults();
    }

    public static final class Server {
        private final ModConfigSpec spec;
        public final ModConfigSpec.IntValue defaultFluidMbPerExperience;
        public final ModConfigSpec.ConfigValue<List<? extends String>> fluidMbPerExperienceOverrides;

        private Server() {
            var builder = new ModConfigSpec.Builder();
            builder.push("experience");
            defaultFluidMbPerExperience = builder
                    .comment("Default mB required for one raw XP point from recognized experience fluids.")
                    .worldRestart()
                    .defineInRange("defaultFluidMbPerExperience", ExperienceFluidRates.DEFAULT_RATE, 1, Integer.MAX_VALUE);
            fluidMbPerExperienceOverrides = builder
                    .comment("Fluid ID overrides in the form namespace:fluid=positive_mB_per_XP.",
                            "Listed fluids are recognized even without an experience-fluid tag.",
                            "Later entries for the same ID win. An empty list removes all exceptions.")
                    .worldRestart()
                    .defineListAllowEmpty("fluidMbPerExperienceOverrides", ExperienceFluidRates.DEFAULT_OVERRIDES,
                            ExperienceFluidRates::isValidOverride);
            builder.pop();
            spec = builder.build();
        }

        public ModConfigSpec get() {
            return spec;
        }
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
