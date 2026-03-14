package me.myogoo.myotus.init;

import me.myogoo.myotus.api.config.AE2TerminalConfigTab;
import me.myogoo.myotus.api.config.TerminalConfigTab;
import me.myogoo.myotus.client.TranslateKey;
import me.myogoo.myotus.client.gui.MyoIcon;
import me.myogoo.myotus.client.gui.config.MyotusConfigScreen;

public class MyotusConfigTab {
    public static void initialize() {
        AE2TerminalConfigTab.registerTab(new TerminalConfigTab(
                TranslateKey.TITLE_TERMINAL_SETTING.getTranslate(),
                MyoIcon.MYOTUS_CONFIG,
                "myotus.json",
                new MyotusConfigScreen()
        ));
    }
}
