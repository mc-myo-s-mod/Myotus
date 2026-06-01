package me.myogoo.myotus.platform;

import me.myogoo.myotus.util.reflect.annotation.AnnotationScanner.ScannedAnnotation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData;

import java.util.stream.Stream;

public final class AnnotationScanData {
    private static final String VERSION_MISMATCH_STARTUP_TEST_ANNOTATION =
            "me.myogoo.myotus.util.mod.startuptest.VersionMismatchStartupTestMyoMod";
    private static final String VERSION_MISMATCH_STARTUP_TEST_PROPERTY =
            "myotus.startupVersionMismatchTest";

    private AnnotationScanData() {
    }

    public static Stream<ScannedAnnotation> getAnnotations() {
        return ModList.get().getAllScanData().stream()
                .flatMap(scanData -> scanData.getAnnotations().stream())
                .map(AnnotationScanData::wrap)
                .filter(AnnotationScanData::includeStartupTestAnnotation);
    }

    private static boolean includeStartupTestAnnotation(ScannedAnnotation annotation) {
        return !VERSION_MISMATCH_STARTUP_TEST_ANNOTATION.equals(annotation.className())
                || Boolean.getBoolean(VERSION_MISMATCH_STARTUP_TEST_PROPERTY);
    }

    private static ScannedAnnotation wrap(AnnotationData annotation) {
        return new ScannedAnnotation(annotation.annotationType(), annotation.targetType(), annotation.clazz());
    }
}
