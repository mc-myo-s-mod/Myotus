package me.myogoo.myotus.api.experience;

import org.junit.jupiter.api.Test;

import java.util.List;

import static me.myogoo.myotus.api.experience.ExperienceMath.ExperienceSource.APPLIED_EXPERIENCED_AMOUNT;
import static me.myogoo.myotus.api.experience.ExperienceMath.ExperienceSource.FLUID_XP;
import static me.myogoo.myotus.api.experience.ExperienceMath.ExperienceSource.PLAYER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExperienceMathTest {
    @Test
    void exposesAppliedExperiencedAndFluidXpIdentifiers() {
        assertEquals("appex", ExperienceMath.APPLIED_EXPERIENCED_MOD_ID);
        assertEquals("appex:experience", ExperienceMath.APPLIED_EXPERIENCED_AE_KEY_ID);
        assertEquals("appex:experience_amount", ExperienceMath.APPLIED_EXPERIENCED_AMOUNT_COMPONENT_ID);
        assertEquals("fluid:xp", ExperienceMath.FLUID_XP_ID);
    }

    @Test
    void convertsMinecraftLevelsToTotalExperiencePoints() {
        assertEquals(0, ExperienceMath.totalExperienceForLevel(0));
        assertEquals(7, ExperienceMath.totalExperienceForLevel(1));
        assertEquals(315, ExperienceMath.totalExperienceForLevel(15));
        assertEquals(352, ExperienceMath.totalExperienceForLevel(16));
        assertEquals(394, ExperienceMath.totalExperienceForLevel(17));
        assertEquals(1395, ExperienceMath.totalExperienceForLevel(30));
        assertEquals(1507, ExperienceMath.totalExperienceForLevel(31));
        assertEquals(1628, ExperienceMath.totalExperienceForLevel(32));
    }

    @Test
    void convertsTotalExperiencePointsBackToLevelsAndProgress() {
        assertEquals(0, ExperienceMath.levelForTotalExperience(0));
        assertEquals(0, ExperienceMath.levelForTotalExperience(6));
        assertEquals(1, ExperienceMath.levelForTotalExperience(7));
        assertEquals(15, ExperienceMath.levelForTotalExperience(315));
        assertEquals(31, ExperienceMath.levelForTotalExperience(1627));
        assertEquals(32, ExperienceMath.levelForTotalExperience(1628));

        assertEquals(6, ExperienceMath.experienceIntoLevel(6));
        assertEquals(0, ExperienceMath.experienceIntoLevel(7));
        assertEquals(120, ExperienceMath.experienceIntoLevel(1627));
    }

    @Test
    void combinesAppliedExperiencedPointsAndFluidXpPoints() {
        assertEquals(42, ExperienceMath.totalExperience(40, 2));
        assertEquals(47, ExperienceMath.totalExperience(40, 7));
    }

    @Test
    void combinesPlayerFluidAndAppliedExperiencedAmountPoints() {
        assertEquals(57, ExperienceMath.totalExperience(10, 20, 27));
    }

    @Test
    void worksWithOnlyFluidXpWhenAppliedExperiencedIsAbsent() {
        assertEquals(7, ExperienceMath.totalExperience(0, 7));
        assertEquals(1, ExperienceMath.levelForTotalExperience(ExperienceMath.totalExperience(0, 7)));
        assertEquals(0, ExperienceMath.experienceIntoLevel(ExperienceMath.totalExperience(0, 7)));
    }

    @Test
    void consumesExperienceUsingDefaultAnvilPriority() {
        var plan = ExperienceMath.consumeExperience(75, 30, 40, 50);

        assertTrue(plan.canPay());
        assertEquals(75, plan.requiredExperience());
        assertEquals(120, plan.availableExperience());
        assertEquals(30, plan.playerExperienceUsed());
        assertEquals(40, plan.fluidXpUsed());
        assertEquals(5, plan.appliedExperiencedAmountUsed());
        assertEquals(0, plan.missingExperience());
        assertEquals(30, plan.used(PLAYER));
        assertEquals(40, plan.used(FLUID_XP));
        assertEquals(5, plan.used(APPLIED_EXPERIENCED_AMOUNT));
    }

    @Test
    void consumesExperienceUsingCustomPriority() {
        var plan = ExperienceMath.consumeExperience(75, 30, 40, 50,
                List.of(APPLIED_EXPERIENCED_AMOUNT, FLUID_XP, PLAYER));

        assertTrue(plan.canPay());
        assertEquals(0, plan.playerExperienceUsed());
        assertEquals(25, plan.fluidXpUsed());
        assertEquals(50, plan.appliedExperiencedAmountUsed());
    }

    @Test
    void reportsMissingExperienceWhenPrioritySourcesAreInsufficient() {
        var plan = ExperienceMath.consumeExperience(75, 30, 40, 50, List.of(PLAYER, FLUID_XP));

        assertFalse(plan.canPay());
        assertEquals(30, plan.playerExperienceUsed());
        assertEquals(40, plan.fluidXpUsed());
        assertEquals(0, plan.appliedExperiencedAmountUsed());
        assertEquals(5, plan.missingExperience());
    }

    @Test
    void ignoresDuplicatePriorityEntriesAfterFirstUse() {
        var plan = ExperienceMath.consumeExperience(70, 30, 40, 50, List.of(PLAYER, PLAYER, FLUID_XP));

        assertTrue(plan.canPay());
        assertEquals(30, plan.playerExperienceUsed());
        assertEquals(40, plan.fluidXpUsed());
        assertEquals(0, plan.appliedExperiencedAmountUsed());
    }

    @Test
    void rejectsNegativeExperienceInputs() {
        assertThrows(IllegalArgumentException.class, () -> ExperienceMath.totalExperience(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> ExperienceMath.totalExperience(0, -1));
        assertThrows(IllegalArgumentException.class, () -> ExperienceMath.totalExperience(-1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> ExperienceMath.totalExperience(0, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> ExperienceMath.totalExperience(0, 0, -1));
        assertThrows(IllegalArgumentException.class, () -> ExperienceMath.consumeExperience(-1, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> ExperienceMath.consumeExperience(0, -1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> ExperienceMath.consumeExperience(0, 0, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> ExperienceMath.consumeExperience(0, 0, 0, -1));
        assertThrows(IllegalArgumentException.class, () -> ExperienceMath.totalExperienceForLevel(-1));
        assertThrows(IllegalArgumentException.class, () -> ExperienceMath.levelForTotalExperience(-1));
    }
}
