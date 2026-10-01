package me.myogoo.myotus.util.reflect;

import me.myogoo.myotus.util.MyoLogger;
import org.objectweb.asm.Type;

import java.util.Optional;

public final class SafeClass {
    private static volatile boolean dedicatedServer;

    private SafeClass() {
    }

    /** Configured by the loader before annotation and integration discovery. */
    public static void setDedicatedServer(boolean value) {
        dedicatedServer = value;
    }

    public static Class<?> forName(String name) {
        return optionalName(name).orElse(null);
    }

    public static Optional<Class<?>> optionalName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        if (dedicatedServer && isClientOnlyName(name)) {
            return Optional.empty();
        }

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = SafeClass.class.getClassLoader();
            }
            return Optional.of(Class.forName(name, false, classLoader));
        } catch (ClassNotFoundException e) {
            MyoLogger.debug("Could not resolve class {}", name);
            return Optional.empty();
        } catch (LinkageError | RuntimeException e) {
            MyoLogger.debug("Could not safely load class {}", name, e);
            return Optional.empty();
        }
    }

    public static Class<?> forType(Type type) {
        return optionalType(type).orElse(null);
    }

    public static Optional<Class<?>> optionalType(Type type) {
        if (type == null) {
            return Optional.empty();
        }
        return optionalName(type.getClassName());
    }

    public static boolean isPresent(String name) {
        return optionalName(name).isPresent();
    }

    public static boolean isPresent(Type type) {
        return optionalType(type).isPresent();
    }

    private static boolean isClientOnlyName(String name) {
        return name.startsWith("com.mojang.blaze3d.")
                || name.startsWith("net.minecraft.client.")
                || name.contains(".client.");
    }
}
