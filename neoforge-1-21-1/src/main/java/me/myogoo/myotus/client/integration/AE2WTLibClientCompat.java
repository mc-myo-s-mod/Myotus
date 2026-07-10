package me.myogoo.myotus.client.integration;

import de.mari_023.ae2wtlib.api.gui.ScrollingUpgradesPanel;
import de.mari_023.ae2wtlib.api.terminal.WTMenuHost;
import me.myogoo.myotus.mixin.ae2wtlib.ScrollingUpgradesPanelAccessor;

import java.util.Optional;

/** Client-only calls isolated from code that must load without AE2WTLib. */
public final class AE2WTLibClientCompat {
    private AE2WTLibClientCompat() {
    }

    public static boolean isMenuHost(Object host) {
        return host instanceof WTMenuHost;
    }

    public static Optional<Boolean> configureScrollingUpgradesPanel(Object panel, int maxRows) {
        if (!(panel instanceof ScrollingUpgradesPanel scrollingPanel)) {
            return Optional.empty();
        }
        scrollingPanel.setMaxRows(maxRows);
        return Optional.of(((ScrollingUpgradesPanelAccessor) (Object) scrollingPanel).myotus$isScrolling());
    }
}
