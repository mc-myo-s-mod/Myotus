package me.myogoo.myotus.client.gui.config;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.widgets.AECheckbox;
import me.myogoo.myotus.api.config.MyoConfigTabScreen;
import me.myogoo.myotus.client.TranslateKey;

import appeng.client.gui.WidgetContainer;
import me.myogoo.myotus.client.gui.widgets.KeyBindingButton;
import me.myogoo.myotus.config.MyotusClientConfig;

public class MyotusConfigScreen implements MyoConfigTabScreen {
    @Override
    public void buildTab(WidgetContainer widgets, AEBaseScreen<?> screen) {
        widgets.add("key:open_terminal_setting", new KeyBindingButton(
                TranslateKey.OPEN_TERMINAL_SETTING_KEY.getTranslate(), keys -> {
        }));
        widgets.add("key:toggle_subsidepanel", new KeyBindingButton(
                TranslateKey.TOGGLE_SUB_SIDE_PANEL_KEY.getTranslate(), keys -> {
        }));
    }

    protected void updateState() {
    }

    protected void save() {
        MyotusClientConfig.CLIENT.get().save();
        updateState();
    }

}