package me.myogoo.myotus.impl.integration;

import me.myogoo.myotus.api.integration.IModIntegrationManager;
import me.myogoo.myotus.util.mod.ModIntegrationManager;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Implementation of {@link IModIntegrationManager} that delegates to
 * the internal {@link ModIntegrationManager}.
 */
public class ModIntegrationManagerImpl implements IModIntegrationManager {

    @Override
    public boolean isLoaded(Class<? extends Annotation> annotationClass) {
        return ModIntegrationManager.isLoaded(annotationClass);
    }

    @Override
    public Class<? extends Annotation> getAnnotationClass(String modId) {
        return ModIntegrationManager.getClass(modId);
    }

    @Override
    public Map<?, Class<? extends Annotation>> getActiveIntegrations() {
        return ModIntegrationManager.getActiveIntegrations();
    }
}
