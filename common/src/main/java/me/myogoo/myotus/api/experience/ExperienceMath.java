package me.myogoo.myotus.api.experience;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Utility methods and stable identifiers for experience values used by Myotus integrations.
 *
 * <p>Applied Experienced stores experience as raw vanilla experience points. This class keeps that
 * representation explicit and provides vanilla level conversion helpers so addons can safely bridge
 * player experience, Applied Experienced values, and AE/fluid entries such as {@code fluid:xp}.</p>
 */
public final class ExperienceMath {
    /** Applied Experienced mod id. */
    public static final String APPLIED_EXPERIENCED_MOD_ID = "appex";

    /** Applied Experienced AE key id for raw experience points. */
    public static final String APPLIED_EXPERIENCED_AE_KEY_ID = "appex:experience";

    /** Applied Experienced item/block data component id storing raw experience points. */
    public static final String APPLIED_EXPERIENCED_AMOUNT_COMPONENT_ID = "appex:experience_amount";

    /** AE/fluid entry id commonly used for XP fluid stacks. */
    public static final String FLUID_XP_ID = "fluid:xp";

    /** Common fluid tag used by Applied Experienced on NeoForge for experience-compatible fluids. */
    public static final String COMMON_EXPERIENCE_FLUID_TAG_ID = "c:experience";

    /** Applied Experienced accepts/creates one vanilla experience bottle as seven experience points. */
    public static final int EXPERIENCE_PER_BOTTLE = 7;

    /** Applied Experienced's energy adapter converts one experience point to thirty-two AE. */
    public static final int AE_PER_EXPERIENCE = 32;

    /** Default consumption order for anvil-like UIs: player XP first, then fluid XP, then stored Applied Experienced amount. */
    public static final List<ExperienceSource> DEFAULT_ANVIL_SOURCE_PRIORITY = Collections.unmodifiableList(Arrays.asList(
            ExperienceSource.PLAYER,
            ExperienceSource.FLUID_XP,
            ExperienceSource.APPLIED_EXPERIENCED_AMOUNT));

    private ExperienceMath() {
    }

    /**
     * Raw XP source types understood by Myotus' experience helper APIs.
     */
    public enum ExperienceSource {
        /** Raw points currently owned by the player. */
        PLAYER,
        /** Raw points represented by {@code fluid:xp}. */
        FLUID_XP,
        /** Raw points stored in Applied Experienced's {@code appex:experience_amount}. */
        APPLIED_EXPERIENCED_AMOUNT
    }

    /**
     * Immutable result of spending a raw XP amount from multiple source pools in a caller-provided priority order.
     */
    public static final class ExperienceConsumptionPlan {
        private final long requiredExperience;
        private final long availableExperience;
        private final long playerExperienceUsed;
        private final long fluidXpUsed;
        private final long appliedExperiencedAmountUsed;
        private final long missingExperience;

        private ExperienceConsumptionPlan(long requiredExperience, long availableExperience, long playerExperienceUsed,
                long fluidXpUsed, long appliedExperiencedAmountUsed, long missingExperience) {
            this.requiredExperience = requiredExperience;
            this.availableExperience = availableExperience;
            this.playerExperienceUsed = playerExperienceUsed;
            this.fluidXpUsed = fluidXpUsed;
            this.appliedExperiencedAmountUsed = appliedExperiencedAmountUsed;
            this.missingExperience = missingExperience;
        }

        /** Raw XP points requested by the caller. */
        public long requiredExperience() {
            return requiredExperience;
        }

        /** Raw XP points available across all supplied sources. */
        public long availableExperience() {
            return availableExperience;
        }

        /** Raw player XP points to consume. */
        public long playerExperienceUsed() {
            return playerExperienceUsed;
        }

        /** Raw {@code fluid:xp} points to consume. */
        public long fluidXpUsed() {
            return fluidXpUsed;
        }

        /** Raw Applied Experienced amount points to consume. */
        public long appliedExperiencedAmountUsed() {
            return appliedExperiencedAmountUsed;
        }

        /** Raw XP points still missing after all sources are considered. Zero means the plan is payable. */
        public long missingExperience() {
            return missingExperience;
        }

        /** Returns {@code true} when all required XP can be paid from the supplied sources. */
        public boolean canPay() {
            return missingExperience == 0;
        }

        /** Returns the amount used for a specific source. */
        public long used(ExperienceSource source) {
            Objects.requireNonNull(source, "source");
            if (source == ExperienceSource.PLAYER) {
                return playerExperienceUsed;
            }
            if (source == ExperienceSource.FLUID_XP) {
                return fluidXpUsed;
            }
            return appliedExperiencedAmountUsed;
        }
    }

    /**
     * Adds Applied Experienced raw points and {@code fluid:xp} raw points into one vanilla XP total.
     *
     * @param appliedExperience raw points reported by Applied Experienced
     * @param fluidXp raw points represented by {@code fluid:xp}
     * @return combined raw vanilla experience points
     */
    public static long totalExperience(long appliedExperience, long fluidXp) {
        requireNonNegative(appliedExperience, "appliedExperience");
        requireNonNegative(fluidXp, "fluidXp");
        return Math.addExact(appliedExperience, fluidXp);
    }

    /**
     * Adds player XP, {@code fluid:xp}, and Applied Experienced amount raw points into one vanilla XP total.
     *
     * @param playerExperience raw player XP points
     * @param fluidXp raw points represented by {@code fluid:xp}
     * @param appliedExperienceAmount raw points stored by Applied Experienced's amount component
     * @return combined raw vanilla experience points
     */
    public static long totalExperience(long playerExperience, long fluidXp, long appliedExperienceAmount) {
        requireNonNegative(playerExperience, "playerExperience");
        requireNonNegative(fluidXp, "fluidXp");
        requireNonNegative(appliedExperienceAmount, "appliedExperienceAmount");
        return Math.addExact(Math.addExact(playerExperience, fluidXp), appliedExperienceAmount);
    }

