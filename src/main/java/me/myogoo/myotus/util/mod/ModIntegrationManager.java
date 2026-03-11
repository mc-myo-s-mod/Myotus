package me.myogoo.myotus.util.mod;

import net.neoforged.fml.ModLoadingException;
import net.neoforged.fml.ModLoadingIssue;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public final class ModIntegrationManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModIntegrationManager.class);

    private static final Map<Class<? extends Annotation>, SupportedMod> supportIntegrations = new HashMap<>();
    private static final Map<SupportedMod, Class<? extends Annotation>> activeIntegrations = new HashMap<>();

    public static void initialize() {
        LOGGER.info("Checking for mod integrations...");
        activeIntegrations.clear();

        for (var mod : supportIntegrations.values()) {
            if (!mod.isModLoaded()) {
                continue;
            }
            if (!mod.test()) {
                throw new ModLoadingException(
                        ModLoadingIssue.error(
                                "error.myotus.mod.loading.version.mismatch",
                                mod.getDisplayModName(),
                                mod.getModVersion(),
                                mod.getVersionRange()));
            }

            activeIntegrations.put(mod, mod.getAnnotationClass());
            LOGGER.info("Integration enabled for: {} (version: {})", mod.getDisplayModName(), mod.getModVersion());
        }
    }

    public static Class<? extends Annotation> getClass(SupportedMod mod) {
        return activeIntegrations.get(mod);
    }

    public static Class<? extends Annotation> getClass(String modId) {
        for (var value : activeIntegrations.values()) {
            if (value.getSimpleName().equals(modId)) {
                return value;
            }
        }
        return null;
    }

    public static boolean isLoaded(Class<? extends Annotation> annotationClass) {
        return activeIntegrations.containsValue(annotationClass);
    }

    public static boolean isLoaded(SupportedMod mod) {
        if (mod == null)
            return false;
        return activeIntegrations.containsKey(mod);
    }

    public static Map<SupportedMod, Class<? extends Annotation>> getActiveIntegrations() {
        return activeIntegrations;
    }

    public static SupportedMod get(String modDisplayName) {
        return activeIntegrations.keySet().stream().filter(mod -> mod.getDisplayModName().equals(modDisplayName))
                .findFirst().orElse(null);
    }

    public static void put(Class<? extends Annotation> annotationClass, String modId) {
        supportIntegrations.put(annotationClass, new SupportedMod(modId, annotationClass, "*"));
    }

    public static void put(Class<? extends Annotation> annotationClass, String modId, String versionRange) {
        supportIntegrations.put(annotationClass, new SupportedMod(modId, annotationClass, versionRange));
    }

    public static void put(Class<? extends Annotation> annotationClass, String modId, String displayModName, String versionRange) {
        supportIntegrations.put(annotationClass, new SupportedMod(modId, annotationClass, versionRange, displayModName));
    }

    private ModIntegrationManager() {
    }
}