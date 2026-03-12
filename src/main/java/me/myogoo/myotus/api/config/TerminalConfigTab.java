package me.myogoo.myotus.api.config;

import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;
import me.myogoo.myotus.client.gui.MyoIcon;
import me.myogoo.myotus.client.gui.config.BaseConfigTabScreen;
import me.myogoo.myotus.client.gui.widgets.button.CustomTabButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * A record containing tab information for the Terminal Config screen.
 */

public record TerminalConfigTab(Component title, Blitter blitter, ItemStack stack, String stylePath,
                                BaseConfigTabScreen configTabScreen) {
    public TerminalConfigTab(Component title, Icon icon, String stylePath, BaseConfigTabScreen configTabScreen) {
        this(title, icon.getBlitter(), null, stylePath, configTabScreen);
    }

    public TerminalConfigTab(Component title, MyoIcon icon, String stylePath, BaseConfigTabScreen configTabScreen) {
        this(title, icon.getBlitter(), null, stylePath, configTabScreen);
    }

    public TerminalConfigTab(Component title, ItemStack stack, String stylePath, BaseConfigTabScreen configTabScreen) {
        this(title, null, stack, stylePath, configTabScreen);
    }

    public CustomTabButton getTabButton(Button.OnPress onPress) {
        if(stack == null) {
            return new CustomTabButton(blitter, title, onPress);
        } else {
            return new CustomTabButton(stack, title, onPress);
        }
    }
}
