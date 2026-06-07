package me.myogoo.myotus.integration.itemList;

import me.myogoo.myotus.api.annotation.MyotusSubscriber;
import me.myogoo.myotus.api.annotation.MyoMod;
import me.myogoo.myotus.util.reflect.annotation.AnnotationScanner;
import me.myogoo.myotus.util.MyoLogger;
import me.myogoo.myotus.util.mod.ModIntegrationManager;
import me.myogoo.myotus.util.reflect.SafeClass;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;

public class ItemListModLoadHelper {
    public static <R> void invokeItemListMod(
            Type markerAnnotation,
            Class<R> parameterType,
            R parameter) {
        AnnotationScanner
                .findActive(markerAnnotation)
                .stream()
                .map(a -> SafeClass.forType(a.clazz()))
                .filter(Objects::nonNull)
                .filter(ItemListModLoadHelper::hasNoIntegrationMarkerOrActiveIntegration)
                .forEach(clazz -> invokeMethod(clazz, parameterType, parameter));
    }

    private static boolean hasNoIntegrationMarkerOrActiveIntegration(Class<?> clazz) {
        var integrationAnnotations = Arrays.stream(clazz.getDeclaredAnnotations())
                .filter(annotation -> isIntegrationAnnotation(annotation.annotationType()))
                .toList();

        return integrationAnnotations.isEmpty()
                || integrationAnnotations.stream()
                        .allMatch(annotation -> ModIntegrationManager.isLoaded(annotation.annotationType()));
    }

    private static boolean isIntegrationAnnotation(Class<? extends Annotation> annotationClass) {
        return annotationClass.isAnnotationPresent(MyoMod.class)
                || ModIntegrationManager.isRegistered(annotationClass);
    }

    public static <R> void invokeItemListMod(
            Class<?> markerAnnotation,
            Class<R> parameterType,
            R parameter) {
        invokeItemListMod(
                Type.getType(markerAnnotation),
                parameterType,
                parameter);
    }

    private static <R> void invokeMethod(Class<?> clazz, Class<R> parameterType, R registration) {
        try {
            Method[] methods = clazz.getDeclaredMethods();
            for (var method : methods) {
                if (method.isAnnotationPresent(MyotusSubscriber.class)) {
                    if (!Modifier.isStatic(method.getModifiers())) {
                        MyoLogger.warn(
                                "Method {} in class {} is annotated with @MyotusSubscriber but is not static.",
                                method.getName(), clazz.getName());
                        continue;
                    }
                    if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == parameterType) {
                        method.setAccessible(true);
                        method.invoke(null, registration);
                    } else {
                        MyoLogger.warn(
                                "Method {} in class {} is annotated with @MyotusSubscriber but does not have the correct parameters.",
                                method.getName(), clazz.getName());
                    }
                }
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            MyoLogger.error("Failed to register ItemListMod for class: {}", clazz.getName(), e);
        }
    }
}
