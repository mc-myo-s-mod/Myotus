package me.myogoo.myotus.api.registrar;

import java.lang.annotation.Annotation;

/**
 * Registrar for declaring optional mod integrations.
 *
 * <p>Each registration binds a marker annotation to a mod ID. Myotus later uses
 * that marker annotation to discover classes that should only be processed when
 * the target mod is available.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * MyotusAPI.modRegistrar()
 *         .registerLoadableMod(AE2WTLib.class, "ae2wtlib", "[19.2.5,)")
 *         .registerLoadableMod(JEI.class, "jei", "Just Enough Items", "[19.0.0,)");
 * }</pre>
 */
public interface IModRegistrar {
    /**
     * Registers an integration using the mod ID as both the lookup key and display name.
     *
     * @param annotationClass marker annotation used on integration classes
     * @param modId target mod ID
     */
    void loadableMod(Class<? extends Annotation> annotationClass, String modId);

    /**
     * Registers an integration with a required version range.
     *
     * @param annotationClass marker annotation used on integration classes
     * @param modId target mod ID
     * @param versionRange Maven-style version range
     */
    void loadableMod(Class<? extends Annotation> annotationClass, String modId, String versionRange);

    /**
     * Registers an integration with a custom display name and version range.
     *
     * @param annotationClass marker annotation used on integration classes
     * @param modId target mod ID
     * @param displayModName display name to report for the integration
     * @param versionRange Maven-style version range
     */
    void loadableMod(Class<? extends Annotation> annotationClass, String modId, String displayModName,
            String versionRange);

    /**
     * Fluent alias for {@link #loadableMod(Class, String)}.
     *
     * @param annotationClass marker annotation used on integration classes
     * @param modId target mod ID
     * @return {@code this} for chaining
     */
    default IModRegistrar registerLoadableMod(Class<? extends Annotation> annotationClass, String modId) {
        loadableMod(annotationClass, modId);
        return this;
    }

    /**
     * Fluent alias for {@link #loadableMod(Class, String, String)}.
     *
     * @param annotationClass marker annotation used on integration classes
     * @param modId target mod ID
     * @param versionRange Maven-style version range
     * @return {@code this} for chaining
     */
    default IModRegistrar registerLoadableMod(Class<? extends Annotation> annotationClass, String modId,
            String versionRange) {
        loadableMod(annotationClass, modId, versionRange);
        return this;
    }

    /**
     * Fluent alias for {@link #loadableMod(Class, String, String, String)}.
     *
     * @param annotationClass marker annotation used on integration classes
     * @param modId target mod ID
     * @param displayModName display name to report for the integration
     * @param versionRange Maven-style version range
     * @return {@code this} for chaining
     */
    default IModRegistrar registerLoadableMod(Class<? extends Annotation> annotationClass, String modId,
            String displayModName, String versionRange) {
        loadableMod(annotationClass, modId, displayModName, versionRange);
        return this;
    }
}
