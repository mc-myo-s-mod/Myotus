package me.myogoo.myotus.api.recipe;

import it.unimi.dsi.fastutil.ints.IntList;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class IMyotusTableRecipeTest {
    @Test
    void slotIngredientsPreservesEmptySlotsAndRepeatedIngredients() {
        String first = "first";
        String second = "second";
        var slots = SlotIngredientLayout.expand(IntList.of(0, -1, 1, 0), List.of(first, second));

        assertEquals(4, slots.size());
        assertSame(first, slots.get(0).orElseThrow());
        assertEquals(Optional.empty(), slots.get(1));
        assertSame(second, slots.get(2).orElseThrow());
        assertSame(first, slots.get(3).orElseThrow());
    }
}
