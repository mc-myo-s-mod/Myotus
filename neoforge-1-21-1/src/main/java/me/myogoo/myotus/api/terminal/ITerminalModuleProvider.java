package me.myogoo.myotus.api.terminal;

import appeng.menu.me.common.MEStorageMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Common-side native slot factory. Register the same provider on both physical sides. */
@FunctionalInterface
public interface ITerminalModuleProvider {
    /**
     * Called once per concrete menu on both sides, including when the card is absent. Return native
     * slots in deterministic order, using only layout information already synchronized to the client.
     * Do not return a slot already added to this menu or returned by another provider for this menu.
     * Myotus creates guarded views that remain registered until the menu closes, enforcing card and host locks.
     */
    List<? extends Slot> createSlots(MEStorageMenu menu);

    /** Sends any layout data needed by the client before its menu-open packet. Runs on the server. */
    default void beforeMenuOpen(ServerPlayer player) {}

    /** False closes the server menu instead of changing its packet slot indices while open. */
    default boolean isLayoutValid(MEStorageMenu menu) {
        return true;
    }

    /** Called with the original native slot after Myotus checks creative-mode and module locks. */
    default ItemStack getCloneStack(Slot slot) {
        ItemStack stack = slot.getItem();
        return stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(stack.getMaxStackSize());
    }
}
