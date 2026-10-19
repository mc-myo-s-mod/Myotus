package me.myogoo.myotus.api.experience;

import appeng.api.stacks.AEKey;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;

/**
 * Converts one AE storage key family between storage units and normalized raw experience points.
 *
 * <p>AE fluid amounts are storage units (mB), not experience points. Explicit adapters supply their
 * conversion; Myotus' configured fluid adapter snapshots server defaults and fluid-ID exceptions.</p>
 */
public final class ExperienceStorageAdapter {
    private final ExperienceMath.ExperienceSource source;
    private final Predicate<AEKey> matcher;
    private final long storageUnitsPerExperience;
    private final ToLongFunction<AEKey> rateByKey;

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
        this.rateByKey = null;
    }

    /** Creates a per-key conversion; the matcher and positive rates must remain stable during an operation. */
    public ExperienceStorageAdapter(ExperienceMath.ExperienceSource source, Predicate<AEKey> matcher,
            ToLongFunction<AEKey> rateByKey) {
        this.source = Objects.requireNonNull(source, "source");
        if (source == ExperienceMath.ExperienceSource.PLAYER) {
            throw new IllegalArgumentException("PLAYER is not an AE storage source");
        }
        this.matcher = Objects.requireNonNull(matcher, "matcher");
        this.rateByKey = Objects.requireNonNull(rateByKey, "rateByKey");
        this.storageUnitsPerExperience = 0;
    }

    public ExperienceMath.ExperienceSource source() {
        return source;
    }

    public long storageUnitsPerExperience() {
        if (hasVariableConversion()) {
            throw new IllegalStateException("A storage key is required for a per-key conversion");
        }
        return storageUnitsPerExperience;
    }

    public boolean hasVariableConversion() {
        return rateByKey != null;
    }

    public long storageUnitsPerExperience(AEKey key) {
        Objects.requireNonNull(key, "key");
        long rate = rateByKey == null ? storageUnitsPerExperience : rateByKey.applyAsLong(key);
        if (rate <= 0) {
            throw new IllegalArgumentException("storageUnitsPerExperience must be positive");
        }
        return rate;
    }

    public boolean matches(AEKey key) {
        return key != null && matcher.test(key);
    }

    /** Converts complete storage-unit groups to raw XP, leaving partial groups unavailable. */
    public long toExperience(long storageUnits) {
        requireNonNegative(storageUnits, "storageUnits");
        return storageUnits / storageUnitsPerExperience();
    }

    /** Converts raw XP to the exact storage units that must be extracted. */
    public long toStorageUnits(long experience) {
        requireNonNegative(experience, "experience");
        return Math.multiplyExact(experience, storageUnitsPerExperience());
    }

    private static void requireNonNegative(long value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must be non-negative");
        }
    }
}
