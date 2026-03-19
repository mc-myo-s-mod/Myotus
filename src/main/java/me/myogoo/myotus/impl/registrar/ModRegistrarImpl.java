package me.myogoo.myotus.impl.registrar;

import me.myogoo.myotus.api.registrar.IModRegistrar;
import me.myogoo.myotus.util.mod.ModIntegrationManager;

import java.lang.annotation.Annotation;

public class ModRegistrarImpl implements IModRegistrar {
    @Override
    public void loadableMod(Class<? extends Annotation> annotationClass, String modId) {
        ModIntegrationManager.put(annotationClass, modId);
    }

    @Override
    public void loadableMod(Class<? extends Annotation> annotationClass, String modId, String versionRange) {
        ModIntegrationManager.put(annotationClass, modId, versionRange);
    }

    @Override
    public void loadableMod(Class<? extends Annotation> annotationClass, String modId, String displayModName,
            String versionRange) {
        ModIntegrationManager.put(annotationClass, modId, displayModName, versionRange);
    }
}
