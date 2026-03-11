package me.myogoo.myotus.client.gui.widgets.button;

import me.myogoo.myotus.client.gui.MyoIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class MyoReportButton extends CustomImageButton {
    public MyoReportButton() {
        super(MyoIcon.BUG_REPORT, button -> openBugReport());
    }

    @Override
    public List<Component> getTooltipMessage() {
        return Collections.singletonList(Component.translatable("gui.myotus.button.bug_report.tooltip"));
    }

    private static void openBugReport() {
        Minecraft.getInstance().keyboardHandler
                .setClipboard("https://github.com/myogoo/MyoCertus/issues");
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(
                    Component.literal("Bug report URL copied to clipboard!"), false);
        }
    }
}
