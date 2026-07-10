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

import java.util.List;

import static me.myogoo.myotus.api.experience.ExperienceMath.ExperienceSource.APPLIED_EXPERIENCED_AMOUNT;
import static me.myogoo.myotus.api.experience.ExperienceMath.ExperienceSource.FLUID_XP;
import static me.myogoo.myotus.api.experience.ExperienceMath.ExperienceSource.PLAYER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExperienceFacadeExtractionTest {
    @Test
    void storedReportsVisibleStoredAmountWithoutClaimingExtractability() {
        AEKey key = appliedExperiencedKey();
        MutableStorage storage = new MutableStorage(key, 100);

        assertEquals(100, MyotusAPI.experience().stored(storage,
                ExperienceMath.ExperienceSource.APPLIED_EXPERIENCED_AMOUNT));
    }

    @Test
    void extractableHonorsEnergySimulation() {
        AEKey key = appliedExperiencedKey();
        MutableStorage storage = new MutableStorage(key, 100);
        IEnergySource limitedEnergy = (amount, mode, multiplier) -> Math.min(amount, 25);

        assertEquals(25, MyotusAPI.experience().extractable(limitedEnergy, storage,
                IActionSource.empty(), ExperienceMath.ExperienceSource.APPLIED_EXPERIENCED_AMOUNT));
        assertEquals(100, storage.stored(key));
    }

    @Test
    void extractExactReturnsTrueOnlyAfterFullModulatedExtraction() {
        AEKey key = appliedExperiencedKey();
        MutableStorage storage = new MutableStorage(key, 100);

        assertTrue(MyotusAPI.experience().extractExact(unlimitedEnergy(), storage,
                IActionSource.empty(), 40, ExperienceMath.ExperienceSource.APPLIED_EXPERIENCED_AMOUNT));
        assertEquals(60, storage.stored(key));
    }

    @Test
    void extractExactLeavesStorageUntouchedWhenSimulationCannotPay() {
        AEKey key = appliedExperiencedKey();
        MutableStorage storage = new MutableStorage(key, 30);

        assertFalse(MyotusAPI.experience().extractExact(unlimitedEnergy(), storage,
                IActionSource.empty(), 40, ExperienceMath.ExperienceSource.APPLIED_EXPERIENCED_AMOUNT));
        assertEquals(30, storage.stored(key));
    }

    @Test
    void availableAnvilSourcePriorityUsesOnlyActuallyExtractableNetworkSources() {
        AEKey key = appliedExperiencedKey();
        MutableStorage storage = new MutableStorage(key, 50);

        assertEquals(List.of(APPLIED_EXPERIENCED_AMOUNT, PLAYER),
                MyotusAPI.experience().availableAnvilSourcePriority(unlimitedEnergy(), storage, IActionSource.empty(),
                        FLUID_XP));
        assertEquals(List.of(PLAYER, APPLIED_EXPERIENCED_AMOUNT),
                MyotusAPI.experience().availableAnvilSourcePriority(unlimitedEnergy(), storage, IActionSource.empty(),
                        PLAYER));
    }

    @Test
    void planPaymentUsesExtractableStorageAndPlayerRemainder() {
        AEKey key = appliedExperiencedKey();
        MutableStorage storage = new MutableStorage(key, 50);

        var payment = MyotusAPI.experience().planPayment(unlimitedEnergy(), storage, IActionSource.empty(),
                25, 75, List.of(APPLIED_EXPERIENCED_AMOUNT, PLAYER));

        assertTrue(payment.enough());
        assertEquals(50, payment.appliedExperiencedAmount());
        assertEquals(25, payment.player());
        assertEquals(50, storage.stored(key));
    }


    @Test
    void planPaymentUsesCumulativeNetworkEnergyBeforePlayerFallback() {
        AEKey fluidKey = mock(AEKey.class);
        when(fluidKey.getId()).thenReturn(ResourceLocation.fromNamespaceAndPath("fluid", "xp"));
        when(fluidKey.getType()).thenReturn(AEKeyType.items());
        AEKey appliedKey = appliedExperiencedKey();
        MutableStorage storage = new MutableStorage(fluidKey, 50, appliedKey, 50);
        LimitedEnergySource limitedEnergy = new LimitedEnergySource(50);

        var payment = MyotusAPI.experience().planPayment(limitedEnergy, storage, IActionSource.empty(),
                25, 75, List.of(FLUID_XP, APPLIED_EXPERIENCED_AMOUNT, PLAYER));

        assertTrue(payment.enough());
        assertEquals(50, payment.fluidXp());
        assertEquals(0, payment.appliedExperiencedAmount());
        assertEquals(25, payment.player());
        assertEquals(50, storage.stored(fluidKey));
        assertEquals(50, storage.stored(appliedKey));
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

    private static final class LimitedEnergySource implements IEnergySource {
        private double stored;

        private LimitedEnergySource(double stored) {
            this.stored = stored;
        }

        @Override
        public double extractAEPower(double amount, Actionable mode, PowerMultiplier usePowerMultiplier) {
            double extracted = Math.min(amount, this.stored);
            if (mode == Actionable.MODULATE) {
                this.stored -= extracted;
            }
            return extracted;
        }
    }

    private static final class MutableStorage implements MEStorage {
        private final java.util.LinkedHashMap<AEKey, Long> storedByKey = new java.util.LinkedHashMap<>();

        private MutableStorage(AEKey key, long stored) {
            this.storedByKey.put(key, stored);
        }

        private MutableStorage(AEKey firstKey, long firstStored, AEKey secondKey, long secondStored) {
            this.storedByKey.put(firstKey, firstStored);
            this.storedByKey.put(secondKey, secondStored);
        }

        private long stored(AEKey key) {
            return this.storedByKey.getOrDefault(key, 0L);
        }

        @Override
        public long extract(AEKey requestedKey, long amount, Actionable mode, IActionSource source) {
            long stored = stored(requestedKey);
            long extracted = Math.min(amount, stored);
            if (mode == Actionable.MODULATE) {
                this.storedByKey.put(requestedKey, stored - extracted);
            }
            return extracted;
        }

        @Override
        public KeyCounter getAvailableStacks() {
            KeyCounter counter = new KeyCounter();
            for (var entry : this.storedByKey.entrySet()) {
                if (entry.getValue() > 0) {
                    counter.add(entry.getKey(), entry.getValue());
                }
            }
            return counter;
        }

        @Override
        public Component getDescription() {
            return Component.literal("test storage");
        }
    }
}
