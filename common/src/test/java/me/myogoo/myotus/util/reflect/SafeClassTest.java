package me.myogoo.myotus.util.reflect;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.transformer.throwables.IllegalClassLoadError;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SafeClassTest {
    private static final String MISSING_CLASS = SafeClassTest.class.getPackageName() + ".DoesNotExist";

    @Test
    void resolvesExistingClassesByNameAndType() {
        assertEquals(String.class, SafeClass.forName(String.class.getName()));
        assertEquals(String.class, SafeClass.optionalName(String.class.getName()).orElseThrow());
        assertEquals(String.class, SafeClass.forType(Type.getType(String.class)));
        assertEquals(String.class, SafeClass.optionalType(Type.getType(String.class)).orElseThrow());
        assertTrue(SafeClass.isPresent(String.class.getName()));
        assertTrue(SafeClass.isPresent(Type.getType(String.class)));
    }

    @Test
    void missingBlankAndNullInputsReturnEmptyResults() {
        assertNull(SafeClass.forName(null));
        assertNull(SafeClass.forName(" "));
        assertNull(SafeClass.forName(MISSING_CLASS));
        assertNull(SafeClass.forType(null));
        assertFalse(SafeClass.optionalName(null).isPresent());
        assertFalse(SafeClass.optionalName(" ").isPresent());
        assertFalse(SafeClass.optionalName(MISSING_CLASS).isPresent());
        assertFalse(SafeClass.optionalType(null).isPresent());
        assertFalse(SafeClass.isPresent(MISSING_CLASS));
        assertFalse(SafeClass.isPresent((Type) null));
    }

    @Test
    void loaderSideBlocksClientNamesBeforeClassLoading() {
        var thread = Thread.currentThread();
        var originalLoader = thread.getContextClassLoader();
        var attemptedLoads = new ArrayList<String>();
        var clientNames = List.of("net.minecraft.client.DoesNotExist", "com.mojang.blaze3d.DoesNotExist",
                "example.client.DoesNotExist");
        try {
            thread.setContextClassLoader(new ClassLoader(originalLoader) {
                @Override
                public Class<?> loadClass(String name) throws ClassNotFoundException {
                    attemptedLoads.add(name);
                    return super.loadClass(name);
                }
            });
            SafeClass.setDedicatedServer(true);
            clientNames.forEach(name -> assertTrue(SafeClass.optionalName(name).isEmpty()));
            assertTrue(attemptedLoads.isEmpty(), "Server must not ask the class loader for client classes");
            assertEquals(String.class, SafeClass.forName(String.class.getName()));

            SafeClass.setDedicatedServer(false);
            clientNames.forEach(SafeClass::optionalName);
            assertTrue(attemptedLoads.containsAll(clientNames), "Client may resolve client classes");
        } finally {
            SafeClass.setDedicatedServer(false);
            thread.setContextClassLoader(originalLoader);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void blockedClassesReturnEmptyResults(boolean mixinFailure) {
        var thread = Thread.currentThread();
        var originalLoader = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(new ClassLoader(originalLoader) {
                @Override
                public Class<?> loadClass(String name) throws ClassNotFoundException {
                    if (name.equals(MISSING_CLASS)) {
                        if (mixinFailure) {
                            throw new IllegalClassLoadError("Class belongs to a protected mixin package");
                        }
                        throw new NoClassDefFoundError(name);
                    }
                    return super.loadClass(name);
                }
            });
            assertTrue(SafeClass.optionalName(MISSING_CLASS).isEmpty());
        } finally {
            thread.setContextClassLoader(originalLoader);
        }
    }
}
