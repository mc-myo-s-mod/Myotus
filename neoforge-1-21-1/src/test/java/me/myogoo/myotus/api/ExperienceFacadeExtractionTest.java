package me.myogoo.myotus.api;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import me.myogoo.myotus.api.experience.ExperienceMath;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExperienceFacadeExtractionTest {
    @Test
    void storedStorageExperienceReportsVisibleStoredAmountWithoutClaimingExtractability() {
        AEKey key = appliedExperiencedKey();
        MutableStorage storage = new MutableStorage(key, 100);

        assertEquals(100, MyotusAPI.experience().storedStorageExperience(storage,
                ExperienceMath.ExperienceSource.APPLIED_EXPERIENCED_AMOUNT));
    }

    @Test
    void extractableStorageExperienceHonorsEnergySimulation() {
        AEKey key = appliedExperiencedKey();
        MutableStorage storage = new MutableStorage(key, 100);
        IEnergySource limitedEnergy = (amount, mode, multiplier) -> Math.min(amount, 25);

        assertEquals(25, MyotusAPI.experience().extractableStorageExperience(limitedEnergy, storage,
                IActionSource.empty(), ExperienceMath.ExperienceSource.APPLIED_EXPERIENCED_AMOUNT));
        assertEquals(100, storage.stored);
    }

    @Test
    void extractStorageExperienceExactReturnsTrueOnlyAfterFullModulatedExtraction() {
        AEKey key = appliedExperiencedKey();
        MutableStorage storage = new MutableStorage(key, 100);

        assertTrue(MyotusAPI.experience().extractStorageExperienceExact(unlimitedEnergy(), storage,
                IActionSource.empty(), 40, ExperienceMath.ExperienceSource.APPLIED_EXPERIENCED_AMOUNT));
        assertEquals(60, storage.stored);
    }

    @Test
    void extractStorageExperienceExactLeavesStorageUntouchedWhenSimulationCannotPay() {
        AEKey key = appliedExperiencedKey();
        MutableStorage storage = new MutableStorage(key, 30);

        assertFalse(MyotusAPI.experience().extractStorageExperienceExact(unlimitedEnergy(), storage,
                IActionSource.empty(), 40, ExperienceMath.ExperienceSource.APPLIED_EXPERIENCED_AMOUNT));
        assertEquals(30, storage.stored);
    }

    private static IEnergySource unlimitedEnergy() {
        return (double amount, Actionable mode, PowerMultiplier multiplier) -> amount;
    }

    private static AEKey appliedExperiencedKey() {
        AEKey key = mock(AEKey.class);
        when(key.getId()).thenReturn(ResourceLocation.fromNamespaceAndPath("appex", "experience"));
        when(key.getType()).thenReturn(AEKeyType.items());
        return key;
    }

    private static final class MutableStorage implements MEStorage {
        private final AEKey key;
        private long stored;

        private MutableStorage(AEKey key, long stored) {
            this.key = key;
            this.stored = stored;
        }

        @Override
        public long extract(AEKey requestedKey, long amount, Actionable mode, IActionSource source) {
            if (requestedKey != this.key) {
                return 0;
            }
            long extracted = Math.min(amount, this.stored);
            if (mode == Actionable.MODULATE) {
                this.stored -= extracted;
            }
            return extracted;
        }

        @Override
        public KeyCounter getAvailableStacks() {
            KeyCounter counter = new KeyCounter();
            if (this.stored > 0) {
                counter.add(this.key, this.stored);
            }
            return counter;
        }

        @Override
        public Component getDescription() {
            return Component.literal("test storage");
        }
    }
}
