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
        ConfigTabProvider provider) {
    /**
     * Creates a tab based on an Icon
     */
    public ConfigTab(Component title, Icon icon, ConfigTabProvider provider) {
        this(title, icon, null, provider);
    }

    /**
     * Creates a tab based on an ItemStack icon
     */
    public ConfigTab(Component title, ItemStack iconStack, ConfigTabProvider provider) {
        this(title, null, iconStack, provider);
    }
}
