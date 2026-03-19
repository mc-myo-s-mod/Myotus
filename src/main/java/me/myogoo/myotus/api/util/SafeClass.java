package me.myogoo.myotus.api.util;

import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for safely loading classes by name or ASM Type,
 * logging an error instead of throwing if the class is not found.
 */
public class SafeClass {
    private static final Logger LOGGER = LoggerFactory.getLogger(SafeClass.class);

    public static Class<?> forName(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Can't find Class for : {}", String.valueOf(e));
            return null;
        }
    }

    public static Class<?> forType(Type type) {
        return forName(type.getClassName());
    }
}
