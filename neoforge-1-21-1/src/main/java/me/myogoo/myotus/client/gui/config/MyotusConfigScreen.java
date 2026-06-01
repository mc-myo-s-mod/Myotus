package me.myogoo.myotus.client.gui.config;

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
    @Override
    public void buildTab(WidgetContainer widgets, AEBaseScreen<?> screen) {
        showUpgradePanelButtonCheckbox = widgets.addCheckbox("show_upgrade_panel_button",
                TranslateKey.SHOW_UPGRADE_PANEL_BUTTON.getTranslate(), this::save);
        showViewCellSlotCheckbox = widgets.addCheckbox("show_view_cell_button",
                TranslateKey.SHOW_VIEW_CELL_SLOT.getTranslate(), this::save);
        widgets.add("key:open_terminal_setting", new KeyBindingButton(
                TranslateKey.OPEN_TERMINAL_SETTING_KEY.getTranslate(), keys -> {
        }));

        widgets.add("key:toggle_subsidepanel", new KeyBindingButton(
                TranslateKey.TOGGLE_SUB_SIDE_PANEL_KEY.getTranslate(), keys -> {
        }));
        updateState();
    }

    protected void updateState() {
        if (showUpgradePanelButtonCheckbox != null) {
            showUpgradePanelButtonCheckbox.setSelected(MyotusConfig.CLIENT.showUpgradePanelButton.get());
        }
        if (showViewCellSlotCheckbox != null) {
            showViewCellSlotCheckbox.setSelected(MyotusConfig.CLIENT.showViewCellSlots.get());
        }
    }

    protected void save() {
        if (showUpgradePanelButtonCheckbox != null) {
            MyotusConfig.CLIENT.showUpgradePanelButton.set(showUpgradePanelButtonCheckbox.isSelected());
        }
        if (showViewCellSlotCheckbox != null) {
            MyotusConfig.CLIENT.showViewCellSlots.set(showViewCellSlotCheckbox.isSelected());
        }
        MyotusConfig.CLIENT.get().save();
        updateState();
    }

}
