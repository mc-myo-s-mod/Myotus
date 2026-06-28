package me.myogoo.myotus.client.gui.widgets.button;

import me.myogoo.myotus.client.TranslateKey;
import me.myogoo.myotus.client.gui.MyoIcon;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.net.URI;
import java.util.Collections;
import java.util.List;

public class MyoReportButton extends CustomImageButton {
    private static final URI ISSUE_URI = URI.create("https://github.com/mc-myo-s-mod/Myotus/issues");

    public MyoReportButton() {
        super(MyoIcon.BUG_REPORT, button -> openBugReport());
    }

    @Override
    public List<Component> getTooltipMessage() {
        return Collections.singletonList(Component.translatable(TranslateKey.GUI.TOOLTIP_REPORT_BUG.key()));
    }

    private static void openBugReport() {
        Minecraft minecraft = Minecraft.getInstance();
        Screen currentScreen = minecraft.screen;

        if (currentScreen != null) {
            ConfirmLinkScreen.confirmLinkNow(ISSUE_URI.toString(), currentScreen, true);
        } else {
            Util.getPlatform().openUri(ISSUE_URI);
        }
    }
}
