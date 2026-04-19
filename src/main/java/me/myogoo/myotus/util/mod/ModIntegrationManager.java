package me.myogoo.myotus.util.mod;

import net.neoforged.fml.ModLoadingException;
import net.neoforged.fml.ModLoadingIssue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ModIntegrationManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModIntegrationManager.class);
    private static final String VERSION_MISMATCH_LOADING_ERROR =
            "The %s must be version %s or higher. Current version: %s";

    private static final Map<Class<? extends Annotation>, SupportedMod> supportIntegrations = new HashMap<>();
    private static final Map<SupportedMod, Class<? extends Annotation>> activeIntegrations = new HashMap<>();


    public static Class<? extends Annotation> getClass(SupportedMod mod) {
        return activeIntegrations.get(mod);
    }

    public static Class<? extends Annotation> getClass(String modId) {
        var mod = get(modId);
        if (mod == null) {
            return null;
        }
        return mod.getAnnotationClass();
    }

    public static boolean isLoaded(Class<? extends Annotation> annotationClass) {
        SupportedMod mod = supportIntegrations.get(annotationClass);
        return isLoaded(mod);
    }

    public static boolean isLoaded(SupportedMod mod) {
        if (mod == null)
            return false;
        return activeIntegrations.containsKey(mod);
    }

    public static Map<SupportedMod, Class<? extends Annotation>> getActiveIntegrations() {
        return Collections.unmodifiableMap(activeIntegrations);
    }

    public static SupportedMod get(String modIdOrName) {
        return supportIntegrations.values().stream()
                .filter(mod -> mod.getModId().equals(modIdOrName)
                        || mod.getModName().equals(modIdOrName))
                .findFirst()
                .orElse(null);
    }

    public static void put(Class<? extends Annotation> annotationClass, String modId) {
        put(annotationClass, new SupportedMod(modId, annotationClass, "*"));
    }

    public static void put(Class<? extends Annotation> annotationClass, String modId, String versionRange) {
        put(annotationClass, new SupportedMod(modId, annotationClass, versionRange));
    }

    public static void put(Class<? extends Annotation> annotationClass, String modId, String displayModName,
            String versionRange) {
        put(annotationClass, new SupportedMod(modId, annotationClass, versionRange, displayModName));

    }

    static void put(Class<? extends Annotation> annotationClass, SupportedMod mod) {
        supportIntegrations.put(annotationClass, mod);
        if (mod.isModLoaded()) {
            if (!mod.test()) {
                // Mod loading issues are rendered through FML's own translation table, not the mod's lang files.
                throw new ModLoadingException(
                        ModLoadingIssue.error(
                                VERSION_MISMATCH_LOADING_ERROR,
                                mod.getDisplayModName(),
                                mod.getMiniumVersion(),
                                mod.getModVersion()));
            }
            activeIntegrations.put(mod, mod.getAnnotationClass());
            LOGGER.info("Integration enabled for: {} (version: {})", mod.getDisplayModName(), mod.getModVersion());
        }
    }

    private ModIntegrationManager() {
    }
}
