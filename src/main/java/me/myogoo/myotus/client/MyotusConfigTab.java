package me.myogoo.myotus.client;

import me.myogoo.myotus.api.config.MyoConfigTab;
import me.myogoo.myotus.client.gui.MyoIcon;
import me.myogoo.myotus.client.gui.config.MyotusConfigScreen;
import me.myogoo.myotus.impl.ConfigManager;

public final class MyotusConfigTab {
    private MyotusConfigTab() {
    }

    public static void initialize() {
        ConfigManager.INSTANCE.registerTab(new MyoConfigTab(
                TranslateKey.TITLE_TERMINAL_SETTING.getTranslate(),
                MyoIcon.MYOTUS_CONFIG,
                "myotus.json",
                new MyotusConfigScreen()));
    }
}
