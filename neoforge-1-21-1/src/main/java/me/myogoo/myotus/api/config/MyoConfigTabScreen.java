package me.myogoo.myotus.api.config;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.WidgetContainer;

/**
 * Strategy interface used to build the contents of a {@link MyoConfigTab}.
 */
public interface MyoConfigTabScreen {
    /**
     * Populates the widget container for a terminal configuration tab.
     *
     * <p>Implementations should add buttons, labels, and any other tab-specific
     * widgets to the supplied container.</p>
     *
     * <p>Example:</p>
     * <pre>{@code
     * public final class ExampleConfigScreen implements MyoConfigTabScreen {
     *     @Override
     *     public void buildTab(WidgetContainer widgets, AEBaseScreen<?> screen) {
     *         widgets.addButton(new MyButton(...));
     *     }
     * }
     * }</pre>
     *
     * @param widgets widget container for the current tab
     * @param screen owning AE2 screen instance
     */
    void buildTab(WidgetContainer widgets, AEBaseScreen<?> screen);
}
