package me.myogoo.myotus.api.config;

import appeng.client.gui.style.Blitter;
import me.myogoo.myotus.client.gui.config.BaseConfigTabScreen;
import me.myogoo.myotus.client.gui.widgets.button.CustomTabButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * A record containing tab information for the Terminal Config screen.
 */

public record TerminalConfigTab(Blitter blitter, Component title, String stylePath, BaseConfigTabScreen configTabScreen) {
    public CustomTabButton getTabButton(Button.OnPress onPress) {
        return new CustomTabButton(blitter, title, onPress);
    }
}
