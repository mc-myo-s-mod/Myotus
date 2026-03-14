package me.myogoo.myotus.api.config;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.WidgetContainer;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;

public abstract class BaseConfigTabScreen {
    protected abstract void updateState();
    @OverridingMethodsMustInvokeSuper
    protected void save() {
        updateState();
    }

    /**
     * Called when a tab is selected to place the widgets.
     *
     * @param widgets The widget container
     * @param screen  The current screen
     */
    public abstract void buildTab(WidgetContainer widgets, AEBaseScreen<?> screen);
}
