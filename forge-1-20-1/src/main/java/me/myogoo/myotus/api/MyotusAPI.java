package me.myogoo.myotus.api;

import appeng.menu.me.common.MEStorageMenu;
import com.mojang.blaze3d.platform.InputConstants;
import me.myogoo.myotus.dto.MyoModDto;
import me.myogoo.myotus.api.experience.ExperienceMath;
import me.myogoo.myotus.api.network.IMyotusNetwork;
import me.myogoo.myotus.api.registrar.IConfigRegistrar;
import me.myogoo.myotus.api.registrar.ICreativeTabRegistrar;
import me.myogoo.myotus.client.gui.widgets.KeyBindingButton;
import me.myogoo.myotus.util.mod.ModIntegrationManager;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Static entry point for the Myotus public API.
 *
 * <p>Use this class when you need quick access to the shared API services without
 * storing the {@link IMyotusAPI} instance yourself.</p>
 *
 * <p>Optional integration markers are discovered from annotations such as
 * {@code @MyoMod}; this API exposes the runtime services used by integration
 * code after it is active.</p>
 */
public final class MyotusAPI {
    private static final IntegrationFacade INTEGRATIONS = IntegrationFacade.INSTANCE;
    private static final TerminalUpgradesFacade TERMINAL_UPGRADES = TerminalUpgradesFacade.INSTANCE;
    private static final ExperienceFacade EXPERIENCE = ExperienceFacade.INSTANCE;

    private static IMyotusAPI instance;

    private MyotusAPI() {
    }

    /**
     * Returns the shared Myotus API implementation.
     *
     * <p>This method throws if the API has not been initialized yet. External
     * integrations should normally call it during or after mod setup, not from
     * static initializers.</p>
     *
     * @return the shared API instance
     */
    public static IMyotusAPI get() {
        return Objects.requireNonNull(instance, "MyotusAPI has not been initialized yet!");
    }

    /**
     * Returns whether the API instance is ready to use.
     *
     * @return {@code true} if {@link #get()} can be called safely
     */
    public static boolean isInitialized() {
        return instance != null;
    }

    /**
     * Returns the registrar for terminal configuration tabs.
     *
     * @return the config tab registrar
     */
    public static IConfigRegistrar configTabs() {
        return get().configRegistrar();
    }

    /**
     * Returns the registrar for entries in the shared Myotus creative tab.
     *
     * @return the creative tab registrar
     */
    public static ICreativeTabRegistrar creativeTabs() {
        return get().creativeTabRegistrar();
    }

    /**
     * Returns the public facade for Myotus-managed optional integrations.
     *
     * @return the integration facade
     */
    public static IntegrationFacade integrations() {
        return INTEGRATIONS;
    }

    /**
     * Returns the public facade for terminal upgrade slot helpers.
     *
     * @return the terminal upgrade facade
     */
    public static TerminalUpgradesFacade terminalUpgrades() {
        return TERMINAL_UPGRADES;
    }

    /**
     * Returns helpers for Applied Experienced / {@code fluid:xp} calculations.
     *
     * @return the experience calculation facade
     */
    public static ExperienceFacade experience() {
        return EXPERIENCE;
    }




    /**
     * Returns the shared networking facade used to register and send Myotus packets.
     *
     * @return the networking facade
     */
    public static IMyotusNetwork network() {
        return get().network();
    }

    /**
     * Public facade for Myotus-managed optional integrations.
     */
    public static final class IntegrationFacade {
        private static final IntegrationFacade INSTANCE = new IntegrationFacade();

        private IntegrationFacade() {
        }

        public MyoModDto get(String id) {
            return ModIntegrationManager.get(id);
        }

        public Class<? extends Annotation> getClass(String id) {
            return ModIntegrationManager.getClass(id);
        }

        public Class<? extends Annotation> getClass(MyoModDto mod) {
            return ModIntegrationManager.getClass(mod);
        }

        public boolean isLoaded(String id) {
            return ModIntegrationManager.isLoaded(id);
        }

        public boolean isLoaded(Class<? extends Annotation> annotationClass) {
            return ModIntegrationManager.isLoaded(annotationClass);
        }

        public boolean isLoaded(Type annotationType) {
            return ModIntegrationManager.isLoaded(annotationType);
        }

        public boolean isLoaded(MyoModDto mod) {
            return ModIntegrationManager.isLoaded(mod);
        }

        public boolean isRegistered(String id) {
            return ModIntegrationManager.isRegistered(id);
        }

        public boolean isRegistered(Class<? extends Annotation> annotationClass) {
            return ModIntegrationManager.isRegistered(annotationClass);
        }

        public Map<MyoModDto, Class<? extends Annotation>> getActiveIntegrations() {
            return ModIntegrationManager.getActiveIntegrations();
        }
    }

    /**
     * Public facade for terminal upgrade slot helpers.
     *
     * <p>Prefer this entry point over importing Myotus internal menu classes.</p>
     */
    public static final class TerminalUpgradesFacade {
        private static final TerminalUpgradesFacade INSTANCE = new TerminalUpgradesFacade();

        private TerminalUpgradesFacade() {
        }

        public List<ItemStack> getInstalledUpgrades(MEStorageMenu menu) {
            return TerminalUpgradeFacade.getInstalledUpgrades(menu);
        }

        public Set<Item> getInstalledUpgradeItems(MEStorageMenu menu) {
            return TerminalUpgradeFacade.getInstalledUpgradeItems(menu);
        }

        public List<ItemStack> getAvailableUpgradeCards(MEStorageMenu menu) {
            return TerminalUpgradeFacade.getAvailableUpgradeCards(menu);
        }

