package me.myogoo.myotus.util.reflect.annotation;

import me.myogoo.myotus.api.annotation.MyotusSubscriber;
import me.myogoo.myotus.util.reflect.annotation.AnnotationScanner.ScannedAnnotation;
import me.myogoo.myotus.util.MyoLogger;
import me.myogoo.myotus.util.reflect.SafeClass;
import org.objectweb.asm.Type;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

public class ItemListScanner {
    public static void invokeItemListSubscribers(Class<?> targetAnnotation, Object... args) {
        var res = AnnotationScanner
                .getItemListAnnotations()
                .stream()
                .filter(anno -> AnnotationTypes.matches(anno.annotationType(), Type.getType(targetAnnotation)))
                .toList();

        for(var a : res) {
            invokeSubscribe(a, args);
        }
    }

    private static void invokeSubscribe(ScannedAnnotation annotation, Object... args) {
        Class<?> clazz = SafeClass.forType(annotation.clazz());
        if (Objects.isNull(clazz)) {
            return;
        }

        Method[] methods = clazz.getDeclaredMethods();
        for(var method : methods) {
            if(method.isAnnotationPresent(MyotusSubscriber.class)) {
                if(!Modifier.isStatic(method.getModifiers())) {
                    MyoLogger.warn("Method {} in class {} is annotated with @MyotusSubscriber but is not static. Skipping.",
                            method.getName(), clazz.getName());
                    continue;
                }
                method.setAccessible(true);
                try {
                    method.invoke(null, args);
                } catch (InvocationTargetException e) {
                    MyoLogger.error("Failed to invoke method {} in class {}: {}", method.getName(), clazz.getName(), e.getCause().getMessage());
                } catch (IllegalAccessException e) {
                    MyoLogger.error("Failed to access method {} in class {}: {}", method.getName(), clazz.getName(), e.getMessage());
                }
            }
        }
    }
}
