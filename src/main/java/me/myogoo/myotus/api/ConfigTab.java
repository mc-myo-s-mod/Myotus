package me.myogoo.myotus.api;

import appeng.client.gui.Icon;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Terminal Config 화면의 탭 정보를 담는 레코드.
 */
public record ConfigTab(
        Component title,
        Icon icon,
        ItemStack iconStack,
        ConfigTabProvider provider) {
    /**
     * Icon 기반 탭 생성
     */
    public ConfigTab(Component title, Icon icon, ConfigTabProvider provider) {
        this(title, icon, null, provider);
    }

    /**
     * ItemStack 아이콘 기반 탭 생성
     */
    public ConfigTab(Component title, ItemStack iconStack, ConfigTabProvider provider) {
        this(title, null, iconStack, provider);
    }
}