    /**
     * Creates a consumption plan using {@link #DEFAULT_ANVIL_SOURCE_PRIORITY}.
     *
     * @param requiredExperience raw XP points to spend
     * @param playerExperience available raw player XP points
     * @param fluidXp available raw {@code fluid:xp} points
     * @param appliedExperienceAmount available raw Applied Experienced amount points
     * @return immutable source-by-source consumption plan
     */
    public static ExperienceConsumptionPlan consumeExperience(long requiredExperience, long playerExperience,
            long fluidXp, long appliedExperienceAmount) {
        return consumeExperience(requiredExperience, playerExperience, fluidXp, appliedExperienceAmount,
                DEFAULT_ANVIL_SOURCE_PRIORITY);
    }

    /**
     * Creates a consumption plan using the supplied source priority. Earlier sources are consumed first.
     *
     * <p>The returned plan is safe to inspect even when the supplied pools are insufficient: in that case
     * {@link ExperienceConsumptionPlan#canPay()} returns {@code false} and
     * {@link ExperienceConsumptionPlan#missingExperience()} reports the missing raw points.</p>
     *
     * @param requiredExperience raw XP points to spend
     * @param playerExperience available raw player XP points
     * @param fluidXp available raw {@code fluid:xp} points
     * @param appliedExperienceAmount available raw Applied Experienced amount points
     * @param sourcePriority source order to consume from; duplicate entries are ignored after their first use
     * @return immutable source-by-source consumption plan
     */
    public static ExperienceConsumptionPlan consumeExperience(long requiredExperience, long playerExperience,
            long fluidXp, long appliedExperienceAmount, List<ExperienceSource> sourcePriority) {
        requireNonNegative(requiredExperience, "requiredExperience");
        requireNonNegative(playerExperience, "playerExperience");
        requireNonNegative(fluidXp, "fluidXp");
        requireNonNegative(appliedExperienceAmount, "appliedExperienceAmount");
        Objects.requireNonNull(sourcePriority, "sourcePriority");

        long availableExperience = totalExperience(playerExperience, fluidXp, appliedExperienceAmount);
        Map<ExperienceSource, Long> remainingBySource = new EnumMap<>(ExperienceSource.class);
        remainingBySource.put(ExperienceSource.PLAYER, playerExperience);
        remainingBySource.put(ExperienceSource.FLUID_XP, fluidXp);
        remainingBySource.put(ExperienceSource.APPLIED_EXPERIENCED_AMOUNT, appliedExperienceAmount);

        Map<ExperienceSource, Long> usedBySource = new EnumMap<>(ExperienceSource.class);
        for (ExperienceSource source : ExperienceSource.values()) {
            usedBySource.put(source, 0L);
        }

        long remainingRequirement = requiredExperience;
        for (ExperienceSource source : sourcePriority) {
            Objects.requireNonNull(source, "sourcePriority contains null");
            if (remainingRequirement == 0 || usedBySource.get(source) > 0) {
                continue;
            }
            long availableFromSource = remainingBySource.get(source);
            long used = Math.min(availableFromSource, remainingRequirement);
            usedBySource.put(source, used);
            remainingRequirement -= used;
        }

        return new ExperienceConsumptionPlan(
                requiredExperience,
                availableExperience,
                usedBySource.get(ExperienceSource.PLAYER),
                usedBySource.get(ExperienceSource.FLUID_XP),
                usedBySource.get(ExperienceSource.APPLIED_EXPERIENCED_AMOUNT),
                remainingRequirement);
    }

    /**
     * Returns the amount of raw vanilla experience needed to reach the start of {@code level}.
     *
     * @param level vanilla experience level
     * @return total raw experience points at that level boundary
     */
    public static long totalExperienceForLevel(int level) {
        requireNonNegative(level, "level");
        if (level <= 16) {
            return (long) level * level + 6L * level;
        }
        if (level <= 31) {
            return Math.floorDiv(5L * level * level - 81L * level + 720L, 2L);
        }
        return Math.floorDiv(9L * level * level - 325L * level + 4440L, 2L);
    }

    /**
     * Returns the vanilla level that contains {@code totalExperience}.
     *
     * @param totalExperience raw vanilla experience points
     * @return level at or below the supplied total
     */
    public static int levelForTotalExperience(long totalExperience) {
        requireNonNegative(totalExperience, "totalExperience");
        int level = 0;
        while (totalExperienceForLevel(level + 1) <= totalExperience) {
            level++;
        }
        return level;
    }

    /**
     * Returns how many raw points are already filled inside the current level.
     *
     * @param totalExperience raw vanilla experience points
     * @return progress points within {@link #levelForTotalExperience(long)}
     */
    public static long experienceIntoLevel(long totalExperience) {
        int level = levelForTotalExperience(totalExperience);
        return totalExperience - totalExperienceForLevel(level);
    }

    /**
     * Returns how many raw points are required to advance from {@code level} to the next level.
     *
     * @param level vanilla experience level
     * @return raw points required for the next level
     */
    public static long experienceToNextLevel(int level) {
        requireNonNegative(level, "level");
        return totalExperienceForLevel(level + 1) - totalExperienceForLevel(level);
    }

    private static void requireNonNegative(long value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must be non-negative");
        }
    }
}
