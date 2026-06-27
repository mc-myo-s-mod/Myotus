package me.myogoo.myotus.util.reflect;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

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
}
