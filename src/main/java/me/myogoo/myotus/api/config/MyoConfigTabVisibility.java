package me.myogoo.myotus.api.config;

/**
 * Functional interface used to decide whether a config tab should be shown for
 * the currently opened terminal.
 */
@FunctionalInterface
public interface MyoConfigTabVisibility {
    MyoConfigTabVisibility ALWAYS_VISIBLE = context -> true;

    /**
     * Returns whether the tab should be visible for the supplied terminal
     * context.
     *
     * @param context current terminal context
     * @return {@code true} when the tab should be shown
     */
    boolean isVisible(MyoConfigTabContext context);
}
