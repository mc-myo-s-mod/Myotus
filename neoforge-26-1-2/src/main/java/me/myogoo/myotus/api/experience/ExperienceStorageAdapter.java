package me.myogoo.myotus.api.experience;

import appeng.api.stacks.AEKey;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Converts one AE storage key family between storage units and normalized raw experience points.
 *
 * <p>AE fluid amounts are storage units (mB), not experience points. Callers must therefore supply
 * the conversion configured by the fluid integration instead of assuming a universal ratio.</p>
 */
public final class ExperienceStorageAdapter {
    private final ExperienceMath.ExperienceSource source;
    private final Predicate<AEKey> matcher;
    private final long storageUnitsPerExperience;

    /**
     * Creates an adapter for one network-backed experience source.
     *
     * @param source source represented by matching keys; {@code PLAYER} is not network-backed
     * @param matcher deterministic predicate selecting compatible AE keys; matchers passed together in one
     *                API operation must not overlap
     * @param storageUnitsPerExperience exact number of storage units representing one raw XP point
     */
    public ExperienceStorageAdapter(ExperienceMath.ExperienceSource source, Predicate<AEKey> matcher,
            long storageUnitsPerExperience) {
        this.source = Objects.requireNonNull(source, "source");
        if (source == ExperienceMath.ExperienceSource.PLAYER) {
            throw new IllegalArgumentException("PLAYER is not an AE storage source");
        }
        this.matcher = Objects.requireNonNull(matcher, "matcher");
        if (storageUnitsPerExperience <= 0) {
            throw new IllegalArgumentException("storageUnitsPerExperience must be positive");
        }
        this.storageUnitsPerExperience = storageUnitsPerExperience;
    }

    public ExperienceMath.ExperienceSource source() {
        return source;
    }

    public long storageUnitsPerExperience() {
        return storageUnitsPerExperience;
    }

    public boolean matches(AEKey key) {
        return key != null && matcher.test(key);
    }

    /** Converts complete storage-unit groups to raw XP, leaving partial groups unavailable. */
    public long toExperience(long storageUnits) {
        requireNonNegative(storageUnits, "storageUnits");
        return storageUnits / storageUnitsPerExperience;
    }

    /** Converts raw XP to the exact storage units that must be extracted. */
    public long toStorageUnits(long experience) {
        requireNonNegative(experience, "experience");
        return Math.multiplyExact(experience, storageUnitsPerExperience);
    }

    private static void requireNonNegative(long value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must be non-negative");
        }
    }
}
