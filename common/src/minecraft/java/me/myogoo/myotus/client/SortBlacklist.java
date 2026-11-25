package me.myogoo.myotus.client;

import me.myogoo.myotus.api.annotation.MyoSortBlacklist;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;

/** Client-side checks shared by the optional sorting integrations. */
public final class SortBlacklist {
    private SortBlacklist() {
    }

    public static boolean isBlacklisted(Screen screen) {
        if (screen == null) {
            return false;
        }
        return screen.getClass().isAnnotationPresent(MyoSortBlacklist.class)
                || (screen instanceof AbstractContainerScreen<?> container
                    && container.getMenu().getClass().isAnnotationPresent(MyoSortBlacklist.class));
    }

    public static boolean isBlacklisted(AbstractContainerMenu menu, Screen screen) {
        return menu.getClass().isAnnotationPresent(MyoSortBlacklist.class)
                || (screen instanceof AbstractContainerScreen<?> container
                    && container.getMenu() == menu
                    && screen.getClass().isAnnotationPresent(MyoSortBlacklist.class));
    }
}
