package me.myogoo.myotus.api;

import me.myogoo.myotus.api.integration.IModIntegrationManager;
import me.myogoo.myotus.api.registrar.IConfigRegistrar;
import me.myogoo.myotus.api.registrar.IModRegistrar;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

/**
 * Static entry point for the Myotus public API.
 *
 * <p>Use this class when you need quick access to the shared API services without
 * storing the {@link IMyotusAPI} instance yourself.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * MyotusAPI.modRegistrar()
 *         .registerLoadableMod(MyMarker.class, "examplemod", "[1.0.0,)");
 *
 * if (MyotusAPI.modIntegrationManager().isLoaded("examplemod")) {
 *     // Run optional integration code.
 * }
 * }</pre>
 */
public final class MyotusAPI {
    private static IMyotusAPI instance;

    private MyotusAPI() {
    }

    /**
     * Returns the shared Myotus API implementation.
     *
     * <p>This method throws if the API has not been initialized yet. External
     * integrations should normally call it during or after mod setup, not from
     * static initializers.</p>
     *
     * @return the shared API instance
     */
    public static IMyotusAPI get() {
        return Objects.requireNonNull(instance, "MyotusAPI has not been initialized yet!");
    }

    /**
     * Returns whether the API instance is ready to use.
     *
     * @return {@code true} if {@link #get()} can be called safely
     */
    public static boolean isInitialized() {
        return instance != null;
    }

    /**
     * Returns the registrar used to declare optional mod integrations.
     *
     * @return the integration registrar
     */
    public static IModRegistrar modRegistrar() {
        return get().modRegistrar();
    }

    /**
     * Returns the registrar used to add terminal configuration tabs.
     *
     * @return the config registrar
     */
    public static IConfigRegistrar configRegistrar() {
        return get().configRegistrar();
    }

    /**
     * Returns the runtime integration manager.
     *
     * <p>Use this manager to query whether a registered integration is currently
     * active.</p>
     *
     * @return the integration manager
     */
    public static IModIntegrationManager modIntegrationManager() {
        return get().modIntegrationManager();
    }

    /**
     * Internal bootstrap hook used by Myotus to install the API implementation.
     *
     * @param api the API implementation
     */
    @ApiStatus.Internal
    public static void _setInstance(IMyotusAPI api) {
        if (instance != null) {
            throw new IllegalStateException("MyotusAPI instance is already set!");
        }
        instance = Objects.requireNonNull(api, "api");
    }
}
