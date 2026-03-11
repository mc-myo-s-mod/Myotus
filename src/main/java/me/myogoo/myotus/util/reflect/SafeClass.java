package me.myogoo.myotus.util.reflect;

import me.myogoo.myotus.Myotus;
import org.objectweb.asm.Type;

//maybe unsafe?
public class SafeClass {
    public static Class<?> forName(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            Myotus.LOGGER.error("Can't find Class for : {}", String.valueOf(e));
            return null;
        }
    }

    public static Class<?> forType(Type type) {
        return forName(type.getClassName());
    }
}
