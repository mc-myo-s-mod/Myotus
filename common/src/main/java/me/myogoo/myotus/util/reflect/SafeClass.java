package me.myogoo.myotus.util.reflect;

import me.myogoo.myotus.util.MyoLogger;
import org.objectweb.asm.Type;

import java.util.Optional;

public final class SafeClass {
    private static final boolean DEDICATED_SERVER = detectDedicatedServer();

    private SafeClass() {
    }

    public static Class<?> forName(String name) {
        return optionalName(name).orElse(null);
    }

    public static Optional<Class<?>> optionalName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        if (DEDICATED_SERVER && isClientOnlyName(name)) {
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

    private static boolean detectDedicatedServer() {
        return isDedicatedServer("net.minecraftforge.fml.loading.FMLEnvironment")
                || isDedicatedServer("net.neoforged.fml.loading.FMLEnvironment");
    }

    private static boolean isDedicatedServer(String environmentClassName) {
        try {
            Class<?> environmentClass = Class.forName(environmentClassName);
            Object dist = environmentClass.getField("dist").get(null);
            return "DEDICATED_SERVER".equals(String.valueOf(dist));
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return false;
        }
    }
}
