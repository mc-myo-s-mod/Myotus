package me.myogoo.myotus.client.gui.widgets;

import appeng.util.Icon;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.IconButton;
import me.myogoo.myotus.api.client.ITerminalWidthScreen;
import me.myogoo.myotus.api.client.TerminalWidthMode;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public final class TerminalWidthToggleButton extends IconButton {
    private final ITerminalWidthScreen widthScreen;

    public static Optional<TerminalWidthToggleButton> forWideStyle(
            ITerminalWidthScreen widthScreen, Optional<ScreenStyle> wideStyle) {
        return wideStyle.map(style -> new TerminalWidthToggleButton(widthScreen));
    }

    private TerminalWidthToggleButton(ITerminalWidthScreen widthScreen) {
        super(button -> widthScreen.setTerminalWidthMode(widthScreen.getTerminalWidthMode().next()));
        this.widthScreen = widthScreen;
    }

    @Override
    protected Icon getIcon() {
        return widthScreen.getTerminalWidthMode() == TerminalWidthMode.COMPACT ? Icon.ARROW_RIGHT : Icon.ARROW_LEFT;
    }

    @Override
    public Component getMessage() {
        var modeKey = widthScreen.getTerminalWidthMode() == TerminalWidthMode.COMPACT
                ? "gui.myotus.terminal_width.compact" : "gui.myotus.terminal_width.wide";
        return Component.translatable("gui.myotus.terminal_width", Component.translatable(modeKey));
    }
}
