package me.myogoo.myotus.client;

import me.myogoo.myotus.Myotus;
import me.myogoo.myotus.api.config.MyoConfigTab;
import me.myogoo.myotus.client.gui.MyoIcon;
import me.myogoo.myotus.client.gui.config.MyotusConfigScreen;
import me.myogoo.myotus.impl.ConfigManager;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public class MyotusConfigTab {
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(MyotusConfigTab::initialize);
    }

    public static void initialize() {
        ConfigManager.INSTANCE.registerTab(new MyoConfigTab(
                Myotus.makeId("config"),
                TranslateKey.TITLE_TERMINAL_SETTING.getTranslate(),
                MyoIcon.MYOTUS_CONFIG,
                "myotus.json",
                new MyotusConfigScreen()));
    }
}