        public List<Component> getAvailableUpgradeTooltip(MEStorageMenu menu) {
            return TerminalUpgradeFacade.getAvailableUpgradeTooltip(menu);
        }

        public boolean hasUpgrade(MEStorageMenu menu, Item upgradeItem) {
            return TerminalUpgradeFacade.hasUpgrade(menu, upgradeItem);
        }

        public boolean hasUpgrade(MEStorageMenu menu, ResourceLocation itemId) {
            return TerminalUpgradeFacade.hasUpgrade(menu, itemId);
        }

        public int countUpgrade(MEStorageMenu menu, Item upgradeItem) {
            return TerminalUpgradeFacade.countUpgrade(menu, upgradeItem);
        }

        public int countUpgrade(MEStorageMenu menu, ResourceLocation itemId) {
            return TerminalUpgradeFacade.countUpgrade(menu, itemId);
        }

        public boolean canInsertUpgrade(Iterable<ItemStack> installedStacks, int slot, ItemStack stack) {
            return TerminalUpgradeFacade.canInsertUpgrade(installedStacks, slot, stack);
        }
    }

    /**
     * Public facade for raw vanilla experience calculations and stable experience integration ids.
     */
    public static final class ExperienceFacade {
        private static final ExperienceFacade INSTANCE = new ExperienceFacade();

        private ExperienceFacade() {
        }

        public String appliedExperiencedModId() {
            return ExperienceMath.APPLIED_EXPERIENCED_MOD_ID;
        }

        public String appliedExperiencedAeKeyId() {
            return ExperienceMath.APPLIED_EXPERIENCED_AE_KEY_ID;
        }

        public String appliedExperiencedAmountComponentId() {
            return ExperienceMath.APPLIED_EXPERIENCED_AMOUNT_COMPONENT_ID;
        }

        public String fluidXpId() {
            return ExperienceMath.FLUID_XP_ID;
        }

        public List<ExperienceMath.ExperienceSource> anvilSourcePriority(long fluidXp) {
            return ExperienceMath.anvilSourcePriority(fluidXp);
        }

        public List<ExperienceMath.ExperienceSource> anvilSourcePriority(boolean includeFluidXp) {
            return ExperienceMath.anvilSourcePriority(includeFluidXp);
        }

        public List<ExperienceMath.ExperienceSource> defaultAnvilSourcePriority() {
            return ExperienceMath.DEFAULT_ANVIL_SOURCE_PRIORITY;
        }

        public long totalExperience(long appliedExperience, long fluidXp) {
            return ExperienceMath.totalExperience(appliedExperience, fluidXp);
        }

        public long totalExperience(long playerExperience, long fluidXp, long appliedExperienceAmount) {
            return ExperienceMath.totalExperience(playerExperience, fluidXp, appliedExperienceAmount);
        }

        public ExperienceMath.ExperienceConsumptionPlan consumeExperience(long requiredExperience, long playerExperience,
                long fluidXp, long appliedExperienceAmount) {
            return ExperienceMath.consumeExperience(requiredExperience, playerExperience, fluidXp, appliedExperienceAmount);
        }

        public ExperienceMath.ExperienceConsumptionPlan consumeExperience(long requiredExperience, long playerExperience,
                long fluidXp, long appliedExperienceAmount, List<ExperienceMath.ExperienceSource> sourcePriority) {
            return ExperienceMath.consumeExperience(requiredExperience, playerExperience, fluidXp, appliedExperienceAmount,
                    sourcePriority);
        }

        public long totalExperienceForLevel(int level) {
            return ExperienceMath.totalExperienceForLevel(level);
        }

        public int levelForTotalExperience(long totalExperience) {
            return ExperienceMath.levelForTotalExperience(totalExperience);
        }

        public long experienceIntoLevel(long totalExperience) {
            return ExperienceMath.experienceIntoLevel(totalExperience);
        }

        public long experienceToNextLevel(int level) {
            return ExperienceMath.experienceToNextLevel(level);
        }

        public long vanillaAnvilExperienceCost(int currentLevel, int levelCost) {
            return ExperienceMath.vanillaAnvilExperienceCost(currentLevel, levelCost);
        }

        public long apothicAnvilExperienceCost(int levelCost) {
            return ExperienceMath.apothicAnvilExperienceCost(levelCost);
        }

        public long apothicEnchantingTableExperienceCost(int level, int slot) {
            return ExperienceMath.apothicEnchantingTableExperienceCost(level, slot);
        }

        public long apothicLibraryPointsForLevel(int level) {
            return ExperienceMath.apothicLibraryPointsForLevel(level);
        }
    }

    /**
     * Client-only public facade.
     */
    public static final class Client {
        private Client() {
        }

        public static final class Widgets {
            private Widgets() {
            }

            public static AbstractButton keyBindingButton(Component label,
                    Consumer<List<InputConstants.Key>> changeListener) {
                return new KeyBindingButton(label, changeListener);
            }

            public static AbstractButton keyBindingButton(Component label, InputConstants.Key initialKey,
                    Consumer<List<InputConstants.Key>> changeListener) {
                return new KeyBindingButton(label, initialKey, changeListener);
            }
        }
    }


    /**
     * Internal bootstrap hook used by Myotus to install the API implementation.
     *
     * @param api the API implementation
     */
    @ApiStatus.Internal
    public static void _setInstance(IMyotusAPI api) {
        if (instance != null) {
            throw new IllegalStateException("MyotusAPI instance is already set!");
        }
        instance = Objects.requireNonNull(api, "api");
    }
}
