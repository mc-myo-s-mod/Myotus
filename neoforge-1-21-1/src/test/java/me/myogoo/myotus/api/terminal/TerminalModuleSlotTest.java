package me.myogoo.myotus.api.terminal;

import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.storage.ITerminalHost;
import appeng.menu.me.common.MEStorageMenu;
import me.myogoo.myotus.menu.TerminalUpgradeHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TerminalModuleSlotTest {
    @Test
    void cardRemovalImmediatelyRejectsUserMutationsButAllowsContentsSync() {
        MEStorageMenu menu = mock(MEStorageMenu.class);
        Player player = mock(Player.class);
        Item card = mock(Item.class);
        Slot nativeSlot = mock(Slot.class);
        ItemStack stack = mock(ItemStack.class);
        when(menu.getPlayer()).thenReturn(player);
        when(nativeSlot.isActive()).thenReturn(true);
        when(nativeSlot.getItem()).thenReturn(stack);
        when(nativeSlot.mayPlace(stack)).thenReturn(true);
        when(nativeSlot.mayPickup(player)).thenReturn(true);
        when(nativeSlot.safeInsert(stack)).thenReturn(ItemStack.EMPTY);
        when(nativeSlot.safeTake(1, 64, player)).thenReturn(stack);
        AtomicBoolean installed = new AtomicBoolean();
        try (var helper = mockStatic(TerminalUpgradeHelper.class)) {
            helper.when(() -> TerminalUpgradeHelper.hasUpgrade(menu, card)).thenAnswer(call -> installed.get());
            var slot = new TerminalModuleSlot(menu, nativeSlot, card);
            assertFalse(slot.isActive());
            assertSame(stack, slot.safeInsert(stack));
            assertSame(ItemStack.EMPTY, slot.safeTake(1, 64, player));

            installed.set(true);
            assertTrue(slot.isActive());
            assertTrue(slot.mayPlace(stack));
            assertSame(ItemStack.EMPTY, slot.safeInsert(stack));
            assertSame(stack, slot.safeTake(1, 64, player));

            installed.set(false);
            assertFalse(slot.mayPickup(player));
            assertFalse(slot.mayPlace(stack));
            assertEquals(Optional.empty(), slot.tryRemove(1, 64, player));
            assertSame(ItemStack.EMPTY, slot.remove(1));
            slot.setByPlayer(stack);
            verify(nativeSlot, never()).setByPlayer(stack);
            slot.set(stack);
            verify(nativeSlot).set(stack);
            verify(nativeSlot, times(1)).safeInsert(stack);
            verify(nativeSlot, times(1)).safeTake(1, 64, player);
        }
    }

    @Test
    void preservesNativeRestrictionsAndRealExtractionRefusal() {
        MEStorageMenu menu = mock(MEStorageMenu.class);
        Player player = mock(Player.class);
        Item card = mock(Item.class);
        Slot nativeSlot = mock(Slot.class);
        ItemStack stack = mock(ItemStack.class);
        when(menu.getPlayer()).thenReturn(player);
        when(nativeSlot.isActive()).thenReturn(true);
        when(nativeSlot.getItem()).thenReturn(stack);
        try (var helper = mockStatic(TerminalUpgradeHelper.class)) {
            helper.when(() -> TerminalUpgradeHelper.hasUpgrade(menu, card)).thenReturn(true);
            var slot = new TerminalModuleSlot(menu, nativeSlot, card);
            assertTrue(slot.isInteractionAllowed(player));
            assertFalse(slot.mayPlace(stack));
            assertFalse(slot.mayPickup(player));
            assertFalse(slot.allowModification(player));
            assertSame(ItemStack.EMPTY, slot.safeTake(1, 64, player));
            verify(nativeSlot, never()).safeTake(anyInt(), anyInt(), any());

            when(nativeSlot.mayPickup(player)).thenReturn(true);
            when(nativeSlot.safeTake(1, 64, player)).thenReturn(ItemStack.EMPTY);
            assertSame(ItemStack.EMPTY, slot.safeTake(1, 64, player));
            verify(nativeSlot).safeTake(1, 64, player);
            when(nativeSlot.mayPlace(stack)).thenReturn(true);
            assertTrue(slot.mayPlace(stack));
            assertFalse(slot.allowModification(player));
            assertFalse(slot.isInteractionAllowed(mock(Player.class)));
        }
    }

    @Test
    void backingTerminalRemainsVisibleButCannotBeMoved() {
        MEStorageMenu menu = mock(MEStorageMenu.class);
        Player player = mock(Player.class);
        Item card = mock(Item.class);
        Slot nativeSlot = mock(Slot.class);
        ItemStack stack = mock(ItemStack.class);
        ItemMenuHost<?> host = mock(ItemMenuHost.class, withSettings().extraInterfaces(ITerminalHost.class));
        when(menu.getPlayer()).thenReturn(player);
        when(menu.getHost()).thenReturn((ITerminalHost) host);
        when(host.getItemStack()).thenReturn(stack);
        when(nativeSlot.isActive()).thenReturn(true);
        when(nativeSlot.getItem()).thenReturn(stack);
        try (var helper = mockStatic(TerminalUpgradeHelper.class)) {
            helper.when(() -> TerminalUpgradeHelper.hasUpgrade(menu, card)).thenReturn(true);
            var slot = new TerminalModuleSlot(menu, nativeSlot, card);
            assertTrue(slot.isActive());
            assertFalse(slot.isInteractionAllowed(player));
            assertFalse(slot.mayPickup(player));
            assertSame(ItemStack.EMPTY, slot.remove(1));
        }
    }
}
