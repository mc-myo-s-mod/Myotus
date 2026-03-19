package me.myogoo.myotus.api.config;

import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;
import me.myogoo.myotus.client.gui.MyoIcon;
import me.myogoo.myotus.client.gui.widgets.button.CustomTabButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/**
 * A record containing tab information for the Terminal Config screen.
 */

public record MyoConfigTab(Component title, Blitter blitter, ItemStack stack, String stylePath,
                           MyoConfigTabScreen configTabScreen) {
    public MyoConfigTab(Component title, Icon icon, String stylePath, MyoConfigTabScreen configTabScreen) {
        this(title, icon.getBlitter(), null, stylePath, configTabScreen);
    }

    public MyoConfigTab(Component title, MyoIcon icon, String stylePath, MyoConfigTabScreen configTabScreen) {
        this(title, icon.getBlitter(), null, stylePath, configTabScreen);
    }

    public MyoConfigTab(Component title, ItemStack stack, String stylePath, MyoConfigTabScreen configTabScreen) {
        this(title, null, stack, stylePath, configTabScreen);
    }

    public CustomTabButton getTabButton(Button.OnPress onPress) {
        if(stack != null) {
            return new CustomTabButton(stack, title, onPress);
        } else return new CustomTabButton(Objects.requireNonNullElseGet(blitter, Icon.COG::getBlitter), title, onPress);
    }
}
