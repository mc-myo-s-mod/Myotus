package me.myogoo.myotus.dto;

import me.myogoo.myotus.api.annotation.MyoMod.IntegrationMode;
import org.apache.maven.artifact.versioning.ArtifactVersion;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class MyoModDto {
    private final Class<? extends Annotation> annotationClass;
    private final String modId;
    private final Set<String> aliases;
    private final String namespace;
    private final String displayModName;
    private final ArtifactVersion version;
    private final String versionRange;
    private final IntegrationMode mode;

    public MyoModDto(Class<? extends Annotation> annotationClass, MyoModInfoDto modInfo,
            String versionRange, IntegrationMode mode) {
        this(annotationClass, modInfo, Set.of(), versionRange, mode);
    }

    public MyoModDto(Class<? extends Annotation> annotationClass, MyoModInfoDto modInfo,
            Set<String> aliases, String versionRange, IntegrationMode mode) {
        this.annotationClass = annotationClass;
        this.modId = modInfo.modId();
        this.aliases = normalizeAliases(aliases);
        this.namespace = normalizeDisplayName(modInfo.modId(), modInfo.namespace());
        this.displayModName = normalizeDisplayName(modInfo.modId(), modInfo.displayName());
        this.version = modInfo.version();
        this.versionRange = normalizeVersionRange(versionRange);
        this.mode = mode == null ? IntegrationMode.ONLY : mode;
    }

    public Class<? extends Annotation> getAnnotationClass() {
        return annotationClass;
    }

    public String getModId() {
        return modId;
    }

    public Set<String> getAliases() {
        return aliases;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getDisplayModName() {
        return displayModName;
    }

    public String getModName() {
        return displayModName;
    }

    public ArtifactVersion getModVersion() {
        return version;
    }

    public ArtifactVersion getVersion() {
        return version;
    }

    public String getVersionRange() {
        return versionRange;
    }

    public IntegrationMode getMode() {
        return mode;
    }

    public boolean isSameTarget(MyoModDto other) {
        return other != null
                && annotationClass == other.annotationClass
                && Objects.equals(modId, other.modId);
    }

    public boolean isSameRegistration(MyoModDto other) {
        return isSameTarget(other) && Objects.equals(versionRange, other.versionRange);
    }

    public String describe() {
        return "%s/%s %s".formatted(modId, displayModName, versionRange);
    }

    public boolean matches(String id) {
        return id != null
                && (id.equals(modId)
                        || aliases.contains(id)
                        || id.equals(namespace)
                        || id.equals(displayModName));
    }

    private static String normalizeDisplayName(String modId, String displayName) {
        return displayName == null || displayName.isBlank() ? modId : displayName;
    }

    private static String normalizeVersionRange(String versionRange) {
        return versionRange == null || versionRange.isBlank() ? "*" : versionRange;
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
        return Collections.unmodifiableSet(normalized);
    }
}
