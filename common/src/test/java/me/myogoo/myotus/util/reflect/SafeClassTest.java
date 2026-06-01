package me.myogoo.myotus.util.reflect;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SafeClassTest {
    @Test
    void resolvesExistingClassesByNameAndType() {
        assertEquals(String.class, SafeClass.forName("java.lang.String"));
        assertEquals(String.class, SafeClass.optionalName("java.lang.String").orElseThrow());
        assertEquals(String.class, SafeClass.forType(Type.getType(String.class)));
        assertEquals(String.class, SafeClass.optionalType(Type.getType(String.class)).orElseThrow());
        assertTrue(SafeClass.isPresent("java.lang.String"));
        assertTrue(SafeClass.isPresent(Type.getType(String.class)));
    }

    @Test
    void missingBlankAndNullInputsReturnEmptyResults() {
        assertNull(SafeClass.forName(null));
        assertNull(SafeClass.forName(" "));
        assertNull(SafeClass.forName("me.myogoo.myotus.DoesNotExist"));
        assertNull(SafeClass.forType(null));
        assertFalse(SafeClass.optionalName(null).isPresent());
        assertFalse(SafeClass.optionalName(" ").isPresent());
        assertFalse(SafeClass.optionalName("me.myogoo.myotus.DoesNotExist").isPresent());
        assertFalse(SafeClass.optionalType(null).isPresent());
        assertFalse(SafeClass.isPresent("me.myogoo.myotus.DoesNotExist"));
        assertFalse(SafeClass.isPresent((Type) null));
    }
}
