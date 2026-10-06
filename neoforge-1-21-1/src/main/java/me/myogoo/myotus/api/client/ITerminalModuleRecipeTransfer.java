package me.myogoo.myotus.api.client;

import appeng.menu.me.common.MEStorageMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

/** Viewer-neutral, client-only adapter registered once for a terminal module card. */
public interface ITerminalModuleRecipeTransfer {
    /** Recipe compatibility only; missing ingredients belong in {@link #getError}. Must not mutate state. */
    boolean supports(MEStorageMenu menu, RecipeHolder<?> recipe);

    /** Null allows transfer. Validate current panel/menu state without sending packets or changing inventories. */
    @Nullable Component getError(MEStorageMenu menu, RecipeHolder<?> recipe);

    /**
     * Recheck current state and request a server-authoritative transfer. True means a request was sent,
     * not that extraction succeeded. The server must validate the menu, installed card, recipe and permissions.
     * Implementations may transfer one set when maxTransfer is unsupported; never collect outputs implicitly.
     */
    boolean transfer(MEStorageMenu menu, RecipeHolder<?> recipe, boolean maxTransfer);
}
