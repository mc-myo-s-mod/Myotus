package me.myogoo.myotus.api.integration;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * API interface for querying mod integration status.
 * Use {@link me.myogoo.myotus.api.MyotusAPI#get()} to obtain an instance
 * via {@link me.myogoo.myotus.api.IMyotusAPI#modIntegrationManager()}.
 */
public interface IModIntegrationManager {

    /**
     * Checks if a mod associated with the given annotation class is loaded and
     * active.
     *
     * @param annotationClass The annotation class associated with the mod
     * @return true if the mod is loaded and active
     */
    boolean isLoaded(Class<? extends Annotation> annotationClass);

    /**
     * Gets the annotation class associated with a mod by its mod ID.
     *
     * @param modId The mod ID to look up
     * @return The annotation class, or null if not found
     */
    Class<? extends Annotation> getAnnotationClass(String modId);

    /**
     * Returns a map of all active mod integrations.
     * Keys are internal mod representations, values are their annotation classes.
     *
     * @return An unmodifiable view of active integrations
     */
    Map<?, Class<? extends Annotation>> getActiveIntegrations();
}
