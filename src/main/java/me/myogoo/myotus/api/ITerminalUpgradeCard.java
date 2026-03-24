package me.myogoo.myotus.api;

import appeng.menu.me.common.MEStorageMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Implemented by items that can be inserted into the terminal upgrade slot.
 *
 * <p>Only items implementing this interface are accepted by the terminal upgrade
 * slot filter. Every callback has a default no-op implementation, so card items
 * only need to override the hooks they actually use.</p>
 *
 * <p>Terminal upgrade cards are expected to be non-stackable. Item-backed
 * terminal storage is keyed per {@link ItemStack}, so upgrade cards should use
 * a max stack size of 1.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * public class MyUpgradeCardItem extends Item implements ITerminalUpgradeCard {
 *
 *     @Override
 *     public void onTerminalOpen(MEStorageMenu menu, ItemStack stack) {
 *         // Apply behavior when the terminal is opened.
 *     }
 *
 *     @Override
 *     public void onTerminalTick(MEStorageMenu menu, ItemStack stack) {
 *         // Run server-side logic every tick while installed.
 *     }
 * }
 * }</pre>
 */
public interface ITerminalUpgradeCard {

    /**
     * Called when a terminal containing this card is opened.
     *
     * <p>This callback runs on the server when the menu is opened or when the
     * card is inserted into an already open terminal.</p>
     *
     * @param menu currently open terminal menu
     * @param stack the card stack installed in the slot
     */
    default void onTerminalOpen(MEStorageMenu menu, ItemStack stack) {}

    /**
     * Called when a terminal containing this card is closed.
     *
     * <p>This callback runs on the server when the menu is closed or when the
     * card is removed from an open terminal.</p>
     *
     * @param menu terminal menu being closed
     * @param stack the card stack installed in the slot
     */
    default void onTerminalClose(MEStorageMenu menu, ItemStack stack) {}

    /**
     * Called once per server tick while the card is installed in an open terminal.
     *
     * <p>This is invoked from the server-side menu update flow.</p>
     *
     * @param menu currently open terminal menu
     * @param stack the card stack installed in the slot
     */
    default void onTerminalTick(MEStorageMenu menu, ItemStack stack) {}
}
