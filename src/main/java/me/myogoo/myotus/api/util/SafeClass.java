package me.myogoo.myotus.api.util;

import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for resolving classes without propagating {@link ClassNotFoundException}.
 *
 * <p>Missing classes are logged and reported as {@code null}, which makes this
 * helper suitable for optional-integration discovery code.</p>
 */
public class SafeClass {
    private static final Logger LOGGER = LoggerFactory.getLogger(SafeClass.class);

    /**
     * Resolves a class by fully qualified name.
     *
     * @param name fully qualified class name
     * @return the resolved class, or {@code null} if it cannot be found
     */
    public static Class<?> forName(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Can't find Class for : {}", String.valueOf(e));
            return null;
        }
    }

    /**
     * Resolves a class from its ASM type descriptor.
     *
     * @param type ASM type to resolve
     * @return the resolved class, or {@code null} if it cannot be found
     */
    public static Class<?> forType(Type type) {
        return forName(type.getClassName());
    }
}
