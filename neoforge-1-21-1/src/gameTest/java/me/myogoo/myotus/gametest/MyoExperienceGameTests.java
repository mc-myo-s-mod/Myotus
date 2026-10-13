package me.myogoo.myotus.gametest;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.ILinkStatus;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.MEStorage;
import appeng.api.util.IConfigManager;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import appeng.menu.ISubMenu;
import appeng.menu.locator.MenuLocators;
import appeng.menu.me.common.MEStorageMenu;
import appeng.parts.reporting.ItemTerminalPart;
import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import me.myogoo.myotus.Myotus;
import me.myogoo.myotus.api.MyotusAPI;
import me.myogoo.myotus.api.experience.ExperienceMath;
import me.myogoo.myotus.api.experience.ExperienceStorageAdapter;
import me.myogoo.myotus.config.MyotusConfig;
import me.myogoo.myotus.init.MyoItems;
import me.myogoo.myotus.menu.MyoSlotSemantics;
import me.myogoo.myotus.menu.PlayerUpgradeContainer;
import me.myogoo.myotus.menu.TerminalUpgradeHost;
import me.myogoo.myotus.menu.TerminalUpgradeStorageKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static me.myogoo.myotus.api.experience.ExperienceMath.ExperienceSource.APPLIED_EXPERIENCED_AMOUNT;
import static me.myogoo.myotus.api.experience.ExperienceMath.ExperienceSource.FLUID_XP;
import static me.myogoo.myotus.api.experience.ExperienceMath.ExperienceSource.PLAYER;

@GameTestHolder(Myotus.MODID)
@PrefixGameTestTemplate(false)
public final class MyoExperienceGameTests {
    private MyoExperienceGameTests() {
    }

    @GameTestGenerator
    public static List<TestFunction> wirelessRecipeTests() {
        if (!ModList.get().isLoaded("ae2wtlib")) {
            return List.of();
        }
        return List.of(new TestFunction("defaultBatch",
                "myoexperiencegametests.wirelessrecipespreservebothinputsacrossupgradeandrepeatedopen",
                "myotus:empty", 20, 0L, true,
                MyoExperienceGameTests::wirelessRecipesPreserveBothInputsAcrossUpgradeAndRepeatedOpen));
    }

    public static void wirelessRecipesPreserveBothInputsAcrossUpgradeAndRepeatedOpen(GameTestHelper helper) {
        var player = makeServerPlayer(helper);
        ItemStack crafting = wirelessItem(helper, "ae2:wireless_crafting_terminal");
        ItemStack encoding = wirelessItem(helper, "ae2wtlib:wireless_pattern_encoding_terminal");
        ItemStack access = wirelessItem(helper, "ae2wtlib:wireless_pattern_access_terminal");
        List<String> originalKeys = new ArrayList<>();
        for (ItemStack terminal : List.of(crafting, encoding, access)) {
            String key = TerminalUpgradeStorageKey.of(new TestTerminalHost(player, terminal));
            originalKeys.add(key);
            new PlayerUpgradeContainer(player, key).setItemDirect(0,
                    new ItemStack(MyoItems.MYOTUS_UPGRADE_CARD.get()));
        }
        List<ItemStack> beforeInputs = List.of(crafting.copy(), encoding.copy(), access.copy());
        CompoundTag beforePlayer = player.getPersistentData().copy();
        ItemStack combined = assembleWireless(helper, crafting, encoding);
        assertTrue(helper, ItemStack.matches(combined, assembleWireless(helper, crafting, encoding)),
                "Actual Combine recipe previews must produce identical component data");
        ItemStack upgraded = assembleWireless(helper, combined, access);
        assertTrue(helper, ItemStack.matches(upgraded, assembleWireless(helper, combined, access)),
                "Actual Upgrade recipe previews must produce identical component data");
        assertEquals(helper, beforePlayer, player.getPersistentData(),
                "Both real recipes must leave player storage untouched until opening");
        assertTrue(helper, ItemStack.matches(beforeInputs.get(0), crafting)
                        && ItemStack.matches(beforeInputs.get(1), encoding)
                        && ItemStack.matches(beforeInputs.get(2), access),
                "Recipe assembly must not modify input stacks");

        var host = new TestTerminalHost(player, upgraded);
        var migrated = new PlayerUpgradeContainer(player, host);
        assertTrue(helper, migrated.getStackInSlot(0).is(MyoItems.MYOTUS_UPGRADE_CARD.get()),
                "Opening the actual recipe result must install a source card");
        assertEquals(helper, 2, player.getInventory().countItem(MyoItems.MYOTUS_UPGRADE_CARD.get()),
                "The duplicate cards from all three inputs must be returned without loss");
        for (String key : originalKeys) {
            assertTrue(helper, !player.getPersistentData().contains(key), "Consumed source must be removed: " + key);
        }
        CompoundTag afterMigration = player.getPersistentData().copy();
        var reopened = new PlayerUpgradeContainer(player, host);
        assertTrue(helper, reopened.getStackInSlot(0).is(MyoItems.MYOTUS_UPGRADE_CARD.get()),
                "Reopening must load the saved destination card");
        assertEquals(helper, afterMigration, player.getPersistentData(), "Reopening must not reimport sources");
        assertEquals(helper, 2, player.getInventory().countItem(MyoItems.MYOTUS_UPGRADE_CARD.get()),
                "Reopening must not return duplicate cards again");
        helper.succeed();
    }

