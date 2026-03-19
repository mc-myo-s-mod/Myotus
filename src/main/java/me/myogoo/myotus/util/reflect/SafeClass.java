package me.myogoo.myotus.util.reflect;

import org.objectweb.asm.Type;

/**
 * @deprecated Use {@link me.myogoo.myotus.api.util.SafeClass} instead.
 *             This class is kept for backward compatibility and delegates to
 *             the API version.
 */
@Deprecated
public class SafeClass {
    public static Class<?> forName(String name) {
        return me.myogoo.myotus.api.util.SafeClass.forName(name);
    }

    public static Class<?> forType(Type type) {
        return me.myogoo.myotus.api.util.SafeClass.forType(type);
    }
}
