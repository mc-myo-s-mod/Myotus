package me.myogoo.myotus.integration.ae2;

import me.myogoo.myotus.api.config.ConfigTab;

import java.util.*;

/**
 * Tab registry for the Terminal Config screen.
 * External mods can add tabs by calling registerTab().
 */
public class TerminalConfig {

    private static final List<ConfigTab> TABS = new ArrayList<>();
    private static final List<String> TAB_STYLE = new ArrayList<>();
    /**
     * Registers a new tab. Tabs are displayed in the order they are registered.
     * The first tab (AE2 default settings) is added automatically, so tabs
     * registered here will be displayed from the second position onwards.
     *
     * @param tab The tab information to register
     */
    public static void registerTab(ConfigTab tab) {
        TABS.add(tab);
        TAB_STYLE.add(tab.stylePath());
    }

    /**
     * Returns the list of registered tabs (read-only).
     */
    public static List<ConfigTab> getTabs() {
        return Collections.unmodifiableList(TABS);
    }

    public static String getTabStyle(int index) {
        return TAB_STYLE.get(index);
    }
}
