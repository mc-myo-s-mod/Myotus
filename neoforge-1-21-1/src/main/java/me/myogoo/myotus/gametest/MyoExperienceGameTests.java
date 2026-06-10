package me.myogoo.myotus.gametest;

import me.myogoo.myotus.Myotus;
import me.myogoo.myotus.api.MyotusAPI;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public final class MyoExperienceGameTests {
    private MyoExperienceGameTests() {
    }

    @GameTest(templateNamespace = Myotus.MODID, template = "empty", timeoutTicks = 20)
    public static void vanillaAnvilCostScalesWithCurrentPlayerLevel(GameTestHelper helper) {
        long costFromLevel100 = MyotusAPI.experience().vanillaAnvilExperienceCost(100, 30);
        long costFromLevel30 = MyotusAPI.experience().vanillaAnvilExperienceCost(30, 30);

        helper.assertTrue(costFromLevel100 > costFromLevel30,
                "Vanilla anvil cost must scale with the current player level");
        helper.assertTrue(costFromLevel30 == MyotusAPI.experience().totalExperienceForLevel(30),
                "Removing 30 levels from level 30 should equal the raw XP for level 30");
        helper.succeed();
    }

    @GameTest(templateNamespace = Myotus.MODID, template = "empty", timeoutTicks = 20)
    public static void apothicAnvilCostIgnoresCurrentPlayerLevel(GameTestHelper helper) {
        long expected = MyotusAPI.experience().totalExperienceForLevel(30);

        helper.assertTrue(MyotusAPI.experience().apothicAnvilExperienceCost(30) == expected,
                "Apothic anvil cost should be raw XP for the displayed level cost");
        helper.assertTrue(MyotusAPI.experience().vanillaAnvilExperienceCost(100, 30) > expected,
                "High-level vanilla anvil cost should be larger than Apothic optimal level cost");
        helper.succeed();
    }

    @GameTest(templateNamespace = Myotus.MODID, template = "empty", timeoutTicks = 20)
    public static void apothicEnchantingTableCostSumsSlotSteps(GameTestHelper helper) {
        long level30Step = MyotusAPI.experience().experienceToNextLevel(29);
        long level29Step = MyotusAPI.experience().experienceToNextLevel(28);
        long level28Step = MyotusAPI.experience().experienceToNextLevel(27);

        helper.assertTrue(MyotusAPI.experience().apothicEnchantingTableExperienceCost(30, 0) == level30Step - 1,
                "Slot 0 should charge the level step cost minus one");
        helper.assertTrue(MyotusAPI.experience().apothicEnchantingTableExperienceCost(30, 1) == level30Step + level29Step - 1,
                "Slot 1 should sum two descending level step costs minus one");
        helper.assertTrue(MyotusAPI.experience().apothicEnchantingTableExperienceCost(30, 2) == level30Step + level29Step + level28Step - 1,
                "Slot 2 should sum three descending level step costs minus one");
        helper.succeed();
    }

    @GameTest(templateNamespace = Myotus.MODID, template = "empty", timeoutTicks = 20)
    public static void apothicLibraryPointsAreNotRawExperience(GameTestHelper helper) {
        helper.assertTrue(MyotusAPI.experience().apothicLibraryPointsForLevel(1) == 1,
                "Level 1 enchantments should be worth one library point");
        helper.assertTrue(MyotusAPI.experience().apothicLibraryPointsForLevel(5) == 16,
                "Apothic library points should double for each enchantment level");
        helper.assertTrue(MyotusAPI.experience().apothicLibraryPointsForLevel(5)
                        != MyotusAPI.experience().totalExperienceForLevel(5),
                "Apothic library points must stay separate from raw vanilla XP");
        helper.succeed();
    }

    @GameTest(templateNamespace = Myotus.MODID, template = "empty", timeoutTicks = 20)
    public static void consumptionPlanExhaustsPrioritySourcesInOrder(GameTestHelper helper) {
        var plan = MyotusAPI.experience().consumeExperience(75, 30, 40, 50);

        helper.assertTrue(plan.canPay(), "Plan should be payable across player, fluid XP, and Applied Experienced sources");
        helper.assertTrue(plan.playerExperienceUsed() == 30, "Player XP should be exhausted first by default");
        helper.assertTrue(plan.fluidXpUsed() == 40, "Fluid XP should be exhausted second by default");
        helper.assertTrue(plan.appliedExperiencedAmountUsed() == 5,
                "Applied Experienced amount should only pay the remaining default anvil cost");
        helper.assertTrue(plan.missingExperience() == 0, "Payable plan should not report missing XP");
        helper.succeed();
    }

    @GameTest(templateNamespace = Myotus.MODID, template = "empty", timeoutTicks = 20)
    public static void consumptionPlanReportsMissingExperience(GameTestHelper helper) {
        var plan = MyotusAPI.experience().consumeExperience(100, 10, 20, 30);

        helper.assertFalse(plan.canPay(), "Plan should not be payable when all sources are insufficient");
        helper.assertTrue(plan.playerExperienceUsed() == 10, "Insufficient plan should consume all player XP");
        helper.assertTrue(plan.fluidXpUsed() == 20, "Insufficient plan should consume all fluid XP");
        helper.assertTrue(plan.appliedExperiencedAmountUsed() == 30,
                "Insufficient plan should consume all Applied Experienced amount");
        helper.assertTrue(plan.missingExperience() == 40, "Missing XP should equal the unpaid remainder");
        helper.succeed();
    }
}