    private static ItemStack wirelessItem(GameTestHelper helper, String id) {
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(id)));
        assertTrue(helper, !stack.isEmpty(), "Required wireless terminal is registered: " + id);
        return stack;
    }

    private static ItemStack assembleWireless(GameTestHelper helper, ItemStack first, ItemStack second) {
        CraftingInput input = CraftingInput.of(2, 1, List.of(first, second));
        var recipe = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input,
                helper.getLevel()).orElseThrow();
        return recipe.value().assemble(input, helper.getLevel().registryAccess());
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void exposesStableExperienceIntegrationIds(GameTestHelper helper) {
        assertEquals(helper, "appex", MyotusAPI.experience().appliedModId(), "Applied Experienced mod id");
        assertEquals(helper, "appex:experience", MyotusAPI.experience().appliedAeKeyId(),
                "Applied Experienced AE key id");
        assertEquals(helper, "appex:experience_amount", MyotusAPI.experience().appliedAmountComponentId(),
                "Applied Experienced amount component id");
        assertEquals(helper, "fluid:xp", MyotusAPI.experience().fluidXpId(), "Fluid XP id");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void vanillaLevelConversionsRoundTripBoundaryValues(GameTestHelper helper) {
        assertEquals(helper, 0L, MyotusAPI.experience().totalForLevel(0), "Level 0 total XP");
        assertEquals(helper, 7L, MyotusAPI.experience().totalForLevel(1), "Level 1 total XP");
        assertEquals(helper, 315L, MyotusAPI.experience().totalForLevel(15), "Level 15 total XP");
        assertEquals(helper, 352L, MyotusAPI.experience().totalForLevel(16), "Level 16 total XP");
        assertEquals(helper, 1395L, MyotusAPI.experience().totalForLevel(30), "Level 30 total XP");
        assertEquals(helper, 1628L, MyotusAPI.experience().totalForLevel(32), "Level 32 total XP");

        assertEquals(helper, 0, MyotusAPI.experience().levelForTotal(6), "6 XP is still level 0");
        assertEquals(helper, 1, MyotusAPI.experience().levelForTotal(7), "7 XP reaches level 1");
        assertEquals(helper, 31, MyotusAPI.experience().levelForTotal(1627), "1627 XP is level 31");
        assertEquals(helper, 32, MyotusAPI.experience().levelForTotal(1628), "1628 XP reaches level 32");
        assertEquals(helper, 120L, MyotusAPI.experience().intoLevel(1627), "XP progress within level 31");
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
        ExperienceMath.MyoExperience experience = MyotusAPI.experience().plan(75,
                new ExperienceMath.ExperienceAmounts(30, 40, 50));

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
        ExperienceMath.ExperienceAmounts available = new ExperienceMath.ExperienceAmounts(30, 40, 50);
        ExperienceMath.MyoExperience customExperience = MyotusAPI.experience().plan(75, available,
                List.of(APPLIED_EXPERIENCED_AMOUNT, FLUID_XP, PLAYER));
        assertTrue(helper, customExperience.enough(), "Custom priority experience should be enough");
        assertEquals(helper, 0L, customExperience.player(), "Custom priority should leave player XP untouched");
        assertEquals(helper, 25L, customExperience.fluidXp(), "Fluid XP should pay the custom-priority remainder");
        assertEquals(helper, 50L, customExperience.appliedExperiencedAmount(),
                "Applied Experienced amount should be consumed first by custom priority");

        ExperienceMath.MyoExperience missingExperience = MyotusAPI.experience().plan(75, available,
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
        long vanillaCost = MyotusAPI.experience().vanillaAnvilCost(100, 30);
        long apothicCost = MyotusAPI.experience().apothicAnvilCost(30);

        assertEquals(helper, MyotusAPI.experience().totalForLevel(100)
                - MyotusAPI.experience().totalForLevel(70), vanillaCost,
                "Vanilla anvil cost should remove levels from the current player level");
        assertEquals(helper, MyotusAPI.experience().totalForLevel(30), apothicCost,
                "Apothic anvil cost should be the raw XP value of the displayed level cost");
        assertTrue(helper, vanillaCost > apothicCost, "High-level vanilla anvil cost should exceed Apothic cost");
        assertEquals(helper, 109L, MyotusAPI.experience().vanillaAnvilCost(30, 1, 0.5F),
                "Vanilla anvil cost should preserve the player's progress fraction");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void apothicEnchantingAndLibraryCalculationsStaySeparate(GameTestHelper helper) {
        long level30Step = MyotusAPI.experience().toNextLevel(29);
        long level29Step = MyotusAPI.experience().toNextLevel(28);
        long level28Step = MyotusAPI.experience().toNextLevel(27);

        assertEquals(helper, level30Step - 1, MyotusAPI.experience().apothicEnchantingTableCost(30, 0),
                "Slot 0 should charge the level step cost minus one");
        assertEquals(helper, level30Step + level29Step - 1,
                MyotusAPI.experience().apothicEnchantingTableCost(30, 1),
                "Slot 1 should sum two descending level step costs minus one");
        assertEquals(helper, level30Step + level29Step + level28Step - 1,
                MyotusAPI.experience().apothicEnchantingTableCost(30, 2),
                "Slot 2 should sum three descending level step costs minus one");
        assertEquals(helper, 16L, MyotusAPI.experience().apothicLibraryPointsForLevel(5),
                "Apothic library points should double per enchantment level");
        assertTrue(helper, MyotusAPI.experience().apothicLibraryPointsForLevel(5)
                        != MyotusAPI.experience().totalForLevel(5),
                "Apothic library points must not be treated as raw vanilla XP");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void ae2FluidAdapterUsesExplicitStorageUnitConversion(GameTestHelper helper) {
        AEFluidKey water = AEFluidKey.of(Fluids.WATER);
        MutableStorage storage = new MutableStorage(water, 499);
        TrackingEnergySource energy = new TrackingEnergySource(10);
        ExperienceStorageAdapter adapter = fluidAdapter(water);

        assertEquals(helper, 1L, MyotusAPI.experience().stored(storage, adapter),
                "499 mB at 250 mB per XP should expose one complete XP");
        assertTrue(helper, MyotusAPI.experience().extractExact(energy, storage, IActionSource.empty(), 1, adapter),
                "One complete XP should be extracted");
        assertEquals(helper, 249L, storage.stored(water), "Partial storage units must remain after extraction");
        assertEquals(helper, 8.0, energy.stored(), "AE2 should charge two AE for 250 mB of fluid");
        assertEquals(helper, 1, storage.modulatedExtractions(), "Exact extraction should mutate storage once");
        assertEquals(helper, 1, energy.modulatedExtractions(), "Exact extraction should mutate energy once");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void ae2SimulatedFluidExtractionIsSideEffectFree(GameTestHelper helper) {
        AEFluidKey water = AEFluidKey.of(Fluids.WATER);
        MutableStorage storage = new MutableStorage(water, 500);
        TrackingEnergySource energy = new TrackingEnergySource(4);
        ExperienceStorageAdapter adapter = fluidAdapter(water);

        assertEquals(helper, 2L, MyotusAPI.experience().extract(energy, storage, IActionSource.empty(), 2,
                adapter, Actionable.SIMULATE), "Simulation should report the two extractable XP");
        assertEquals(helper, 500L, storage.stored(water), "Simulation must not mutate ME storage");
        assertEquals(helper, 4.0, energy.stored(), "Simulation must not mutate AE energy");
        assertEquals(helper, 0, storage.modulatedExtractions(), "Simulation must not modulate storage");
        assertEquals(helper, 0, energy.modulatedExtractions(), "Simulation must not modulate energy");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void ae2ExactExtractionWithInsufficientEnergyIsAtomicBeforeModulation(GameTestHelper helper) {
        AEFluidKey water = AEFluidKey.of(Fluids.WATER);
        MutableStorage storage = new MutableStorage(water, 500);
        TrackingEnergySource energy = new TrackingEnergySource(1);
        ExperienceStorageAdapter adapter = fluidAdapter(water);

        assertTrue(helper, !MyotusAPI.experience().extractExact(energy, storage, IActionSource.empty(), 2, adapter),
                "Exact extraction must fail when AE2 cannot power the full request");
        assertEquals(helper, 500L, storage.stored(water), "Failed preflight must leave ME storage unchanged");
        assertEquals(helper, 1.0, energy.stored(), "Failed preflight must leave AE energy unchanged");
        assertEquals(helper, 0, storage.modulatedExtractions(), "Failed preflight must not modulate storage");
        assertEquals(helper, 0, energy.modulatedExtractions(), "Failed preflight must not modulate energy");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void ae2EnergySimulationIsCumulativeAcrossMatchingKeys(GameTestHelper helper) {
        AEFluidKey water = AEFluidKey.of(Fluids.WATER);
        AEFluidKey lava = AEFluidKey.of(Fluids.LAVA);
        MutableStorage storage = new MutableStorage(water, 250, lava, 250);
        TrackingEnergySource energy = new TrackingEnergySource(3);
        ExperienceStorageAdapter adapter = MyotusAPI.experience().fluidStorage(
                key -> key.equals(water) || key.equals(lava), 250);

        assertEquals(helper, 1L, MyotusAPI.experience().extractable(energy, storage, IActionSource.empty(), adapter),
                "Three AE must not be reused for both fluid keys during one simulation");
        assertEquals(helper, 250L, storage.stored(water), "Cumulative simulation must leave water unchanged");
        assertEquals(helper, 250L, storage.stored(lava), "Cumulative simulation must leave lava unchanged");
        assertEquals(helper, 3.0, energy.stored(), "Cumulative simulation must leave backing energy unchanged");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void ae2PlanningReleasesEnergyForIncompleteExperienceUnits(GameTestHelper helper) {
        AEFluidKey water = AEFluidKey.of(Fluids.WATER);
        AEFluidKey lava = AEFluidKey.of(Fluids.LAVA);
        MutableStorage storage = new MutableStorage(water, 250, lava, 125);
        TrackingEnergySource energy = new TrackingEnergySource(1.5);
        ExperienceStorageAdapter fluid = fluidAdapter(water);
        ExperienceStorageAdapter applied = new ExperienceStorageAdapter(
                APPLIED_EXPERIENCED_AMOUNT, lava::equals, 125);
        List<ExperienceStorageAdapter> adapters = List.of(fluid, applied);

        assertEquals(helper, List.of(APPLIED_EXPERIENCED_AMOUNT, PLAYER),
                MyotusAPI.experience().availableAnvilSourcePriority(energy, storage, IActionSource.empty(),
                        FLUID_XP, adapters),
                "An incomplete fluid unit must not reserve energy needed by the next source");

        var payment = MyotusAPI.experience().planPayment(energy, storage, IActionSource.empty(),
                0, 1, List.of(FLUID_XP, APPLIED_EXPERIENCED_AMOUNT, PLAYER), adapters);
        assertTrue(helper, payment.enough(), "The second adapter should still be able to pay one XP");
        assertEquals(helper, 0L, payment.fluidXp(), "The underpowered fluid adapter must pay nothing");
        assertEquals(helper, 1L, payment.appliedExperiencedAmount(), "The next adapter should pay one XP");
        assertEquals(helper, 0L, payment.player(), "No player fallback should be needed");
        assertEquals(helper, 250L, storage.stored(water), "Planning must not mutate water storage");
        assertEquals(helper, 125L, storage.stored(lava), "Planning must not mutate lava storage");
        assertEquals(helper, 1.5, energy.stored(), "Planning must not consume backing AE power");
        assertEquals(helper, 2, storage.reportCalls, "Availability and planning should each request one full report");

        var player = makeServerPlayer(helper);
        player.getAbilities().instabuild = false;
        player.giveExperiencePoints(165);
        assertTrue(helper, MyotusAPI.experience().canConsume(new TrackingEnergySource(100), storage,
                IActionSource.empty(), player, 5, List.of(PLAYER, FLUID_XP, APPLIED_EXPERIENCED_AMOUNT), adapters),
                "A player-first payment must remain affordable with two positive network adapters");
        assertEquals(helper, 3, storage.reportCalls, "canConsume must share one report across both adapters");
        assertEquals(helper, 165L, MyotusAPI.experience().playerRaw(player), "Payment simulation must not debit XP");
        assertEquals(helper, 250L, storage.stored(water), "Payment simulation must not debit fluid storage");
        assertEquals(helper, 125L, storage.stored(lava), "Payment simulation must not debit the second adapter");
        helper.succeed();
    }

    @SuppressWarnings("removal")
    @GameTest(template = "empty", timeoutTicks = 20)
    public static void ae2ConsumeDebitsNetworkAndServerPlayerExactly(GameTestHelper helper) {
        AEFluidKey water = AEFluidKey.of(Fluids.WATER);
        MutableStorage storage = new MutableStorage(water, 250);
        TrackingEnergySource energy = new TrackingEnergySource(2);
        ExperienceStorageAdapter fluid = fluidAdapter(water);
        var player = makeServerPlayer(helper);
        player.getAbilities().instabuild = false;
        player.giveExperiencePoints(165);
        int initialScore = player.getScore();

        assertTrue(helper, MyotusAPI.experience().consume(energy, storage, IActionSource.empty(), player,
                15, List.of(FLUID_XP, PLAYER), List.of(fluid)),
                "One network XP and fourteen player XP should complete the payment");

        long expectedPlayerExperience = 151;
        int expectedLevel = MyotusAPI.experience().levelForTotal(expectedPlayerExperience);
        long expectedPoints = MyotusAPI.experience().intoLevel(expectedPlayerExperience);
        double expectedProgress = (double) expectedPoints / player.getXpNeededForNextLevel();
        assertEquals(helper, expectedPlayerExperience, MyotusAPI.experience().playerRaw(player),
                "Player raw XP should be debited exactly");
        assertEquals(helper, expectedLevel, player.experienceLevel, "Player level after debit");
        assertTrue(helper, Math.abs(expectedProgress - player.experienceProgress) < 0.000_001,
                "Player progress after debit: expected=" + expectedProgress
                        + ", actual=" + player.experienceProgress);
        assertEquals(helper, expectedPlayerExperience, player.totalExperience, "Player totalExperience after debit");
        assertEquals(helper, initialScore - 14, player.getScore(), "Player score should track the player XP debit");
        assertEquals(helper, 0L, storage.stored(water), "The network XP units should be extracted");
        assertEquals(helper, 0.0, energy.stored(), "The network extraction should consume exactly two AE");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void ae2StorageKeysCannotBelongToMultipleExperienceSources(GameTestHelper helper) {
        AEFluidKey water = AEFluidKey.of(Fluids.WATER);
        MutableStorage storage = new MutableStorage(water, 250);
        ExperienceStorageAdapter fluidAdapter = fluidAdapter(water);
        ExperienceStorageAdapter overlappingAppliedAdapter = new ExperienceStorageAdapter(
                APPLIED_EXPERIENCED_AMOUNT, water::equals, 250);

        boolean rejected = false;
        try {
            MyotusAPI.experience().planPayment(new TrackingEnergySource(10), storage, IActionSource.empty(),
                    0, 1, List.of(FLUID_XP, APPLIED_EXPERIENCED_AMOUNT),
                    List.of(fluidAdapter, overlappingAppliedAdapter));
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }

        assertTrue(helper, rejected, "An AE key matched by two source adapters must be rejected");
        var player = makeServerPlayer(helper);
        player.getAbilities().instabuild = false;
        player.giveExperiencePoints(165);
        rejected = false;
        try {
            MyotusAPI.experience().canConsume(new TrackingEnergySource(100), storage, IActionSource.empty(),
                    player, 5, List.of(PLAYER), List.of(fluidAdapter, overlappingAppliedAdapter));
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        assertTrue(helper, rejected, "A player-funded payment must still reject overlapping storage adapters");
        assertEquals(helper, 2, storage.reportCalls, "Each invalid operation must inspect only one full report");
        assertEquals(helper, 250L, storage.stored(water), "Adapter validation must not mutate storage");
        helper.succeed();
    }

    @SuppressWarnings("removal")
    @GameTest(template = "empty", timeoutTicks = 40)
    public static void ae2StorageMenuAppliesTerminalUpgradeMixinContract(GameTestHelper helper) {
        var player = makeServerPlayer(helper);
        var host = new TestTerminalHost(player);
        var menu = new MEStorageMenu(MEStorageMenu.TYPE, 1, player.getInventory(), host);
        var upgradeSlots = menu.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT);
        var card = MyoItems.MYOTUS_UPGRADE_CARD.get();
        var cardStack = new ItemStack(card);

        assertEquals(helper, MyotusConfig.SERVER.terminalUpgradeSlots.get().intValue(), upgradeSlots.size(),
                "MEStorageMenu mixin should add the configured Myotus upgrade slots");
        assertTrue(helper, !upgradeSlots.getFirst().mayPlace(new ItemStack(Items.STICK)),
                "A normal item must be rejected by the terminal upgrade slot");
        assertTrue(helper, upgradeSlots.getFirst().mayPlace(cardStack),
                "An ITerminalUpgradeCard must be accepted by the terminal upgrade slot");

        upgradeSlots.getFirst().set(cardStack);
        assertTrue(helper, !upgradeSlots.get(1).mayPlace(new ItemStack(card)),
                "A terminal must reject a duplicate card item");

        broadcastForEmbeddedPlayer(helper, menu);
        assertEquals(helper, 2, player.getInventory().countItem(Items.DIAMOND),
                "Insertion and first broadcast should dispatch one open and one tick callback");
        broadcastForEmbeddedPlayer(helper, menu);
        assertEquals(helper, 2, player.getInventory().countItem(Items.DIAMOND),
                "Repeated broadcasts in one game tick must be coalesced");

        List<ItemStack> installed = MyotusAPI.terminalUpgrades().installedUpgrades(menu);
        assertEquals(helper, 1, installed.size(), "Installed-upgrade API should omit empty slots");
        installed.getFirst().setCount(0);
        assertEquals(helper, 1, upgradeSlots.getFirst().getItem().getCount(),
                "Installed-upgrade API must return defensive stack copies");
        assertEquals(helper, 2, upgradeCallbackCount(upgradeSlots.getFirst().getItem()),
                "Open and tick callback data should be stored on the live card");

        var persistedMenu = new MEStorageMenu(MEStorageMenu.TYPE, 2, player.getInventory(), host);
        var persistedCard = persistedMenu.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT).getFirst().getItem();
        assertEquals(helper, 2, upgradeCallbackCount(persistedCard),
                "A fresh menu should reload callback data from persistent player storage");
        assertEquals(helper, 2, player.getInventory().countItem(Items.DIAMOND),
                "Constructing a menu must not dispatch upgrade callbacks before its first broadcast");

        helper.runAfterDelay(1, () -> {
            broadcastForEmbeddedPlayer(helper, menu);
            assertEquals(helper, 3, player.getInventory().countItem(Items.DIAMOND),
                    "A later game tick should dispatch the next tick callback");
            menu.removed(player);
            assertEquals(helper, 4, player.getInventory().countItem(Items.DIAMOND),
                    "Closing the menu should dispatch one close callback");
            var reopenedMenu = new MEStorageMenu(MEStorageMenu.TYPE, 3, player.getInventory(), host);
            var reopenedCard = reopenedMenu.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT).getFirst().getItem();
            assertEquals(helper, 4, upgradeCallbackCount(reopenedCard),
                    "Tick and close callback data should survive reopening the menu");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void scrolledTerminalUpgradePersistsAndDispatchesCallbacks(GameTestHelper helper) {
        int originalSlotCount = MyotusConfig.SERVER.terminalUpgradeSlots.get();
        MyotusConfig.SERVER.terminalUpgradeSlots.set(27);
        // Simulate applying a world-restart config without restarting the GameTest server.
        MyotusConfig.SERVER.terminalUpgradeSlots.clearCache();
        try {
            var player = makeServerPlayer(helper);
            var host = new TestTerminalHost(player);
            var card = new ItemStack(MyoItems.MYOTUS_UPGRADE_CARD.get());
            int lastSlot = 26;
            String storageKey = TerminalUpgradeStorageKey.of(host);
            new PlayerUpgradeContainer(player, storageKey).setItemDirect(lastSlot, card);

            var menu = new TestStorageMenu(4, player.getInventory(), host);
            var slots = menu.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT);
            assertEquals(helper, 27, slots.size(), "Every configured upgrade slot must be registered");
            assertTrue(helper, menu.initialized, "Subclass construction must finish before upgrade callbacks");
            assertEquals(helper, 0, player.getInventory().countItem(Items.DIAMOND),
                    "Constructing a subclass menu must not dispatch callbacks");
            assertTrue(helper, ItemStack.matches(card, slots.get(lastSlot).getItem()),
                    "The last upgrade slot must load from player storage");
            assertTrue(helper, !slots.get(lastSlot).mayPlace(new ItemStack(Items.STICK)),
                    "A normal item must be rejected in the last slot");
            assertTrue(helper, !slots.getFirst().mayPlace(card.copy()),
                    "A card in the last slot must block a duplicate in the first slot");
            assertEquals(helper, 1, MyotusAPI.terminalUpgrades().installedUpgrades(menu).size(),
                    "The installed-upgrade API must include a card beyond the first five slots");

            broadcastForEmbeddedPlayer(helper, menu);
            assertEquals(helper, 2, player.getInventory().countItem(Items.DIAMOND),
                    "The last-slot card must receive open and tick callbacks");
            assertEquals(helper, 2, upgradeCallbackCount(slots.get(lastSlot).getItem()),
                    "Last-slot callback data must be saved on the card");

            var reopened = new TestStorageMenu(5, player.getInventory(), host);
            var reopenedCard = reopened.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT).get(lastSlot).getItem();
            assertEquals(helper, 2, upgradeCallbackCount(reopenedCard),
                    "A subclass menu must reload the last slot's callback data");
            assertEquals(helper, 2, player.getInventory().countItem(Items.DIAMOND),
                    "Reopening must not call the card before the first broadcast");
            broadcastForEmbeddedPlayer(helper, reopened);
            assertEquals(helper, 4, player.getInventory().countItem(Items.DIAMOND),
                    "A reopened last-slot card must still receive open and tick callbacks");

            MyotusConfig.SERVER.terminalUpgradeSlots.set(5);
            MyotusConfig.SERVER.terminalUpgradeSlots.clearCache();
            var reduced = new TestStorageMenu(6, player.getInventory(), host);
            assertEquals(helper, 5, reduced.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT).size(),
                    "Lowering the limit must reduce the menu to five slots");
            assertTrue(helper, MyotusAPI.terminalUpgrades().installedUpgrades(reduced).isEmpty(),
                    "Cards outside the configured limit must not remain active upgrades");
            reduced.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT).getFirst()
                    .set(new ItemStack(MyoItems.MYOTUS_UPGRADE_CARD.get()));
            var preserved = new PlayerUpgradeContainer(player, storageKey).getStackInSlot(lastSlot);
            assertEquals(helper, 4, upgradeCallbackCount(preserved),
                    "Saving a reduced menu must preserve the hidden card and its data");

            MyotusConfig.SERVER.terminalUpgradeSlots.set(27);
            MyotusConfig.SERVER.terminalUpgradeSlots.clearCache();
            var expanded = new TestStorageMenu(7, player.getInventory(), host);
            assertTrue(helper, ItemStack.matches(preserved,
                            expanded.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT).get(lastSlot).getItem()),
                    "Raising the limit must restore access to the saved card");
            helper.succeed();
        } finally {
            MyotusConfig.SERVER.terminalUpgradeSlots.set(originalSlotCount);
            MyotusConfig.SERVER.terminalUpgradeSlots.clearCache();
        }
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void terminalPartKeepsAndDropsScrolledUpgrade(GameTestHelper helper) {
        BlockPos pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, AEBlocks.CABLE_BUS.block());
        CableBusBlockEntity cableBus = helper.getBlockEntity(pos);
        ItemTerminalPart part = cableBus.addPart(AEParts.TERMINAL.asItem(), Direction.NORTH, null);
        assertTrue(helper, part != null, "An AE2 terminal part must be attached to the cable bus");
        var card = new ItemStack(MyoItems.MYOTUS_UPGRADE_CARD.get());
        int lastSlot = 26;
        var upgrades = ((TerminalUpgradeHost) part).myotus$getUpgradeInventory();
        assertEquals(helper, 27, upgrades.size(),
                "Placed terminal parts must have all upgrade slots");
        upgrades.setItemDirect(lastSlot, card);

        CompoundTag saved = new CompoundTag();
        part.writeToNBT(saved, helper.getLevel().registryAccess());
        part.clearContent();
        assertTrue(helper, upgrades.getStackInSlot(lastSlot).isEmpty(),
                "Clearing the placed part must remove its upgrade before NBT reload");
        part.readFromNBT(saved, helper.getLevel().registryAccess());
        var restoredUpgrades = ((TerminalUpgradeHost) part).myotus$getUpgradeInventory();
        assertTrue(helper, ItemStack.matches(card, restoredUpgrades.getStackInSlot(lastSlot)),
                "The placed part must reload its last upgrade slot from NBT");

        List<ItemStack> drops = new ArrayList<>();
        part.addAdditionalDrops(drops, false);
        assertTrue(helper, drops.stream().anyMatch(stack -> ItemStack.matches(card, stack)),
                "Breaking the part must drop its last-slot upgrade");
        part.clearContent();
        assertTrue(helper, restoredUpgrades.getStackInSlot(lastSlot).isEmpty(),
                "Clearing the part after drops must remove its upgrade");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void terminalUpgradesSurvivePlayerCloning(GameTestHelper helper) {
        String currentKey = "host:example.CurrentTerminal";
        var legacyKeys = List.of(
                "item:ae2wtlib:wireless_universal_terminal:12345678-abcd-1234-abcd-123456789012",
                "item:example.WirelessHost:example:wireless_terminal:87654321-abcd-1234-abcd-123456789012");
        for (boolean keepEverything : new boolean[] {false, true}) {
            var original = makeServerPlayer(helper);
            var target = makeServerPlayer(helper);
            var card = new ItemStack(MyoItems.MYOTUS_UPGRADE_CARD.get());
            card.set(DataComponents.CUSTOM_NAME, Component.literal("Persistent terminal card"));
            new PlayerUpgradeContainer(original, currentKey).setItemDirect(4, card.copy());
            var saved = original.getPersistentData().get(currentKey);
            assertTrue(helper, saved != null, "Installing a card must save the current inventory");
            for (String key : legacyKeys) {
                // These saved terminals are never opened on the old player.
                original.getPersistentData().put(key, saved.copy());
            }
            original.getPersistentData().putString("othermod:unrelated", "not a terminal");

            target.restoreFrom(original, keepEverything);

            for (String key : List.of(currentKey, legacyKeys.get(0), legacyKeys.get(1))) {
                assertTrue(helper, original.getPersistentData().get(key) != target.getPersistentData().get(key),
                        "Cloned terminal NBT must not share the old player's mutable data");
                var reopened = new PlayerUpgradeContainer(target, key);
                assertTrue(helper, ItemStack.matches(card, reopened.getStackInSlot(4)),
                        "Current and unopened legacy cards must survive death and End-return cloning");
                reopened.setItemDirect(4, ItemStack.EMPTY);
                assertTrue(helper, ItemStack.matches(card,
                                new PlayerUpgradeContainer(original, key).getStackInSlot(4)),
                        "Removing a cloned card must not mutate the old player's saved inventory");
            }
            assertTrue(helper, !target.getPersistentData().contains("othermod:unrelated"),
                    "Cloning terminal upgrades must not copy other root data");
        }
        helper.succeed();
    }

    private static int upgradeCallbackCount(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag()
                .getInt("myotus_test_callbacks");
    }

    /** Native server player without a login: no unrelated mod handshake is required by menu tests. */
    static ServerPlayer makeServerPlayer(GameTestHelper helper) {
        var level = helper.getLevel();
        var cookie = CommonListenerCookie.createInitial(
                new GameProfile(UUID.randomUUID(), "myotus-test-player"), false);
        var player = new ServerPlayer(level.getServer(), level, cookie.gameProfile(), cookie.clientInformation());
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(level.getServer(), connection, player, cookie);
        return player;
    }

    private static void broadcastForEmbeddedPlayer(GameTestHelper helper, MEStorageMenu menu) {
        try {
            menu.broadcastChanges();
        } catch (RuntimeException error) {
            String message = error.getMessage();
            assertTrue(helper, message != null && message.contains("may not be sent to the client"),
                    "Unexpected MEStorageMenu broadcast failure: " + error);
        }
    }

    private static ExperienceStorageAdapter fluidAdapter(AEFluidKey key) {
        return MyotusAPI.experience().fluidStorage(key::equals, 250);
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

    private static void assertEquals(GameTestHelper helper, double expected, double actual, String message) {
        helper.assertTrue(Double.compare(expected, actual) == 0,
                message + ": expected=" + expected + ", actual=" + actual);
    }

    private static void assertEquals(GameTestHelper helper, Object expected, Object actual, String message) {
        helper.assertTrue(expected.equals(actual), message + ": expected=" + expected + ", actual=" + actual);
    }

    private static final class TrackingEnergySource implements IEnergySource {
        private double stored;
        private int modulatedExtractions;

        private TrackingEnergySource(double stored) {
            this.stored = stored;
        }

        @Override
        public double extractAEPower(double amount, Actionable mode, PowerMultiplier multiplier) {
            double extracted = Math.min(amount, stored);
            if (mode == Actionable.MODULATE) {
                stored -= extracted;
                modulatedExtractions++;
            }
            return extracted;
        }

        private double stored() {
            return stored;
        }

        private int modulatedExtractions() {
            return modulatedExtractions;
        }
    }

    private static final class MutableStorage implements MEStorage {
        private int reportCalls;
        private final Map<AEKey, Long> storedByKey = new LinkedHashMap<>();
        private int modulatedExtractions;

        private MutableStorage(AEKey key, long stored) {
            storedByKey.put(key, stored);
        }

        private MutableStorage(AEKey firstKey, long firstStored, AEKey secondKey, long secondStored) {
            storedByKey.put(firstKey, firstStored);
            storedByKey.put(secondKey, secondStored);
        }

        private long stored(AEKey key) {
            return storedByKey.getOrDefault(key, 0L);
        }

        private int modulatedExtractions() {
            return modulatedExtractions;
        }

        @Override
        public long extract(AEKey requestedKey, long amount, Actionable mode, IActionSource source) {
            long stored = stored(requestedKey);
            long extracted = Math.min(amount, stored);
            if (mode == Actionable.MODULATE) {
                storedByKey.put(requestedKey, stored - extracted);
                modulatedExtractions++;
            }
            return extracted;
        }

        @Override
        public KeyCounter getAvailableStacks() {
            reportCalls++;
            KeyCounter counter = new KeyCounter();
            for (var entry : storedByKey.entrySet()) {
                if (entry.getValue() > 0) {
                    counter.add(entry.getKey(), entry.getValue());
                }
            }
            return counter;
        }

        @Override
        public Component getDescription() {
            return Component.literal("Myotus GameTest storage");
        }
    }

    private static final class TestTerminalHost extends ItemMenuHost<Item> implements ITerminalHost {
        private final MEStorage storage = new MutableStorage(AEFluidKey.of(Fluids.WATER), 0);
        private final IConfigManager configManager = IConfigManager.builder(() -> {}).build();

        private TestTerminalHost(Player player) {
            this(player, new ItemStack(Items.STICK));
        }

        private TestTerminalHost(Player player, ItemStack stack) {
            super(stack.getItem(), player, MenuLocators.forStack(stack));
        }

        @Override
        public MEStorage getInventory() {
            return storage;
        }

        @Override
        public ILinkStatus getLinkStatus() {
            return ILinkStatus.ofDisconnected();
        }

        @Override
        public IConfigManager getConfigManager() {
            return configManager;
        }

        @Override
        public void returnToMainMenu(Player player, ISubMenu subMenu) {
        }

        @Override
        public ItemStack getMainMenuIcon() {
            return ItemStack.EMPTY;
        }

    }

    private static final class TestStorageMenu extends MEStorageMenu {
        private final boolean initialized;

        private TestStorageMenu(int id, Inventory inventory, ITerminalHost host) {
            super(MEStorageMenu.TYPE, id, inventory, host);
            initialized = true;
        }
    }

}
