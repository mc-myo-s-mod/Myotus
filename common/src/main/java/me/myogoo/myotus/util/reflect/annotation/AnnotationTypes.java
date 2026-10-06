package me.myogoo.myotus.util.reflect.annotation;

import me.myogoo.myotus.util.MyoLogger;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.transformer.throwables.IllegalClassLoadError;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import me.myogoo.myotus.util.reflect.SafeClass;

public final class AnnotationTypes {
    private AnnotationTypes() {
    }

    public static boolean matches(Type actualType, Type expectedType) {
        if (actualType == null || expectedType == null) {
            return false;
        }
        if (actualType.equals(expectedType)) {
            return true;
        }

        Class<?> actualClass = resolve(actualType);
        Class<?> expectedClass = resolve(expectedType);
        if (actualClass == null
                || expectedClass == null
                || !actualClass.isAnnotation()
                || !expectedClass.isAnnotation()) {
            return false;
        }

        @SuppressWarnings("unchecked")
        Class<? extends Annotation> actualAnnotation = (Class<? extends Annotation>) actualClass;
        @SuppressWarnings("unchecked")
        Class<? extends Annotation> expectedAnnotation = (Class<? extends Annotation>) expectedClass;
        return matches(actualAnnotation, expectedAnnotation);
    }

    public static boolean matches(Class<? extends Annotation> actual, Class<? extends Annotation> expected) {
        return matches(actual, expected, new HashSet<>());
    }

    private static boolean matches(
            Class<? extends Annotation> actual,
            Class<? extends Annotation> expected,
            Set<Class<? extends Annotation>> visited) {
        if (actual == null || expected == null) {
            return false;
        }
        if (actual == expected) {
            return true;
        }
        if (!visited.add(actual)) {
            return false;
        }

        Annotation[] annotations;
        try {
            annotations = actual.getAnnotations();
        } catch (LinkageError | IllegalClassLoadError e) {
            // Metadata can reference classes that a mod's class loader forbids loading.
            MyoLogger.debug("Could not read annotation metadata for {}", actual.getName(), e);
            return false;
        }
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> metaType = annotation.annotationType();
            if (metaType.getPackageName().equals(Annotation.class.getPackageName())) {
                continue;
            }
            if (matches(metaType, expected, visited)) {
                return true;
            }
        }
        return false;
    }

    private static Class<?> resolve(Type type) {
        return SafeClass.optionalName(type.getClassName()).orElse(null);
    }
}
