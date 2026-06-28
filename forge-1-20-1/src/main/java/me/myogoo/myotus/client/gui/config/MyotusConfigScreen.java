package me.myogoo.myotus.client.gui.config;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.widgets.AECheckbox;
import me.myogoo.myotus.api.config.MyoConfigTabScreen;
import me.myogoo.myotus.client.TranslateKey;

import appeng.client.gui.WidgetContainer;
import me.myogoo.myotus.client.gui.widgets.KeyBindingButton;
import me.myogoo.myotus.config.MyotusClientConfig;
import net.minecraft.network.chat.Component;

public class MyotusConfigScreen implements MyoConfigTabScreen {
    private AECheckbox showUpgradePanelButtonCheckbox;
    private AECheckbox showViewCellSlotCheckbox;

    @Override
    public void buildTab(WidgetContainer widgets, AEBaseScreen<?> screen) {
        showUpgradePanelButtonCheckbox = widgets.addCheckbox("show_upgrade_panel_button",
                Component.translatable(TranslateKey.GUI.SHOW_UPGRADE_PANEL_BUTTON.key()), this::save);
        showViewCellSlotCheckbox = widgets.addCheckbox("show_view_cell_button",
                Component.translatable(TranslateKey.GUI.SHOW_VIEW_CELL_SLOT.key()).copy()
                        .append(Component.translatable(TranslateKey.GUI.SHOW_VIEW_CELL_SLOT_ONLY.key())),
                this::save);
        showViewCellSlotCheckbox.active = false;
        widgets.add("key:open_terminal_setting", new KeyBindingButton(
                Component.translatable(TranslateKey.KEY.OPEN_TERMINAL_SETTING.key()), keys -> {
        }));
        widgets.add("key:toggle_upgrade_terminal_panel", new KeyBindingButton(
                Component.translatable(TranslateKey.KEY.TOGGLE_UPGRADE_TERMINAL_PANEL.key()), keys -> {
        }));
        updateState();
    }

    protected void updateState() {
        if (showUpgradePanelButtonCheckbox != null) {
            showUpgradePanelButtonCheckbox.setSelected(MyotusClientConfig.CLIENT.showUpgradePanelButton.get());
        }
        if (showViewCellSlotCheckbox != null) {
            showViewCellSlotCheckbox.setSelected(false);
        }
    }

    protected void save() {
        if (showUpgradePanelButtonCheckbox != null) {
            MyotusClientConfig.CLIENT.showUpgradePanelButton.set(showUpgradePanelButtonCheckbox.isSelected());
        }
        MyotusClientConfig.CLIENT.get().save();
        updateState();
    }

}
