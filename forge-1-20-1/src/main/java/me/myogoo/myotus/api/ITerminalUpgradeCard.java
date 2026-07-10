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
 * <p>Items implementing this interface are also discovered automatically for the
 * empty upgrade-slot tooltip. Each terminal can only install one copy of a
 * given item type at a time.</p>
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
     * Called when a terminal containing this card begins its first server menu update, after the concrete
     * menu has finished construction, or when the card is inserted into an already active menu.
     *
     * <p>This callback runs on the server. {@code stack} is the installed stack and may be used to
     * persist this card's data. Do not replace its item, change its count, or retain the reference.</p>
     *
     * @param menu currently open terminal menu
     * @param stack the card stack installed in the slot
     */
    default void onTerminalOpen(MEStorageMenu menu, ItemStack stack) {}

    /**
     * Called when a terminal containing this card is closed.
     *
     * <p>This callback runs on the server. For an installed card, {@code stack} is the live stack and
     * may persist card data. A card removed from an open menu receives its last snapshot instead.</p>
     *
     * @param menu terminal menu being closed
     * @param stack the card stack installed in the slot
     */
    default void onTerminalClose(MEStorageMenu menu, ItemStack stack) {}

    /**
     * Called once per server tick while the card is installed in an open terminal.
     *
     * <p>This is invoked once per game tick from the server-side menu update flow. {@code stack} is the
     * installed stack and may be used to persist this card's data. Do not retain the reference.</p>
     *
     * @param menu currently open terminal menu
     * @param stack the card stack installed in the slot
     */
    default void onTerminalTick(MEStorageMenu menu, ItemStack stack) {}
}
