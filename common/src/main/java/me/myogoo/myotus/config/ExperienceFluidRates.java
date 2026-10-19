package me.myogoo.myotus.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/** Immutable, server-configured mB required for one raw experience point. */
public record ExperienceFluidRates(long defaultRate, Map<String, Long> overrides) {
    public static final int DEFAULT_RATE = 20;
    public static final List<String> DEFAULT_OVERRIDES = List.of("create_enchantment_industry:experience=1");
    private static final Pattern FLUID_ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9/._-]+");

    public ExperienceFluidRates {
        requireValidRate(defaultRate);
        overrides = Map.copyOf(overrides);
        overrides.forEach((id, rate) -> {
            if (!FLUID_ID.matcher(id).matches()) {
                throw new IllegalArgumentException("Invalid fluid ID: " + id);
            }
            requireValidRate(rate);
        });
    }

    public static ExperienceFluidRates defaults() {
        return parse(DEFAULT_RATE, DEFAULT_OVERRIDES);
    }

    /** Parses exact fluid IDs; a later entry overrides an earlier entry for the same ID. */
    public static ExperienceFluidRates parse(long defaultRate, List<? extends String> entries) {
        Map<String, Long> overrides = new LinkedHashMap<>();
        for (String entry : entries) {
            if (!isValidOverride(entry)) {
                throw new IllegalArgumentException("Expected fluid_id=positive_mB_per_XP: " + entry);
            }
            int separator = entry.indexOf('=');
            overrides.put(entry.substring(0, separator).trim(),
                    Long.parseLong(entry.substring(separator + 1).trim()));
        }
        return new ExperienceFluidRates(defaultRate, overrides);
    }

    public static boolean isValidOverride(Object value) {
        if (!(value instanceof String entry)) {
            return false;
        }
        int separator = entry.indexOf('=');
        if (separator < 0 || !FLUID_ID.matcher(entry.substring(0, separator).trim()).matches()) {
            return false;
        }
        try {
            long rate = Long.parseLong(entry.substring(separator + 1).trim());
            return rate > 0 && rate <= Integer.MAX_VALUE;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    public long rate(String fluidId) {
        return overrides.getOrDefault(fluidId, defaultRate);
    }

    private static void requireValidRate(long rate) {
        if (rate <= 0 || rate > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("mB per experience must be between 1 and " + Integer.MAX_VALUE);
        }
    }
}
