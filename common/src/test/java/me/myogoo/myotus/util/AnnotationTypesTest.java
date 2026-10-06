package me.myogoo.myotus.util;

import me.myogoo.myotus.util.reflect.annotation.AnnotationScanner;
import me.myogoo.myotus.util.reflect.annotation.AnnotationScanner.ScannedAnnotation;
import me.myogoo.myotus.util.reflect.annotation.AnnotationTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.transformer.throwables.IllegalClassLoadError;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnotationTypesTest {
    @Test
    void classMatchesExactMetaAndTransitiveMetaAnnotations() {
        assertTrue(AnnotationTypes.matches(FirstAnnotation.class, FirstAnnotation.class));
        assertTrue(AnnotationTypes.matches(ExtendedFirstAnnotation.class, FirstAnnotation.class));
        assertTrue(AnnotationTypes.matches(DeepExtendedFirstAnnotation.class, FirstAnnotation.class));

        assertFalse(AnnotationTypes.matches(FirstAnnotation.class, ExtendedFirstAnnotation.class));
        assertFalse(AnnotationTypes.matches(SecondAnnotation.class, FirstAnnotation.class));
    }

    @Test
    void asmTypeMatchingUsesTheSameRules() {
        assertTrue(AnnotationTypes.matches(Type.getType(ExtendedFirstAnnotation.class),
                Type.getType(FirstAnnotation.class)));
        assertFalse(AnnotationTypes.matches(Type.getType(FirstAnnotation.class),
                Type.getType(ExtendedFirstAnnotation.class)));
        assertFalse(AnnotationTypes.matches(Type.getType(String.class),
                Type.getType(FirstAnnotation.class)));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void unreadableMetadataDoesNotBreakClassOrAsmMatching(boolean mixinFailure) throws ClassNotFoundException {
        var loader = blockedContainerLoader(mixinFailure);
        var annotation = loader.loadClass(UnreadableAnnotation.class.getName()).asSubclass(Annotation.class);
        Class<? extends Error> failureType = mixinFailure ? IllegalClassLoadError.class : LinkageError.class;
        assertThrows(failureType, annotation::getAnnotations);
        assertTrue(AnnotationTypes.matches(annotation, annotation));
        assertFalse(AnnotationTypes.matches(annotation, FirstAnnotation.class));

        var thread = Thread.currentThread();
        var originalLoader = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(loader);
            assertTrue(AnnotationTypes.matches(Type.getType(annotation), Type.getType(annotation)));
            assertFalse(AnnotationTypes.matches(Type.getType(annotation), Type.getType(FirstAnnotation.class)));
        } finally {
            thread.setContextClassLoader(originalLoader);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void unreadableMetaAnnotationDoesNotHideAHealthySibling(boolean mixinFailure) throws ClassNotFoundException {
        var annotation = blockedContainerLoader(mixinFailure).loadClass(PartiallyReadableAnnotation.class.getName())
                .asSubclass(Annotation.class);
        assertTrue(AnnotationTypes.matches(annotation, FirstAnnotation.class));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void scannerSkipsUnreadableForeignMetadataAndKeepsHealthyTargets(boolean mixinFailure) {
        var thread = Thread.currentThread();
        var originalLoader = thread.getContextClassLoader();
        var foreign = new ScannedAnnotation(Type.getType(UnreadableAnnotation.class), Type.getType(String.class));
        var healthy = new ScannedAnnotation(Type.getType(FirstAnnotation.class), Type.getType(String.class));
        try {
            thread.setContextClassLoader(blockedContainerLoader(mixinFailure));
            AnnotationScanner.setAnnotationProvider(() -> Stream.of(foreign, healthy));
            assertEquals(List.of(healthy), List.copyOf(AnnotationScanner.find(FirstAnnotation.class)));
            assertEquals(List.of(healthy), List.copyOf(AnnotationScanner.findActive(FirstAnnotation.class)));
            assertTrue(AnnotationScanner.getActiveIntegrationAnnotations().isEmpty());
            assertTrue(AnnotationScanner.getItemListAnnotations().isEmpty());
        } finally {
            AnnotationScanner.setAnnotationProvider(Stream::empty);
            thread.setContextClassLoader(originalLoader);
        }
    }

    private static ClassLoader blockedContainerLoader(boolean mixinFailure) {
        return new ClassLoader(AnnotationTypesTest.class.getClassLoader()) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.equals(UnreadableContainer.class.getName())) {
                    if (mixinFailure) {
                        throw new IllegalClassLoadError("Repeatable container is in a protected mixin package");
                    }
                    throw new LinkageError("Repeatable container is in a protected mixin package");
                }
                if (!name.equals(UnreadableAnnotation.class.getName())
                        && !name.equals(PartiallyReadableAnnotation.class.getName())) {
                    return super.loadClass(name, resolve);
                }
                Class<?> loaded = findLoadedClass(name);
                if (loaded == null) {
                    try (var input = getResourceAsStream(name.replace('.', '/') + ".class")) {
                        byte[] bytes = input.readAllBytes();
                        loaded = defineClass(name, bytes, 0, bytes.length);
                    } catch (IOException e) {
                        throw new ClassNotFoundException(name, e);
                    }
                }
                if (resolve) {
                    resolveClass(loaded);
                }
                return loaded;
            }
        };
    }

    @Repeatable(UnreadableContainer.class)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface UnreadableAnnotation {
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface UnreadableContainer {
        UnreadableAnnotation[] value();
    }

    @UnreadableAnnotation
    @FirstAnnotation
    @Retention(RetentionPolicy.RUNTIME)
    public @interface PartiallyReadableAnnotation {
    }

    @Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    private @interface FirstAnnotation {
    }

    @FirstAnnotation
    @Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    private @interface ExtendedFirstAnnotation {
    }

    @ExtendedFirstAnnotation
    @Retention(RetentionPolicy.RUNTIME)
    private @interface DeepExtendedFirstAnnotation {
    }

    @Retention(RetentionPolicy.RUNTIME)
    private @interface SecondAnnotation {
    }
}
