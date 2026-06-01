package me.myogoo.myotus.api.util;

import org.objectweb.asm.Type;

/**
 * Utility for resolving classes without propagating {@link ClassNotFoundException}.
 *
 * <p>Missing classes are logged and reported as {@code null}, which makes this
 * helper suitable for optional-integration discovery code.</p>
 */
public final class SafeClass {
    private SafeClass() {
    }

    /**
     * Resolves a class by fully qualified name.
     *
     * @param name fully qualified class name
     * @return the resolved class, or {@code null} if it cannot be found
     */
    public static Class<?> forName(String name) {
        return me.myogoo.myotus.util.reflect.SafeClass.forName(name);
    }

    /**
     * Resolves a class from its ASM type descriptor.
     *
     * @param type ASM type to resolve
     * @return the resolved class, or {@code null} if it cannot be found
     */
    public static Class<?> forType(Type type) {
        return me.myogoo.myotus.util.reflect.SafeClass.forType(type);
    }
}
