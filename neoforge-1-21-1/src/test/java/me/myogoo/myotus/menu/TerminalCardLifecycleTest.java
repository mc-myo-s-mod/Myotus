package me.myogoo.myotus.menu;

import appeng.menu.me.common.MEStorageMenu;
import me.myogoo.myotus.api.ITerminalCard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/** Exercises the actual transformed callback bodies without introducing registered test items. */
class TerminalCardLifecycleTest {
    @Test
    void restoredUnavailableCardNeverReceivesOpenTickRemovalOrMenuClose() throws Exception {
        Fixture fixture = fixture(false);
        invoke(MEStorageMenu.class, fixture.menu(), "myotus$dispatchUpgradeOpen");
        invoke(MEStorageMenu.class, fixture.menu(), "myotus$dispatchUpgradeTick");
        fixture.closeMenu();
        fixture.removeCard();
        verify(fixture.card(), never()).onTerminalOpen(any(), any());
        verify(fixture.card(), never()).onTerminalTick(any(), any());
        verify(fixture.card(), never()).onTerminalClose(any(), any());
    }

    @Test
    void availableCardReceivesOpenTickAndRemovalCallbacks() throws Exception {
        Fixture fixture = fixture(true);
        invoke(MEStorageMenu.class, fixture.menu(), "myotus$dispatchUpgradeOpen");
        invoke(MEStorageMenu.class, fixture.menu(), "myotus$dispatchUpgradeTick");
        fixture.removeCard();
        verify(fixture.card()).onTerminalOpen(fixture.menu(), fixture.stack());
        verify(fixture.card()).onTerminalTick(fixture.menu(), fixture.stack());
        verify(fixture.card()).onTerminalClose(fixture.menu(), fixture.stack());
    }

    @Test
    void availableCardReceivesInsertedOpenAndInstalledMenuCloseCallbacks() throws Exception {
        Fixture fixture = fixture(true);
        invoke(MEStorageMenu.class, fixture.menu(), "myotus$checkUpgradeSlotChanges");
        fixture.closeMenu();
        verify(fixture.card()).onTerminalOpen(fixture.menu(), fixture.stack());
        verify(fixture.card()).onTerminalClose(fixture.menu(), fixture.stack());
    }

    private static Fixture fixture(boolean available) throws Exception {
        MEStorageMenu menu = mock(MEStorageMenu.class, CALLS_REAL_METHODS);
        var previous = MEStorageMenu.class.getDeclaredField("myotus$prevUpgradeItems");
        previous.setAccessible(true);
        previous.set(menu, new IdentityHashMap<Slot, ItemStack>());
        Item item = mock(Item.class, withSettings().extraInterfaces(ITerminalCard.class));
        ITerminalCard card = (ITerminalCard) item;
        when(card.isAvailable()).thenReturn(available);
        ItemStack stack = mock(ItemStack.class);
        when(stack.getItem()).thenReturn(item);
        when(stack.is(item)).thenReturn(true);
        when(stack.copy()).thenReturn(stack);
        Slot slot = mock(Slot.class);
        when(slot.getItem()).thenReturn(stack);
        doReturn(List.of(slot)).when(menu).getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT);
        return new Fixture(menu, slot, stack, card);
    }

    private static void invoke(Class<?> owner, Object target, String name, Object... arguments) throws Exception {
        // Mixin prefixes injected handler names; the original identifying suffix stays intact.
        List<Method> methods = Arrays.stream(owner.getDeclaredMethods())
                .filter(method -> method.getName().contains(name))
                .filter(method -> method.getParameterCount() == arguments.length)
                .toList();
        assertEquals(1, methods.size(), "Expected the transformed Myotus callback: " + name);
        Method method = methods.getFirst();
        method.setAccessible(true);
        method.invoke(target, arguments);
    }

    private record Fixture(MEStorageMenu menu, Slot slot, ItemStack stack, ITerminalCard card) {
        void removeCard() throws Exception {
            when(slot.getItem()).thenReturn(ItemStack.EMPTY);
            invoke(MEStorageMenu.class, menu, "myotus$checkUpgradeSlotChanges");
        }

        void closeMenu() throws Exception {
            invoke(AbstractContainerMenu.class, menu, "myotus$dispatchUpgradeClose",
                    mock(ServerPlayer.class), new CallbackInfo("removed", false));
        }
    }
}
