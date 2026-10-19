package me.myogoo.myotus.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public final class MyotusServerConfig {
    public static final MyotusServerConfig SERVER = new MyotusServerConfig();
    private final ForgeConfigSpec spec;
    public final ForgeConfigSpec.IntValue defaultFluidMbPerExperience;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> fluidMbPerExperienceOverrides;

    private MyotusServerConfig() {
        var builder = new ForgeConfigSpec.Builder();
        builder.push("experience");
        defaultFluidMbPerExperience = builder
                .comment("Default mB required for one raw XP point from recognized experience fluids.")
                .worldRestart()
                .defineInRange("defaultFluidMbPerExperience", ExperienceFluidRates.DEFAULT_RATE, 1, Integer.MAX_VALUE);
        fluidMbPerExperienceOverrides = builder
                .comment("Fluid ID overrides in the form namespace:fluid=positive_mB_per_XP.",
                        "Listed fluids are recognized even without an experience-fluid tag.",
                        "Later entries for the same ID win. An empty list removes all exceptions.")
                .worldRestart()
                .defineListAllowEmpty("fluidMbPerExperienceOverrides", ExperienceFluidRates.DEFAULT_OVERRIDES,
                        ExperienceFluidRates::isValidOverride);
        builder.pop();
        spec = builder.build();
    }

    public ForgeConfigSpec get() {
        return spec;
    }

    public static ExperienceFluidRates getExperienceFluidRates() {
        return SERVER.spec.isLoaded()
                ? ExperienceFluidRates.parse(SERVER.defaultFluidMbPerExperience.get(),
                        SERVER.fluidMbPerExperienceOverrides.get())
                : ExperienceFluidRates.defaults();
    }
}
