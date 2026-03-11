package me.myogoo.myotus.api.config;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.WidgetContainer;

/**
 * A callback interface that configures the widgets when a tab is selected.
 */
@FunctionalInterface
public interface ConfigTabProvider {
    /**
     * Called when a tab is selected to place the widgets.
     *
     * @param widgets The widget container
     * @param screen  The current screen
     */
    void buildTab(WidgetContainer widgets, AEBaseScreen<?> screen);
}
