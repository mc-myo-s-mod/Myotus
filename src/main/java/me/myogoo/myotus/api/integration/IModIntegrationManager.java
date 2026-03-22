package me.myogoo.myotus.api.integration;

import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Runtime view of the integrations registered through Myotus.
 *
 * <p>Use this manager to check whether a registered optional dependency is
 * currently active after version and load checks have been applied.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * IModIntegrationManager manager = MyotusAPI.modIntegrationManager();
 *
 * if (manager.isLoaded("ae2wtlib")) {
 *     // Safe to use AE2WTLib-specific classes that were registered with Myotus.
 * }
 * }</pre>
 */
public interface IModIntegrationManager {

    /**
     * Returns whether the integration mapped to the given marker annotation is active.
     *
     * @param annotationClass marker annotation associated with a registered mod
     * @return {@code true} if the mod is loaded and passed the integration checks
     */
    boolean isLoaded(Class<? extends Annotation> annotationClass);

    /**
     * Returns whether the integration registered for the supplied mod ID is active.
     *
     * @param modId registered mod ID
     * @return {@code true} if the mod is loaded and passed the integration checks
     */
    default boolean isLoaded(String modId) {
        var annotationClass = getAnnotationClass(modId);
        return annotationClass != null && isLoaded(annotationClass);
    }

    /**
     * Returns the marker annotation class associated with a registered mod ID.
     *
     * @param modId registered mod ID
     * @return the marker annotation class, or {@code null} if the mod ID is unknown
     */
    @Nullable
    Class<? extends Annotation> getAnnotationClass(String modId);

    /**
     * Returns whether the supplied mod ID has been registered with Myotus.
     *
     * @param modId registered mod ID
     * @return {@code true} if a mapping exists for the mod ID
     */
    default boolean isRegistered(String modId) {
        return getAnnotationClass(modId) != null;
    }

    /**
     * Returns all currently active integrations.
     *
     * <p>The returned map is read-only. Its key type is intentionally opaque because
     * callers are expected to inspect activation state through this interface instead
     * of depending on internal integration model classes.</p>
     *
     * @return an unmodifiable view of active integrations
     */
    Map<?, Class<? extends Annotation>> getActiveIntegrations();
}
