package me.myogoo.myotus.integration.ae2;

import me.myogoo.myotus.api.config.ConfigTab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tab registry for the Terminal Config screen.
 * External mods can add tabs by calling registerTab().
 */
public class TerminalConfig {

    private static final List<ConfigTab> TABS = new ArrayList<>();

    /**
     * Registers a new tab. Tabs are displayed in the order they are registered.
     * The first tab (AE2 default settings) is added automatically, so tabs
     * registered here will be displayed from the second position onwards.
     *
     * @param tab The tab information to register
     */
    public static void registerTab(ConfigTab tab) {
        TABS.add(tab);
    }

    /**
     * Returns the list of registered tabs (read-only).
     */
    public static List<ConfigTab> getTabs() {
        return Collections.unmodifiableList(TABS);
    }
}
