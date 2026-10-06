package me.myogoo.myotus.client.gui.config;

import net.minecraft.network.chat.Component;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.widgets.AECheckbox;
import me.myogoo.myotus.api.config.MyoConfigTabScreen;
import me.myogoo.myotus.client.TranslateKey;

import appeng.client.gui.WidgetContainer;
import me.myogoo.myotus.client.gui.widgets.KeyBindingButton;
import me.myogoo.myotus.config.MyotusConfig;

public class MyotusConfigScreen implements MyoConfigTabScreen {
    private AECheckbox showUpgradePanelButtonCheckbox;
    private AECheckbox showViewCellSlotCheckbox;
    private AECheckbox animatePanelMovementCheckbox;
    private AECheckbox snapPanelMovementCheckbox;
    @Override
    public void buildTab(WidgetContainer widgets, AEBaseScreen<?> screen) {
        showUpgradePanelButtonCheckbox = widgets.addCheckbox("show_upgrade_panel_button",
                Component.translatable(TranslateKey.GUI.SHOW_UPGRADE_PANEL_BUTTON.key()), this::save);
        showViewCellSlotCheckbox = widgets.addCheckbox("show_view_cell_button",
                Component.translatable(TranslateKey.GUI.SHOW_VIEW_CELL_SLOT.key()), this::save);
        widgets.add("key:open_terminal_setting", new KeyBindingButton(
                Component.translatable(TranslateKey.KEY.OPEN_TERMINAL_SETTING.key()), keys -> {
        }));

        widgets.add("key:toggle_upgrade_terminal_panel", new KeyBindingButton(
                Component.translatable(TranslateKey.KEY.TOGGLE_UPGRADE_TERMINAL_PANEL.key()), keys -> {
        }));
        widgets.addButton("reset_positions", Component.translatable("gui.myotus.terminal_module.reset_positions"),
                MyotusConfig.CLIENT::resetPanelPositions);
        animatePanelMovementCheckbox = widgets.addCheckbox("animate_panel_movement",
                Component.translatable("gui.myotus.terminal_module.animate_movement"), this::save);
        snapPanelMovementCheckbox = widgets.addCheckbox("snap_panel_movement",
                Component.translatable("gui.myotus.terminal_module.snap_movement"), this::save);
        updateState();
    }

    protected void updateState() {
        if (showUpgradePanelButtonCheckbox != null) {
            showUpgradePanelButtonCheckbox.setSelected(MyotusConfig.CLIENT.showUpgradePanelButton.get());
        }
        if (showViewCellSlotCheckbox != null) {
            showViewCellSlotCheckbox.setSelected(MyotusConfig.CLIENT.showViewCellSlots.get());
        }
        if (animatePanelMovementCheckbox != null) {
            animatePanelMovementCheckbox.setSelected(MyotusConfig.CLIENT.animatePanelMovement.get());
        }
        if (snapPanelMovementCheckbox != null) {
            snapPanelMovementCheckbox.setSelected(MyotusConfig.CLIENT.snapPanelMovement.get());
        }
    }

    protected void save() {
        if (showUpgradePanelButtonCheckbox != null) {
            MyotusConfig.CLIENT.showUpgradePanelButton.set(showUpgradePanelButtonCheckbox.isSelected());
        }
        if (showViewCellSlotCheckbox != null) {
            MyotusConfig.CLIENT.showViewCellSlots.set(showViewCellSlotCheckbox.isSelected());
        }
        if (animatePanelMovementCheckbox != null) {
            MyotusConfig.CLIENT.animatePanelMovement.set(animatePanelMovementCheckbox.isSelected());
        }
        if (snapPanelMovementCheckbox != null) {
            MyotusConfig.CLIENT.snapPanelMovement.set(snapPanelMovementCheckbox.isSelected());
        }
        MyotusConfig.CLIENT.get().save();
        updateState();
    }

}
