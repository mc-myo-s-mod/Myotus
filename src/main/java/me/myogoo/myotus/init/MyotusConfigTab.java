package me.myogoo.myotus.init;

import me.myogoo.myotus.api.config.AE2TerminalConfigTab;
import me.myogoo.myotus.api.config.TerminalConfigTab;
import me.myogoo.myotus.client.gui.MyoIcon;
import me.myogoo.myotus.client.gui.config.MyotusConfigScreen;
import net.minecraft.network.chat.Component;

public class MyotusConfigTab {
    public static void initialize() {
        AE2TerminalConfigTab.registerTab(new TerminalConfigTab(
                MyoIcon.MYOTUS_CONFIG.getBlitter(),
                Component.translatable("gui.myotus.config.title"),
                "myotus.json",
                new MyotusConfigScreen()
        ));
    }
}
