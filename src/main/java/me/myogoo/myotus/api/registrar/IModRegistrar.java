package me.myogoo.myotus.api.registrar;

import java.lang.annotation.Annotation;

/**
 * Interface for registering mod integrations.
 */
public interface IModRegistrar {
    void loadableMod(Class<? extends Annotation> annotationClass, String modId);

    void loadableMod(Class<? extends Annotation> annotationClass, String modId, String versionRange);

    void loadableMod(Class<? extends Annotation> annotationClass, String modId, String displayModName,
            String versionRange);
}
