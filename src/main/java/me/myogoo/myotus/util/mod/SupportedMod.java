package me.myogoo.myotus.util.mod;

import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;
import org.apache.maven.artifact.versioning.ArtifactVersion;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;

public class SupportedMod {

    private final String modId;
    private final String versionRange;
    private final Class<? extends Annotation> annotationClass;
    private final Predicate<IModInfo> customLoadCondition;

    SupportedMod(String modId, Class<? extends Annotation> annotationClass, String versionRange) {
        this(modId, annotationClass, versionRange, modInfo -> true);
    }

    SupportedMod(String modId, Class<? extends Annotation> annotationClass, String versionRange,
            String displayModName) {
        this(modId, annotationClass, versionRange, modInfo -> displayModName.equals(modInfo.getDisplayName()));
    }

    SupportedMod(String modId, Class<? extends Annotation> annotationClass, String versionRange,
            Predicate<IModInfo> customLoadCondition) {
        this.modId = modId;
        this.annotationClass = annotationClass;
        this.versionRange = versionRange;
        this.customLoadCondition = customLoadCondition;
    }

    public Class<? extends Annotation> getAnnotationClass() {
        return annotationClass;
    }

    public String getModId() {
        return modId;
    }

    public String getDisplayModName() {
        return ModList.get()
                .getModContainerById(modId)
                .map(container -> container.getModInfo().getDisplayName())
                .orElse(modId);
    }

    public ArtifactVersion getModVersion() {
        return ModList.get()
                .getModContainerById(modId)
                .map(container -> container.getModInfo().getVersion())
                .orElse(null);
    }

    public String getVersionRange() {
        return versionRange;
    }

    public ArtifactVersion getMiniumVersion() {
        return ModVersionHelper.getMinimumVersion(versionRange);
    }

    public boolean isModLoaded() {
        return ModList.get()
                .getModContainerById(modId)
                .map(container -> customLoadCondition.test(container.getModInfo()))
                .orElse(false);
    }

    public boolean test() {
        return ModVersionHelper.isVersionInRange(versionRange, getModVersion());
    }
}
