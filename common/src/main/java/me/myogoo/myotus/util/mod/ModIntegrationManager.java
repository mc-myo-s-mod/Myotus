package me.myogoo.myotus.util.mod;

import me.myogoo.myotus.api.annotation.MyoMod;
import me.myogoo.myotus.api.annotation.MyoMod.IntegrationMode;
import me.myogoo.myotus.api.integration.MyoCustomCondition;
import me.myogoo.myotus.dto.MyoModDto;
import me.myogoo.myotus.dto.MyoModInfoDto;
import me.myogoo.myotus.platform.mod.IModList;
import me.myogoo.myotus.util.MyoLogger;
import me.myogoo.myotus.util.reflect.annotation.AnnotationScanner;
import me.myogoo.myotus.util.reflect.annotation.AnnotationTypes;
import me.myogoo.myotus.util.reflect.SafeClass;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ModIntegrationManager {
    private static final Map<Class<? extends Annotation>, MyoModRegistration> registeredIntegrations =
            new LinkedHashMap<>();
    private static final Map<MyoModDto, Class<? extends Annotation>> activeIntegrations = new LinkedHashMap<>();
    private static IModList modList = IModList.EMPTY;

    private ModIntegrationManager() {
    }

    public static void setModList(IModList modList) {
        ModIntegrationManager.modList = Objects.requireNonNull(modList, "modList");
        registeredIntegrations.clear();
        activeIntegrations.clear();
        registerMyoModAnnotations();
        rebuildActiveIntegrations();
    }

    public static MyoModDto get(String id) {
        return activeIntegrations.keySet().stream()
                .filter(mod -> matches(mod, id))
                .findFirst()
                .orElse(null);
    }

    public static Class<? extends Annotation> getClass(MyoModDto mod) {
        return activeIntegrations.entrySet().stream()
                .filter(entry -> entry.getKey().isSameRegistration(mod))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    public static boolean isLoaded(Class<? extends Annotation> annotationClass) {
        return activeIntegrations.values().stream()
                .anyMatch(active -> AnnotationTypes.matches(annotationClass, active)
                        || AnnotationTypes.matches(active, annotationClass));
    }

    public static boolean isLoaded(MyoModDto mod) {
        return activeIntegrations.keySet().stream()
                .anyMatch(active -> active.isSameRegistration(mod));
    }

    public static boolean isLoaded(org.objectweb.asm.Type annotationType) {
        Class<?> annotationClass = SafeClass.forType(annotationType);
        if (annotationClass == null || !annotationClass.isAnnotation()) {
            return false;
        }
        @SuppressWarnings("unchecked")
        Class<? extends Annotation> typedAnnotationClass = (Class<? extends Annotation>) annotationClass;
        return isLoaded(typedAnnotationClass);
    }

    public static boolean isLoaded(String id) {
        return activeIntegrations.keySet().stream()
                .anyMatch(mod -> matches(mod, id));
    }

    public static boolean isRegistered(String id) {
        return registeredIntegrations.values().stream()
                .anyMatch(mod -> mod.matches(id));
    }

    public static boolean isRegistered(Class<? extends Annotation> annotationClass) {
        return registeredIntegrations.keySet().stream()
                .anyMatch(registered -> AnnotationTypes.matches(annotationClass, registered)
                        || AnnotationTypes.matches(registered, annotationClass));
    }

    public static Class<? extends Annotation> getClass(String id) {
        var active = activeIntegrations.entrySet().stream()
                .filter(entry -> matches(entry.getKey(), id))
                .map(Map.Entry::getValue)
                .distinct()
                .toList();
        if (active.size() == 1) {
            return active.get(0);
        }
        if (!active.isEmpty()) {
            return null;
        }

        var registered = registeredIntegrations.values().stream()
                .filter(mod -> mod.matches(id))
                .map(MyoModRegistration::annotationClass)
                .distinct()
                .toList();
        return registered.size() == 1 ? registered.get(0) : null;
    }

    public static Map<MyoModDto, Class<? extends Annotation>> getActiveIntegrations() {
        return Collections.unmodifiableMap(activeIntegrations);
    }

    private static void registerMyoModAnnotations() {
        for (AnnotationScanner.ScannedAnnotation annotation : AnnotationScanner.getMyoModAnnotations()) {
            Class<?> annotationClass = SafeClass.forType(annotation.clazz());
            if (annotationClass == null || !annotationClass.isAnnotation()) {
                continue;
            }

            @SuppressWarnings("unchecked")
            Class<? extends Annotation> typedAnnotationClass = (Class<? extends Annotation>) annotationClass;
            if (registeredIntegrations.containsKey(typedAnnotationClass)) {
                continue;
            }

            MyoMod myoMod = typedAnnotationClass.getAnnotation(MyoMod.class);
            if (myoMod == null) {
                continue;
            }
            registeredIntegrations.put(typedAnnotationClass,
                    MyoModRegistration.fromAnnotation(typedAnnotationClass, myoMod));
        }
        validateAliases();
    }

    private static void rebuildActiveIntegrations() {
        activeIntegrations.clear();

        Map<String, Set<String>> aliasesByModId = aliasesByModId();
        Map<String, List<MyoModDto>> activeByModId = new LinkedHashMap<>();
        for (MyoModRegistration registration : registeredIntegrations.values()) {
            MyoModDto mod = finalizeRegistration(registration, aliasesForActiveRegistration(registration, aliasesByModId));
            if (mod != null) {
                activeByModId.computeIfAbsent(mod.getModId(), ignored -> new ArrayList<>()).add(mod);
            }
        }

        for (List<MyoModDto> mods : activeByModId.values()) {
            List<MyoModDto> overrides = mods.stream()
                    .filter(mod -> mod.getMode() == IntegrationMode.OVERRIDE)
                    .toList();
            if (!overrides.isEmpty()) {
                overrides.forEach(ModIntegrationManager::activate);
                continue;
            }

            List<MyoModDto> only = mods.stream()
                    .filter(mod -> mod.getMode() == IntegrationMode.ONLY)
                    .toList();
            if (only.size() > 1) {
                MyoLogger.warn("Multiple ONLY MyoMod integrations were active for {}; using {}",
                        only.get(0).getModId(), only.get(0).getAnnotationClass().getName());
            }
            if (!only.isEmpty()) {
                activate(only.get(0));
            }

            mods.stream()
                    .filter(mod -> mod.getMode() == IntegrationMode.EXTENDED)
                    .forEach(ModIntegrationManager::activate);
        }
    }

    private static void activate(MyoModDto mod) {
        activeIntegrations.put(mod, mod.getAnnotationClass());
    }

    private static MyoModDto finalizeRegistration(MyoModRegistration registration, Set<String> sharedAliases) {
        if (!modList.isLoaded(registration.modId())) {
            return null;
        }

        MyoModInfoDto modInfo = modList.getModInfoById(registration.modId());
        if (modInfo == null) {
            return null;
        }
        if (!ModVersionHelper.isVersionInRange(registration.versionRange(), modInfo.version())) {
            throw new MyoModVersionMismatchException(modInfo, registration.versionRange());
        }
        if (!testCustomCondition(registration, modInfo)) {
            return null;
        }

        return new MyoModDto(registration.annotationClass(), modInfo, sharedAliases,
                registration.versionRange(), registration.mode());
    }

    private static Set<String> aliasesForActiveRegistration(MyoModRegistration registration,
            Map<String, Set<String>> aliasesByModId) {
        if (registration.hasCustomCondition()) {
            return registration.aliases();
        }
        return aliasesByModId.getOrDefault(registration.modId(), Set.of());
    }

    private static Map<String, Set<String>> aliasesByModId() {
        Map<String, Set<String>> aliasesByModId = new LinkedHashMap<>();
        for (MyoModRegistration registration : registeredIntegrations.values()) {
            if (registration.hasCustomCondition()) {
                continue;
            }
            aliasesByModId.computeIfAbsent(registration.modId(), ignored -> new LinkedHashSet<>())
                    .addAll(registration.aliases());
        }
        return aliasesByModId;
    }

    private static void validateAliases() {
        Map<String, String> aliasOwners = new HashMap<>();
        Map<String, Class<? extends Annotation>> aliasOwnerClasses = new HashMap<>();
        for (MyoModRegistration registration : registeredIntegrations.values()) {
            for (String alias : registration.aliases()) {
                String previousModId = aliasOwners.putIfAbsent(alias, registration.modId());
                if (previousModId != null && !previousModId.equals(registration.modId())) {
                    throw new IllegalStateException(
                            "MyoMod alias '%s' is used for both '%s' (%s) and '%s' (%s)".formatted(
                                    alias,
                                    previousModId,
                                    aliasOwnerClasses.get(alias).getName(),
                                    registration.modId(),
                                    registration.annotationClass().getName()));
                }
                aliasOwnerClasses.putIfAbsent(alias, registration.annotationClass());
            }
        }

        Map<String, Class<? extends Annotation>> modIdOwners = new HashMap<>();
        for (MyoModRegistration registration : registeredIntegrations.values()) {
            modIdOwners.putIfAbsent(registration.modId(), registration.annotationClass());
        }
        for (MyoModRegistration registration : registeredIntegrations.values()) {
            for (String alias : registration.aliases()) {
                Class<? extends Annotation> aliasedModIdOwner = modIdOwners.get(alias);
                if (aliasedModIdOwner != null && !alias.equals(registration.modId())) {
                    throw new IllegalStateException(
                            "MyoMod alias '%s' on '%s' (%s) collides with mod id '%s' declared by %s".formatted(
                                    alias,
                                    registration.modId(),
                                    registration.annotationClass().getName(),
                                    alias,
                                    aliasedModIdOwner.getName()));
                }
            }
        }
    }

    private static boolean testCustomCondition(MyoModRegistration registration, MyoModInfoDto modInfo) {
        Class<? extends MyoCustomCondition> conditionClass = registration.customConditionClass();
        if (conditionClass == MyoCustomCondition.class) {
            return true;
        }

        try {
            var constructor = conditionClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance().test(modInfo);
        } catch (ReflectiveOperationException | RuntimeException e) {
            MyoLogger.warn("Failed to evaluate MyoMod custom condition {} for {}",
                    conditionClass.getName(), registration.describe(), e);
            return false;
        }
    }

    private static boolean matches(MyoModDto mod, String id) {
        return mod.matches(id);
    }

    private record MyoModRegistration(
            String modId,
            Class<? extends Annotation> annotationClass,
            Set<String> aliases,
            String versionRange,
            IntegrationMode mode,
            Class<? extends MyoCustomCondition> customConditionClass) {

        private MyoModRegistration {
            aliases = normalizeAliases(aliases);
            versionRange = versionRange == null || versionRange.isBlank() ? "*" : versionRange;
            mode = mode == null ? IntegrationMode.ONLY : mode;
            customConditionClass = customConditionClass == null ? MyoCustomCondition.class : customConditionClass;
        }

        static MyoModRegistration fromAnnotation(Class<? extends Annotation> annotationClass, MyoMod myoMod) {
            return new MyoModRegistration(
                    myoMod.value(),
                    annotationClass,
                    Set.of(myoMod.alias()),
                    myoMod.versionRange(),
                    myoMod.mode(),
                    myoMod.customCondition());
        }

        boolean hasCustomCondition() {
            return customConditionClass != MyoCustomCondition.class;
        }

        boolean matches(String id) {
            return id != null && (id.equals(modId) || aliases.contains(id));
        }

        String describe() {
            return "%s %s".formatted(modId, versionRange);
        }

        private static Set<String> normalizeAliases(Set<String> aliases) {
            if (aliases == null || aliases.isEmpty()) {
                return Set.of();
            }

            LinkedHashSet<String> normalized = new LinkedHashSet<>();
            for (String alias : aliases) {
                if (alias != null && !alias.isBlank()) {
                    normalized.add(alias);
                }
            }
            return Set.copyOf(normalized);
        }
    }
}
