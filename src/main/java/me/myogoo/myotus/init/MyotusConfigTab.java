package me.myogoo.myotus.init;

import me.myogoo.myotus.client.TranslateKey;
import me.myogoo.myotus.impl.ConfigManager;
import me.myogoo.myotus.api.config.MyoConfigTab;
import me.myogoo.myotus.client.gui.MyoIcon;
import me.myogoo.myotus.client.gui.config.MyotusConfigScreen;

public class MyotusConfigTab {
    public static void initialize() {
        ConfigManager.INSTANCE.registerTab(new MyoConfigTab(
                TranslateKey.TITLE_TERMINAL_SETTING.getTranslate(),
                MyoIcon.MYOTUS_CONFIG,
                "myotus.json",
                new MyotusConfigScreen()));
    }
}
