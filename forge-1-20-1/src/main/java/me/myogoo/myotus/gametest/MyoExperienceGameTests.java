package me.myogoo.myotus.gametest;

import me.myogoo.myotus.Myotus;
import me.myogoo.myotus.api.MyotusAPI;
import me.myogoo.myotus.api.experience.ExperienceMath;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static me.myogoo.myotus.api.experience.ExperienceMath.ExperienceSource.APPLIED_EXPERIENCED_AMOUNT;
import static me.myogoo.myotus.api.experience.ExperienceMath.ExperienceSource.FLUID_XP;
import static me.myogoo.myotus.api.experience.ExperienceMath.ExperienceSource.PLAYER;

@GameTestHolder(Myotus.MODID)
@PrefixGameTestTemplate(false)
public final class MyoExperienceGameTests {
    private MyoExperienceGameTests() {
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void exposesStableExperienceIntegrationIds(GameTestHelper helper) {
        assertEquals(helper, "appex", MyotusAPI.experience().appliedExperiencedModId(), "Applied Experienced mod id");
        assertEquals(helper, "appex:experience", MyotusAPI.experience().appliedExperiencedAeKeyId(),
                "Applied Experienced AE key id");
        assertEquals(helper, "appex:experience_amount", MyotusAPI.experience().appliedExperiencedAmountComponentId(),
                "Applied Experienced amount component id");
        assertEquals(helper, "fluid:xp", MyotusAPI.experience().fluidXpId(), "Fluid XP id");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void vanillaLevelConversionsRoundTripBoundaryValues(GameTestHelper helper) {
        assertEquals(helper, 0L, MyotusAPI.experience().totalExperienceForLevel(0), "Level 0 total XP");
        assertEquals(helper, 7L, MyotusAPI.experience().totalExperienceForLevel(1), "Level 1 total XP");
        assertEquals(helper, 315L, MyotusAPI.experience().totalExperienceForLevel(15), "Level 15 total XP");
        assertEquals(helper, 352L, MyotusAPI.experience().totalExperienceForLevel(16), "Level 16 total XP");
        assertEquals(helper, 1395L, MyotusAPI.experience().totalExperienceForLevel(30), "Level 30 total XP");
        assertEquals(helper, 1628L, MyotusAPI.experience().totalExperienceForLevel(32), "Level 32 total XP");

        assertEquals(helper, 0, MyotusAPI.experience().levelForTotalExperience(6), "6 XP is still level 0");
        assertEquals(helper, 1, MyotusAPI.experience().levelForTotalExperience(7), "7 XP reaches level 1");
        assertEquals(helper, 31, MyotusAPI.experience().levelForTotalExperience(1627), "1627 XP is level 31");
        assertEquals(helper, 32, MyotusAPI.experience().levelForTotalExperience(1628), "1628 XP reaches level 32");
        assertEquals(helper, 120L, MyotusAPI.experience().experienceIntoLevel(1627), "XP progress within level 31");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void anvilPriorityIncludesFluidXpOnlyWhenAvailable(GameTestHelper helper) {
        assertEquals(helper, List.of(PLAYER, APPLIED_EXPERIENCED_AMOUNT), MyotusAPI.experience().anvilSourcePriority(0),
                "No fluid XP should skip the fluid source");
        assertEquals(helper, List.of(PLAYER, FLUID_XP, APPLIED_EXPERIENCED_AMOUNT),
                MyotusAPI.experience().anvilSourcePriority(1), "Positive fluid XP should include the fluid source");
        assertEquals(helper, List.of(PLAYER, APPLIED_EXPERIENCED_AMOUNT),
                MyotusAPI.experience().anvilSourcePriority(false), "Explicit false should skip fluid XP");
        assertEquals(helper, List.of(PLAYER, FLUID_XP, APPLIED_EXPERIENCED_AMOUNT),
                MyotusAPI.experience().anvilSourcePriority(true), "Explicit true should include fluid XP");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void myoExperienceUsesDefaultAnvilPriority(GameTestHelper helper) {
        ExperienceMath.MyoExperience experience = MyotusAPI.experience().consumeExperience(75, 30, 40, 50);

        assertTrue(helper, experience.enough(), "MyoExperience should be enough across all default sources");
        assertEquals(helper, 75L, experience.required(), "Required XP");
        assertEquals(helper, 120L, experience.available(), "Available XP");
        assertEquals(helper, 120L, experience.spendable(), "Spendable XP should include all default priority sources");
        assertEquals(helper, 30L, experience.player(), "Player XP should be exhausted first");
        assertEquals(helper, 40L, experience.fluidXp(), "Fluid XP should be exhausted second");
        assertEquals(helper, 5L, experience.appliedExperiencedAmount(),
                "Applied Experienced amount should pay the remainder");
        assertEquals(helper, 0L, experience.missing(), "Enough MyoExperience should not miss XP");
        assertEquals(helper, 40L, experience.used(FLUID_XP), "used(source) should match the source total");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void myoExperienceSupportsCustomPriorityAndMissingExperience(GameTestHelper helper) {
        ExperienceMath.MyoExperience customExperience = MyotusAPI.experience().consumeExperience(75, 30, 40, 50,
                List.of(APPLIED_EXPERIENCED_AMOUNT, FLUID_XP, PLAYER));
        assertTrue(helper, customExperience.enough(), "Custom priority experience should be enough");
        assertEquals(helper, 0L, customExperience.player(), "Custom priority should leave player XP untouched");
        assertEquals(helper, 25L, customExperience.fluidXp(), "Fluid XP should pay the custom-priority remainder");
        assertEquals(helper, 50L, customExperience.appliedExperiencedAmount(),
                "Applied Experienced amount should be consumed first by custom priority");

        ExperienceMath.MyoExperience missingExperience = MyotusAPI.experience().consumeExperience(75, 30, 40, 50,
                List.of(PLAYER, FLUID_XP));
        assertTrue(helper, !missingExperience.enough(), "MyoExperience should be missing XP when priority excludes a needed source");
        assertEquals(helper, 120L, missingExperience.available(), "Available XP should include non-priority sources");
        assertEquals(helper, 70L, missingExperience.spendable(), "Spendable XP should include priority sources only");
        assertEquals(helper, 30L, missingExperience.player(), "Missing experience should consume player XP");
        assertEquals(helper, 40L, missingExperience.fluidXp(), "Missing experience should consume fluid XP");
        assertEquals(helper, 0L, missingExperience.appliedExperiencedAmount(),
                "Excluded source should not be consumed");
        assertEquals(helper, 5L, missingExperience.missing(), "Missing XP should equal the unpaid remainder");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void vanillaAndApothicCostsStaySemanticallySeparate(GameTestHelper helper) {
        long vanillaCost = MyotusAPI.experience().vanillaAnvilExperienceCost(100, 30);
        long apothicCost = MyotusAPI.experience().apothicAnvilExperienceCost(30);

        assertEquals(helper, MyotusAPI.experience().totalExperienceForLevel(100)
                - MyotusAPI.experience().totalExperienceForLevel(70), vanillaCost,
                "Vanilla anvil cost should remove levels from the current player level");
        assertEquals(helper, MyotusAPI.experience().totalExperienceForLevel(30), apothicCost,
                "Apothic anvil cost should be the raw XP value of the displayed level cost");
        assertTrue(helper, vanillaCost > apothicCost, "High-level vanilla anvil cost should exceed Apothic cost");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void apothicEnchantingAndLibraryCalculationsStaySeparate(GameTestHelper helper) {
        long level30Step = MyotusAPI.experience().experienceToNextLevel(29);
        long level29Step = MyotusAPI.experience().experienceToNextLevel(28);
        long level28Step = MyotusAPI.experience().experienceToNextLevel(27);

        assertEquals(helper, level30Step - 1, MyotusAPI.experience().apothicEnchantingTableExperienceCost(30, 0),
                "Slot 0 should charge the level step cost minus one");
        assertEquals(helper, level30Step + level29Step - 1,
                MyotusAPI.experience().apothicEnchantingTableExperienceCost(30, 1),
                "Slot 1 should sum two descending level step costs minus one");
        assertEquals(helper, level30Step + level29Step + level28Step - 1,
                MyotusAPI.experience().apothicEnchantingTableExperienceCost(30, 2),
                "Slot 2 should sum three descending level step costs minus one");
        assertEquals(helper, 16L, MyotusAPI.experience().apothicLibraryPointsForLevel(5),
                "Apothic library points should double per enchantment level");
        assertTrue(helper, MyotusAPI.experience().apothicLibraryPointsForLevel(5)
                        != MyotusAPI.experience().totalExperienceForLevel(5),
                "Apothic library points must not be treated as raw vanilla XP");
        helper.succeed();
    }

    private static void assertTrue(GameTestHelper helper, boolean condition, String message) {
        helper.assertTrue(condition, message);
    }

    private static void assertEquals(GameTestHelper helper, long expected, long actual, String message) {
        helper.assertTrue(expected == actual, message + ": expected=" + expected + ", actual=" + actual);
    }

    private static void assertEquals(GameTestHelper helper, int expected, int actual, String message) {
        helper.assertTrue(expected == actual, message + ": expected=" + expected + ", actual=" + actual);
    }

    private static void assertEquals(GameTestHelper helper, Object expected, Object actual, String message) {
        helper.assertTrue(expected.equals(actual), message + ": expected=" + expected + ", actual=" + actual);
    }
}
