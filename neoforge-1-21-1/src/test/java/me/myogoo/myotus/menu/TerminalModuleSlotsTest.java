package me.myogoo.myotus.menu;

import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import appeng.menu.me.common.MEStorageMenu;
import me.myogoo.myotus.api.terminal.ITerminalModuleProvider;
import me.myogoo.myotus.api.ITerminalModuleCard;
import me.myogoo.myotus.api.terminal.TerminalModuleSlot;
import me.myogoo.myotus.mixin.ae2.AEBaseMenuAccessor;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.util.ArrayList;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TerminalModuleSlotsTest {
    private static final SlotSemantic MODULE = SlotSemantics.register("myotus_test_provider_slots", false);
    private Item card;
    private Field frozenRegistrations;
    private Object originalRegistrations;

    @BeforeEach
    void installTestProvider() throws ReflectiveOperationException {
        card = mock(Item.class, withSettings().extraInterfaces(ITerminalModuleCard.class));
        ITerminalModuleProvider provider = menu -> List.of(
                menu.getPlayer().inventoryMenu.getSlot(5), menu.getPlayer().inventoryMenu.getSlot(6),
                menu.getPlayer().inventoryMenu.getSlot(7), menu.getPlayer().inventoryMenu.getSlot(8),
                menu.getPlayer().inventoryMenu.getSlot(45));
        // Test-only registration without changing Minecraft's frozen item registry or production providers.
        var registration = Class.forName(TerminalModuleSlots.class.getName() + "$Registration")
                .getDeclaredConstructor(Item.class, SlotSemantic.class, ITerminalModuleProvider.class);
        registration.setAccessible(true);
        frozenRegistrations = TerminalModuleSlots.class.getDeclaredField("frozenRegistrations");
        frozenRegistrations.setAccessible(true);
        originalRegistrations = frozenRegistrations.get(null);
        frozenRegistrations.set(null, List.of(registration.newInstance(card, MODULE, provider)));
    }

    @AfterEach
    void restoreProviders() throws IllegalAccessException {
        frozenRegistrations.set(null, originalRegistrations);
    }

    @Test
    void rejectsInvalidRegistrationsBeforeTouchingTheSharedRegistry() {
        SlotSemantic semantic = MODULE;
        ITerminalModuleProvider factory = menu -> List.of();
        assertThrows(NullPointerException.class, () -> TerminalModuleSlots.register(null, semantic, factory));
        assertThrows(NullPointerException.class,
                () -> TerminalModuleSlots.register(mock(Item.class), null, factory));
        assertThrows(NullPointerException.class,
                () -> TerminalModuleSlots.register(mock(Item.class), semantic, null));
        assertThrows(IllegalArgumentException.class,
                () -> TerminalModuleSlots.register(mock(Item.class), semantic, factory));
        assertThrows(IllegalArgumentException.class,
                () -> TerminalModuleSlots.register(mock(Item.class,
                        withSettings().extraInterfaces(ITerminalModuleCard.class)), semantic, factory));
    }

    @Test
    void externalFactoriesUsePermanentGuardedSlotsAndInitializationIsIdempotent() throws Exception {
        Fixture fixture = fixture();
        TerminalModuleSlots.initialize(fixture.menu());
        var equipment = TerminalModuleSlots.slots(fixture.menu(), card);
        assertEquals(5, equipment.size());
        for (int i = 0; i < equipment.size(); i++) {
            TerminalModuleSlot managed = assertInstanceOf(TerminalModuleSlot.class, equipment.get(i));
            assertSame(fixture.nativeSlots().get(i), managed.getDelegate());
            assertSame(fixture.menu(), managed.getMenu());
            assertTrue(TerminalModuleSlots.isModuleSlot(fixture.menu(), managed));
            assertSame(managed.getDelegate(), TerminalModuleSlots.nativeSlot(managed));
        }
        List<Slot> initial = List.copyOf(fixture.menu().slots);
        TerminalModuleSlots.initialize(fixture.menu());
        assertEquals(initial, fixture.menu().slots);
        assertEquals(5, fixture.addedSemantics().stream()
                .filter(MODULE::equals).count());
        Fixture second = fixture();
        TerminalModuleSlots.initialize(second.menu());
        assertEquals(fixture.addedSemantics(), second.addedSemantics());
        assertEquals(equipment.stream().map(slot -> slot.index).toList(),
                TerminalModuleSlots.slots(second.menu(), card)
                        .stream().map(slot -> slot.index).toList());
    }

    @Test
    void rejectsAlreadyPopulatedModuleSemanticsBeforeAppendingSlots() throws Exception {
        Fixture fixture = fixture();
        when(fixture.menu().getSlots(MODULE))
                .thenReturn(List.of(mock(Slot.class)));
        assertThrows(IllegalArgumentException.class, () -> TerminalModuleSlots.initialize(fixture.menu()));
        assertTrue(fixture.menu().slots.isEmpty());
    }

    private static Fixture fixture() throws Exception {
        MEStorageMenu menu = mock(MEStorageMenu.class, withSettings()
                .extraInterfaces(TerminalModuleMenu.class, AEBaseMenuAccessor.class));
        var slotsField = AbstractContainerMenu.class.getField("slots");
        slotsField.setAccessible(true);
        slotsField.set(menu, NonNullList.create());
        when(((TerminalModuleMenu) menu).myotus$getModuleSlots()).thenReturn(new TerminalModuleSlots.State());
        when(menu.getSlots(any(SlotSemantic.class))).thenReturn(List.of());
        Player player = mock(Player.class);
        InventoryMenu inventoryMenu = mock(InventoryMenu.class);
        var inventoryMenuField = Player.class.getField("inventoryMenu");
        inventoryMenuField.setAccessible(true);
        inventoryMenuField.set(player, inventoryMenu);
        when(menu.getPlayer()).thenReturn(player);
        List<Slot> nativeSlots = new ArrayList<>();
        for (int index : new int[] {5, 6, 7, 8, 45}) {
            Slot slot = mock(Slot.class);
            nativeSlots.add(slot);
            when(inventoryMenu.getSlot(index)).thenReturn(slot);
        }
        List<SlotSemantic> added = new ArrayList<>();
        when(((AEBaseMenuAccessor) menu).myotus$addModuleSlot(any(Slot.class), any(SlotSemantic.class)))
                .thenAnswer(call -> {
                    Slot slot = call.getArgument(0);
                    slot.index = menu.slots.size();
                    menu.slots.add(slot);
                    added.add(call.getArgument(1));
                    return slot;
                });
        return new Fixture(menu, nativeSlots, added);
    }

    private record Fixture(MEStorageMenu menu, List<Slot> nativeSlots, List<SlotSemantic> addedSemantics) {}
}
