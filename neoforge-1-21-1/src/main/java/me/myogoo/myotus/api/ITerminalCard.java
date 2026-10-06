package me.myogoo.myotus.api;

import appeng.menu.me.common.MEStorageMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Implemented by items that can be installed in a terminal card slot.
 *
 * <p>Every lifecycle callback has a default no-op implementation. Callbacks run on the server
 * per open menu/viewer, not globally per shared terminal. Installed stacks are live and may be
 * used to persist card data, but their item or count must not be changed. Item singletons must
 * not retain a player, menu or stack. Unavailable cards receive no lifecycle callbacks.</p>
 *
 * <p>Card items should use a maximum stack size of one. Each terminal accepts at most one card
 * of each item type across upgrade and module cards.</p>
 */
public interface ITerminalCard {
    /**
     * Whether this card is available for this installation, based only on stable dependency presence.
     * Implementations must return the same result on client and server, and must not use per-player
     * or mutable runtime conditions here. Unavailable cards remain recoverable from saved inventories.
     */
    default boolean isAvailable() {
        return true;
    }

    /** Called on the first server menu update after construction, or insertion into an active menu. */
    default void onTerminalOpen(MEStorageMenu menu, ItemStack stack) {}

    /** Called when the terminal closes or this card is removed; removal receives the last stack snapshot. */
    default void onTerminalClose(MEStorageMenu menu, ItemStack stack) {}

    /** Called once per server tick while this card is installed in an open terminal. */
    default void onTerminalTick(MEStorageMenu menu, ItemStack stack) {}
}
