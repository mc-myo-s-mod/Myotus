package me.myogoo.myotus.client;

import me.myogoo.myotus.api.annotation.MyoSortBlacklist;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SortBlacklistTest {
    @BeforeAll
    static void bootstrap() {
        Bootstrap.bootStrap();
    }

    @Test
    void markedScreenAndItsSubclassAreBlacklisted() {
        assertTrue(SortBlacklist.isBlacklisted(mock(MarkedScreen.class)));
        assertTrue(SortBlacklist.isBlacklisted(mock(ChildScreen.class)));
    }

    @Test
    void menuMarkerAlsoProtectsAnUnmarkedScreen() {
        var screen = mock(ContainerScreen.class);
        when(screen.getMenu()).thenReturn(mock(ChildMenu.class));

        assertTrue(SortBlacklist.isBlacklisted(screen));
    }

    @Test
    void unrelatedScreensAndMenusAreNotBlacklisted() {
        var screen = mock(ContainerScreen.class);
        when(screen.getMenu()).thenReturn(mock(AbstractContainerMenu.class));

        assertFalse(SortBlacklist.isBlacklisted(screen));
        assertFalse(SortBlacklist.isBlacklisted(mock(Screen.class)));
        assertFalse(SortBlacklist.isBlacklisted(null));
    }

    @Test
    void screenOnlyMarkerDoesNotLeakToOtherMenus() {
        var screen = mock(MarkedContainerScreen.class);
        var menu = mock(AbstractContainerMenu.class);
        var otherMenu = mock(AbstractContainerMenu.class);
        when(screen.getMenu()).thenReturn(menu);

        assertTrue(SortBlacklist.isBlacklisted(menu, screen));
        assertFalse(SortBlacklist.isBlacklisted(otherMenu, screen));
        assertFalse(SortBlacklist.isBlacklisted(otherMenu, null));
        assertTrue(SortBlacklist.isBlacklisted(mock(ChildMenu.class), null));
    }

    @MyoSortBlacklist
    public abstract static class MarkedScreen extends Screen {
        protected MarkedScreen() {
            super(Component.empty());
        }
    }

    public abstract static class ChildScreen extends MarkedScreen {
    }

    public abstract static class ContainerScreen extends AbstractContainerScreen<AbstractContainerMenu> {
        protected ContainerScreen(AbstractContainerMenu menu, Inventory inventory, Component title) {
            super(menu, inventory, title);
        }
    }

    @MyoSortBlacklist
    public abstract static class MarkedContainerScreen extends ContainerScreen {
        protected MarkedContainerScreen(AbstractContainerMenu menu, Inventory inventory, Component title) {
            super(menu, inventory, title);
        }
    }

    @MyoSortBlacklist
    public abstract static class MarkedMenu extends AbstractContainerMenu {
        protected MarkedMenu() {
            super(null, 0);
        }
    }

    public abstract static class ChildMenu extends MarkedMenu {
    }
}
