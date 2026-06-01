package me.myogoo.myotus.util.mod;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModVersionHelperTest {
    @Test
    void wildcardAndBlankRangesMatchAnyVersion() {
        var actualVersion = new DefaultArtifactVersion("9.9.9");

        assertTrue(ModVersionHelper.isVersionInRange(null, actualVersion));
        assertTrue(ModVersionHelper.isVersionInRange("", actualVersion));
        assertTrue(ModVersionHelper.isVersionInRange("*", actualVersion));
    }

    @Test
    void inclusiveAndExclusiveRangeBoundsAreRespected() {
        assertTrue(ModVersionHelper.isVersionInRange("[1.2.0,2.0.0)",
                new DefaultArtifactVersion("1.2.0")));
        assertTrue(ModVersionHelper.isVersionInRange("[1.2.0,2.0.0)",
                new DefaultArtifactVersion("1.9.9")));
        assertFalse(ModVersionHelper.isVersionInRange("[1.2.0,2.0.0)",
                new DefaultArtifactVersion("2.0.0")));
        assertFalse(ModVersionHelper.isVersionInRange("[1.2.0,2.0.0)",
                new DefaultArtifactVersion("1.1.9")));
    }

    @Test
    void invalidRangeDoesNotMatch() {
        assertFalse(ModVersionHelper.isVersionInRange("[1.0.0,broken",
                new DefaultArtifactVersion("1.0.0")));
    }

    @Test
    void minimumVersionUsesWildcardFallbackRecommendedVersionAndLowerBound() {
        assertEquals("0.0.0", ModVersionHelper.getMinimumVersion(null).toString());
        assertEquals("0.0.0", ModVersionHelper.getMinimumVersion("").toString());
        assertEquals("0.0.0", ModVersionHelper.getMinimumVersion("*").toString());
        assertEquals("1.2.3", ModVersionHelper.getMinimumVersion("1.2.3").toString());
        assertEquals("1.5.0", ModVersionHelper.getMinimumVersion("[1.5.0,2.0.0)").toString());
        assertEquals("0.0.0", ModVersionHelper.getMinimumVersion("[1.0.0,broken").toString());
    }
}
