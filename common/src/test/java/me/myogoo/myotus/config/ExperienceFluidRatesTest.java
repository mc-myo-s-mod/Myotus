package me.myogoo.myotus.config;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExperienceFluidRatesTest {
    @Test
    void defaultsUseTwentyMbExceptForCreateEnchantmentIndustry() {
        var rates = ExperienceFluidRates.defaults();

        assertEquals(20, rates.rate("mob_grinding_utils:fluid_xp"));
        assertEquals(1, rates.rate("create_enchantment_industry:experience"));
    }

    @Test
    void parsesCustomRatesAndUsesLastDuplicate() {
        var rates = ExperienceFluidRates.parse(25,
                List.of("example:xp=2", " example:xp = 5 ", "example:other/xp=10"));

        assertEquals(25, rates.rate("example:unlisted"));
        assertEquals(5, rates.rate("example:xp"));
        assertEquals(10, rates.rate("example:other/xp"));
        assertEquals(2, rates.overrides().size());
    }

    @Test
    void emptyOverridesRemoveBuiltInExceptions() {
        var rates = ExperienceFluidRates.parse(20, List.of());

        assertTrue(rates.overrides().isEmpty());
        assertEquals(20, rates.rate("create_enchantment_industry:experience"));
    }

    @Test
    void validatesIdsAndPositiveBoundedIntegerRates() {
        for (String entry : List.of("example:xp=0", "example:xp=-1", "example:xp=1.5",
                "example:xp=2147483648", "example:xp=999999999999999999999", "example:xp=",
                "example:xp=1=2", "Example:xp=1", "xp=1", ":xp=1", "example:=1", "example:bad id=1")) {
            assertFalse(ExperienceFluidRates.isValidOverride(entry), entry);
            assertThrows(IllegalArgumentException.class, () -> ExperienceFluidRates.parse(20, List.of(entry)));
        }
        assertFalse(ExperienceFluidRates.isValidOverride(null));
        assertFalse(ExperienceFluidRates.isValidOverride(1));
        assertTrue(ExperienceFluidRates.isValidOverride("example:xp=2147483647"));
        assertThrows(IllegalArgumentException.class, () -> ExperienceFluidRates.parse(0, List.of()));
        assertThrows(IllegalArgumentException.class, () -> ExperienceFluidRates.parse(-1, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> ExperienceFluidRates.parse((long) Integer.MAX_VALUE + 1, List.of()));
    }

    @Test
    void snapshotsAndValidatesDirectlySuppliedOverrides() {
        var overrides = new LinkedHashMap<>(Map.of("example:xp", 1L));
        var rates = new ExperienceFluidRates(20, overrides);
        overrides.put("example:xp", 2L);

        assertEquals(1, rates.rate("example:xp"));
        assertThrows(UnsupportedOperationException.class, () -> rates.overrides().put("example:xp", 3L));
        assertThrows(IllegalArgumentException.class, () -> new ExperienceFluidRates(20, Map.of("bad", 1L)));
        assertThrows(IllegalArgumentException.class, () -> new ExperienceFluidRates(20, Map.of("example:xp", 0L)));
    }
}
