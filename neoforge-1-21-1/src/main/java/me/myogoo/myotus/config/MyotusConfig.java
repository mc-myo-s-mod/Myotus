package me.myogoo.myotus.config;

import me.myogoo.myotus.menu.PlayerUpgradeContainer;
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

    public static final class Server {
        private final ModConfigSpec spec;
        public final ModConfigSpec.IntValue terminalUpgradeSlots;

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
