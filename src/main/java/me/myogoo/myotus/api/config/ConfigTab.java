package me.myogoo.myotus.api.config;

import appeng.client.gui.Icon;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * A record containing tab information for the Terminal Config screen.
 */
public record ConfigTab(
        Component title,
        Icon icon,
        ItemStack iconStack,
        ConfigTabProvider provider, String stylePath) {
    /**
     * Creates a tab based on an Icon
     */
    public ConfigTab(Component title, Icon icon, String stylePath,  ConfigTabProvider provider) {
        this(title, icon, null, provider, stylePath);
    }

    /**
     * Creates a tab based on an ItemStack icon
     */
    public ConfigTab(Component title, ItemStack iconStack, String stylePath, ConfigTabProvider provider) {
        this(title, null, iconStack, provider, stylePath);
    }
}
