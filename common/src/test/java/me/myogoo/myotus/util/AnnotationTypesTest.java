package me.myogoo.myotus.util;

import me.myogoo.myotus.util.reflect.annotation.AnnotationTypes;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
