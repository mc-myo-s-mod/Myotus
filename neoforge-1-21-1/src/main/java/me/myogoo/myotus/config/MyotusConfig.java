package me.myogoo.myotus.config;

import me.myogoo.myotus.menu.PlayerUpgradeContainer;
import me.myogoo.myotus.api.client.TerminalWidthMode;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

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
        public final ModConfigSpec.IntValue terminalUpgradeSlots;
        public final ModConfigSpec.IntValue defaultFluidMbPerExperience;
        public final ModConfigSpec.ConfigValue<List<? extends String>> fluidMbPerExperienceOverrides;

        private Server() {
            var builder = new ModConfigSpec.Builder();
            builder.push("terminal");
            terminalUpgradeSlots = builder
                    .comment("Number of Myotus terminal upgrade slots. Five or fewer slots hide the scrollbar.",
                            "Change only while the server is stopped (or the singleplayer world is closed), then restart.",
                            "Cards beyond this limit remain saved but inactive; raise the limit to access them again.")
                    .worldRestart()
                    .defineInRange("terminalUpgradeSlots", 5, 1, PlayerUpgradeContainer.SIZE);
            builder.pop();
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
        private final Set<ModConfigSpec.ConfigValue<List<Integer>>> additionalPanelPositions = new LinkedHashSet<>();
        public final ModConfigSpec.BooleanValue openSidePanel;
        public final ModConfigSpec.BooleanValue showUpgradePanelButton;
        public final ModConfigSpec.BooleanValue showViewCellSlots;
        public final ModConfigSpec.BooleanValue animatePanelMovement;
        public final ModConfigSpec.BooleanValue snapPanelMovement;
        public final ModConfigSpec.EnumValue<TerminalWidthMode> terminalWidthMode;

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

            builder.push("terminalModules");
            animatePanelMovement = builder
                    .comment("Whether to animate large terminal panel relocations.")
                    .define("animatePanelMovement", true);
            snapPanelMovement = builder
                    .comment("Snap dragged module panels to nearby terminal and module edges.")
                    .define("snapPanelMovement", true);
            builder.pop();

            builder.push("terminal");
            terminalWidthMode = builder
                    .comment("COMPACT uses the native terminal style. WIDE uses its dedicated wide style when available.")
                    .defineEnum("widthMode", TerminalWidthMode.COMPACT);
            builder.pop();

            this.spec = builder.build();
        }

        public ModConfigSpec get() {
            return spec;
        }

        public void save() {
            spec.save();
        }

        public void registerTerminalModulePosition(ModConfigSpec.ConfigValue<List<Integer>> savedPosition) {
            additionalPanelPositions.add(Objects.requireNonNull(savedPosition, "savedPosition"));
        }

        public void resetPanelPositions() {
            for (var position : additionalPanelPositions) {
                position.set(List.of());
            }
            for (var position : additionalPanelPositions) {
                position.save();
            }
        }
    }
}
