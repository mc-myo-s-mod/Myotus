package me.myogoo.myotus.api;

import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

/**
 * Entry point for the Myotus API.
 */
public final class MyotusAPI {
    private static IMyotusAPI instance;

    private MyotusAPI() {
    }

    /**
     * Gets the main API instance.
     * Use {@link IMyotusAPI#modRegistrar()} to register mod integrations
     * and {@link IMyotusAPI#configRegistrar()} to register configuration tabs.
     *
     * @return The Myotus API instance.
     */
    public static IMyotusAPI get() {
        return Objects.requireNonNull(instance, "MyotusAPI has not been initialized yet!");
    }

    /**
     * Internal use only. Sets the API implementation.
     * 
     * @param api The API implementation.
     */
    @ApiStatus.Internal
    public static void _setInstance(IMyotusAPI api) {
        if (instance != null) {
            throw new IllegalStateException("MyotusAPI instance is already set!");
        }
        instance = api;
    }
}
