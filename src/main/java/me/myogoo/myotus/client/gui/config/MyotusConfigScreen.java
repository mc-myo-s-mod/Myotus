package me.myogoo.myotus.client.gui.config;

import me.myogoo.myotus.api.config.MyoConfigTabScreen;
import me.myogoo.myotus.client.TranslateKey;

import appeng.client.gui.WidgetContainer;
import me.myogoo.myotus.client.gui.widgets.KeyBindingButton;
import me.myogoo.myotus.config.MyotusClientConfig;

public class MyotusConfigScreen implements MyoConfigTabScreen {

    // private AECheckbox configTabSorting;
    private KeyBindingButton keyBindingButton;

    @Override
    public void buildTab(WidgetContainer widgets, appeng.client.gui.AEBaseScreen<?> screen) {
        // configTabSorting = widgets.addCheckbox("sort",
        // TranslateKey.CONFIG_TAB_SORTING.getTranslate(),
        // this::updateConfigTabSorting);
        keyBindingButton = new KeyBindingButton(
                TranslateKey.OPEN_TERMINAL_SETTING_KEY.getTranslate(), keys -> {
                });
        widgets.add("key_binding", keyBindingButton);
    }

    protected void updateState() {
        // configTabSorting.setSelected(MyotusClientConfig.INSTANCE.activeTabSorting.get());
    }

    protected void save() {
        MyotusClientConfig.INSTANCE.spec.save();
        updateState();
    }

    private void updateConfigTabSorting() {
        MyotusClientConfig.INSTANCE.activeTabSorting.set(!MyotusClientConfig.INSTANCE.activeTabSorting.get());
        save();
    }
}