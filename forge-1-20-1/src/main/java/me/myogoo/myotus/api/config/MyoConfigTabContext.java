package me.myogoo.myotus.api.config;

import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.storage.ITerminalHost;
import appeng.menu.me.common.MEStorageMenu;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.Optional;

/**
 * Runtime context exposed to config tab visibility checks.
 *
 * @param menu current terminal menu
 * @param host terminal host backing the menu
 * @param player player viewing the menu
 */
public record MyoConfigTabContext(MEStorageMenu menu, ITerminalHost host, Player player) {
    public MyoConfigTabContext {
        Objects.requireNonNull(menu, "menu");
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(player, "player");
    }

    /**
     * Creates a context from a terminal menu.
     *
     * @param menu terminal menu
     * @return a new config tab context
     */
    public static MyoConfigTabContext from(MEStorageMenu menu) {
        Objects.requireNonNull(menu, "menu");
        return new MyoConfigTabContext(menu, menu.getHost(), menu.getPlayer());
    }

    /**
     * Returns whether the current terminal is backed by an item host.
     *
     * @return {@code true} if the terminal host is item-backed
     */
    public boolean isItemHost() {
        return host instanceof ItemMenuHost;
    }

    /**
     * Returns the host item stack when the terminal is item-backed.
     *
     * @return the host item stack or {@link ItemStack#EMPTY}
     */
    public ItemStack getHostItemStack() {
        if (host instanceof ItemMenuHost itemHost) {
            return itemHost.getItemStack();
        }
        return ItemStack.EMPTY;
    }

    /**
     * Returns whether the host item matches the supplied item.
     *
     * @param item item to test
     * @return {@code true} if the host item matches
     */
    public boolean isHostItem(Item item) {
        return !getHostItemStack().isEmpty() && getHostItemStack().is(item);
    }

    /**
     * Returns whether the host item matches the supplied item id.
     *
     * @param itemId item id to test
     * @return {@code true} if the host item matches
     */
    public boolean isHostItem(ResourceLocation itemId) {
        return Optional.ofNullable(ForgeRegistries.ITEMS.getValue(itemId))
                .map(this::isHostItem)
                .orElse(false);
    }

    /**
     * Returns whether the host item belongs to the supplied namespace.
     *
     * @param namespace item namespace to test
     * @return {@code true} if the host item namespace matches
     */
    public boolean isHostItemFrom(String namespace) {
        ItemStack stack = getHostItemStack();
        if (stack.isEmpty()) {
            return false;
        }

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return itemId != null && namespace.equals(itemId.getNamespace());
    }
}
