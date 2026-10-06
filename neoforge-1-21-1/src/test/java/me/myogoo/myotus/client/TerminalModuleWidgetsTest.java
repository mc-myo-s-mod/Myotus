package me.myogoo.myotus.client;

import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.me.common.TerminalSettingsScreen;
import appeng.client.gui.style.WidgetStyle;
import me.myogoo.myotus.api.ITerminalUpgradeCard;
import me.myogoo.myotus.api.ITerminalCard;
import me.myogoo.myotus.api.client.AbstractTerminalModuleWidget;
import me.myogoo.myotus.client.gui.config.CustomConfigTabScreen;
import me.myogoo.myotus.client.gui.config.TabbedTerminalSettingsScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class TerminalModuleWidgetsTest {
    @Test
    void slotlessModuleSharesLayeringCaptureAndBlocksCoveredNativeSlots() {
        var equipment = panel(0);
        var slotless = mock(AbstractTerminalModuleWidget.class);
        when(slotless.getBounds()).thenReturn(new Rect2i(10, 0, 30, 30));
        when(slotless.isVisible()).thenReturn(true);
        Slot slot = mock(Slot.class);
        when(equipment.ownsSlot(slot)).thenReturn(true);
        Item equipmentCard = card();
        Item slotlessCard = card();
        TerminalModuleWidgets.register(equipmentCard, (screen, stack) -> equipment);
        TerminalModuleWidgets.register(slotlessCard, (screen, stack) -> slotless);
        var modules = new TerminalModuleWidgets();
        Map<String, ICompositeWidget> widgets = new LinkedHashMap<>();
        Map<String, WidgetStyle> styles = new LinkedHashMap<>();
        modules.update(null, List.of(stack(equipmentCard), stack(slotlessCard)), widgets, styles);

        var overlap = new Point(15, 15);
        assertEquals(List.of(equipment, slotless), modules.getModuleWidgets());
        assertThrows(UnsupportedOperationException.class, () -> modules.getModuleWidgets().clear());
        assertEquals(List.of(equipment), modules.getEquipmentPanels());
        assertSame(slotless, modules.getModuleAt(overlap));
        assertNull(modules.getPanelAt(overlap));
        assertFalse(modules.isSlotAccessible(slot, overlap));
        assertTrue(modules.isSlotAccessible(slot, new Point(5, 15)));

        modules.onMouseDown(new Point(5, 15), 0);
        assertSame(equipment, modules.getModuleAt(overlap));
        assertTrue(modules.isSlotAccessible(slot, overlap));
        var exposedSlotless = new Point(35, 15);
        when(slotless.onMouseDown(exposedSlotless, 0)).thenReturn(true);
        assertEquals(Boolean.TRUE, modules.onMouseDown(exposedSlotless, 0));
        assertSame(slotless, modules.getModuleAt(overlap));
        var outside = new Point(100, 100);
        assertEquals(Boolean.TRUE, modules.onMouseDrag(outside, 0));
        verify(slotless).onMouseDrag(outside, 0);
        assertEquals(Boolean.TRUE, modules.onMouseWheel(overlap, -1));
        verify(slotless).onMouseWheel(overlap, -1);

        modules.update(null, List.of(stack(equipmentCard)), widgets, styles);
        verify(slotless).onMouseUp(Point.ZERO, 0);
        assertEquals(List.of(equipment), modules.getModuleWidgets());
        assertNull(modules.onMouseDrag(overlap, 0));
        assertTrue(modules.isSlotAccessible(slot, overlap));
    }

    @Test
    void restoredUnavailableCardNeverCreatesAWidget() {
        Item card = card();
        when(((ITerminalCard) card).isAvailable()).thenReturn(false);
        AtomicInteger creations = new AtomicInteger();
        TerminalModuleWidgets.register(card, (screen, stack) -> {
            creations.incrementAndGet();
            throw new AssertionError("Unavailable card factory must not run");
        });
        Map<String, ICompositeWidget> widgets = new LinkedHashMap<>();
        Map<String, WidgetStyle> styles = new LinkedHashMap<>();
        var modules = new TerminalModuleWidgets();
        assertFalse(modules.update(null, List.of(stack(card)), widgets, styles));
        assertEquals(0, creations.get());
        assertTrue(widgets.isEmpty());
        assertTrue(styles.isEmpty());
    }

    @Test
    void settingsScreensNeverCreateModulesAndLeaveTheParentWidgetIntact() {
        Item card = card();
        AtomicInteger creations = new AtomicInteger();
        ICompositeWidget widget = widget();
        TerminalModuleWidgets.register(card, (screen, stack) -> {
            creations.incrementAndGet();
            return widget;
        });
        AEBaseScreen<?> parent = mock(AEBaseScreen.class);
        var parentModules = new TerminalModuleWidgets();
        Map<String, ICompositeWidget> parentWidgets = new LinkedHashMap<>();
        Map<String, WidgetStyle> parentStyles = new LinkedHashMap<>();
        List<ItemStack> installed = List.of(stack(card));
        assertTrue(parentModules.update(parent, installed, parentWidgets, parentStyles));
        WidgetStyle parentStyle = parentStyles.values().iterator().next();

        for (AEBaseScreen<?> settings : List.of(mock(TerminalSettingsScreen.class),
                mock(TabbedTerminalSettingsScreen.class), mock(CustomConfigTabScreen.class))) {
            var modules = new TerminalModuleWidgets();
            Map<String, ICompositeWidget> widgets = new LinkedHashMap<>();
            Map<String, WidgetStyle> styles = new LinkedHashMap<>();
            assertFalse(modules.update(settings, installed, widgets, styles));
            assertTrue(widgets.isEmpty());
            assertTrue(styles.isEmpty());
            assertTrue(modules.getEquipmentPanels().isEmpty());
        }

        assertFalse(parentModules.update(parent, installed, parentWidgets, parentStyles));
        assertEquals(1, creations.get());
        assertSame(widget, parentWidgets.values().iterator().next());
        assertSame(parentStyle, parentStyles.values().iterator().next());
    }

    @Test
    void validatesRegistrationsWithoutReplacingTheOriginalFactory() {
        Item card = card();
        assertThrows(NullPointerException.class, () -> TerminalModuleWidgets.register(null, (screen, stack) -> null));
        assertThrows(NullPointerException.class, () -> TerminalModuleWidgets.register(card, null));
        assertThrows(IllegalArgumentException.class,
                () -> TerminalModuleWidgets.register(mock(Item.class), (screen, stack) -> null));
        ICompositeWidget widget = widget();
        TerminalModuleWidgets.register(card, (screen, stack) -> widget);
        assertThrows(IllegalArgumentException.class,
                () -> TerminalModuleWidgets.register(card, (screen, stack) -> null));

        Map<String, ICompositeWidget> widgets = new LinkedHashMap<>();
        assertTrue(new TerminalModuleWidgets().update(null, List.of(stack(card)), widgets, new LinkedHashMap<>()));
        assertSame(widget, widgets.values().iterator().next());
    }

    @Test
    void reconcilesItemIdentityAndSnapshotsWithoutRecreatingOnResizeOrDataChanges() {
        Item card = card();
        ItemStack installed = stack(card);
        ItemStack snapshot = installed.copy();
        ICompositeWidget widget = widget();
        AtomicInteger creations = new AtomicInteger();
        TerminalModuleWidgets.register(card, (screen, stack) -> {
            creations.incrementAndGet();
            if (creations.get() == 1) {
                assertSame(snapshot, stack);
            }
            return widget;
        });
        var modules = new TerminalModuleWidgets();
        Map<String, ICompositeWidget> widgets = new LinkedHashMap<>();
        Map<String, WidgetStyle> styles = new LinkedHashMap<>();
        assertTrue(modules.update(null, List.of(installed, installed), widgets, styles));
        assertEquals(1, creations.get());
        WidgetStyle layout = styles.values().iterator().next();
        assertEquals(-40, layout.getLeft());
        assertEquals(10, layout.getTop());
        assertEquals(30, layout.getWidth());
        assertEquals(20, layout.getHeight());

        // Another screen can remove the shared cached style. A resize must restore it, not recreate the widget.
        styles.clear();
        assertFalse(modules.update(null, List.of(installed), widgets, styles));
        assertSame(layout, styles.values().iterator().next());
        assertFalse(modules.update(null, List.of(stack(card)), widgets, styles));
        assertEquals(1, creations.get());
        assertSame(widget, widgets.values().iterator().next());

        assertTrue(modules.update(null, List.of(), widgets, styles));
        assertTrue(widgets.isEmpty());
        assertTrue(styles.isEmpty());
        assertFalse(modules.update(null, List.of(), widgets, styles));
        assertTrue(modules.update(null, List.of(installed), widgets, styles));
        assertEquals(2, creations.get());
    }

    @Test
    void reopensWithCachedStylesAndRestoresEachScreensOwnGeometry() {
        Item card = card();
        AtomicInteger creations = new AtomicInteger();
        TerminalModuleWidgets.register(card, (screen, stack) -> {
            ICompositeWidget widget = mock(ICompositeWidget.class);
            when(widget.getBounds()).thenReturn(new Rect2i(creations.incrementAndGet() * 10, 0, 20, 20));
            return widget;
        });
        var first = new TerminalModuleWidgets();
        var reopened = new TerminalModuleWidgets();
        Map<String, ICompositeWidget> firstWidgets = new LinkedHashMap<>();
        Map<String, ICompositeWidget> reopenedWidgets = new LinkedHashMap<>();
        Map<String, WidgetStyle> cachedStyles = new LinkedHashMap<>();
        List<ItemStack> installed = List.of(stack(card));
        assertTrue(first.update(null, installed, firstWidgets, cachedStyles));
        assertTrue(reopened.update(null, installed, reopenedWidgets, cachedStyles));
        assertEquals(1, cachedStyles.size());
        assertEquals(20, cachedStyles.values().iterator().next().getLeft());
        assertFalse(first.update(null, installed, firstWidgets, cachedStyles));
        assertEquals(10, cachedStyles.values().iterator().next().getLeft());
        assertTrue(reopened.update(null, List.of(), reopenedWidgets, cachedStyles));
        assertFalse(first.update(null, installed, firstWidgets, cachedStyles));
        assertEquals(10, cachedStyles.values().iterator().next().getLeft());
        assertEquals(2, creations.get());
    }

    @Test
    void ignoresUnregisteredCardsAndRejectsNullWidgets() {
        var modules = new TerminalModuleWidgets();
        assertFalse(modules.update(null, List.of(stack(card())), new LinkedHashMap<>(), new LinkedHashMap<>()));
        Item card = card();
        TerminalModuleWidgets.register(card, (screen, stack) -> null);
        assertThrows(NullPointerException.class,
                () -> modules.update(null, List.of(stack(card)), new LinkedHashMap<>(), new LinkedHashMap<>()));
    }

    @Test
    void exposedPanelClickRaisesItWithoutChangingWidgetOrderAndSurvivesUpdates() {
        Item firstCard = card();
        Item secondCard = card();
        TerminalEquipmentPanel first = panel(0);
        TerminalEquipmentPanel second = panel(10);
        TerminalModuleWidgets.register(firstCard, (screen, stack) -> first);
        TerminalModuleWidgets.register(secondCard, (screen, stack) -> second);
        var modules = new TerminalModuleWidgets();
        Map<String, ICompositeWidget> widgets = new LinkedHashMap<>();
        Map<String, WidgetStyle> styles = new LinkedHashMap<>();
        List<ItemStack> installed = List.of(stack(firstCard), stack(secondCard));
        modules.update(null, installed, widgets, styles);
        Point overlap = new Point(15, 15);
        Point exposed = new Point(5, 15);
        assertSame(second, modules.getPanelAt(overlap));
        assertEquals(List.of(first, second), modules.getEquipmentPanels());
        assertThrows(UnsupportedOperationException.class, () -> modules.getEquipmentPanels().clear());
        when(first.onMouseDown(exposed, 0)).thenReturn(true);

        assertEquals(Boolean.TRUE, modules.onMouseDown(exposed, 0));
        assertSame(first, modules.getPanelAt(overlap));
        assertEquals(List.of(second, first), modules.getEquipmentPanels());
        assertEquals(List.of(first, second), List.copyOf(widgets.values()));
        verify(second, never()).onMouseDown(exposed, 0);
        assertFalse(modules.update(null, installed, widgets, styles));
        assertEquals(List.of(second, first), modules.getEquipmentPanels());
        when(first.isVisible()).thenReturn(false);
        assertSame(second, modules.getPanelAt(overlap));
    }

    @Test
    void nativeSlotClickBypassesOtherWidgetsWithoutCapturingAndFiltersCoveredSlots() {
        TerminalEquipmentPanel first = panel(0);
        TerminalEquipmentPanel second = panel(10);
        var modules = modules(first, second);
        Slot firstSlot = mock(Slot.class);
        Slot secondSlot = mock(Slot.class);
        Slot terminalSlot = mock(Slot.class);
        when(first.ownsSlot(firstSlot)).thenReturn(true);
        when(second.ownsSlot(secondSlot)).thenReturn(true);
        Point overlap = new Point(15, 15);

        assertEquals(Boolean.FALSE, modules.onMouseDown(overlap, 0));
        verify(second).onMouseDown(overlap, 0);
        verify(first, never()).onMouseDown(overlap, 0);
        assertNull(modules.onMouseDrag(overlap, 0));
        assertNull(modules.onMouseUp(overlap, 0));
        assertTrue(modules.ownsSlot(firstSlot));
        assertFalse(modules.ownsSlot(terminalSlot));
        assertTrue(modules.isSlotAccessible(secondSlot, overlap));
        assertFalse(modules.isSlotAccessible(firstSlot, overlap));
        assertFalse(modules.isSlotAccessible(terminalSlot, overlap));
        assertTrue(modules.isSlotAccessible(firstSlot, new Point(5, 15)));
        Point outside = new Point(-1, 15);
        assertFalse(modules.isSlotAccessible(firstSlot, outside));
        assertTrue(modules.isSlotAccessible(terminalSlot, outside));
        assertNull(modules.onMouseDown(outside, 0));
    }

    @Test
    void movingPanelBlocksItsOwnAndCoveredSlotsButNotExposedSlots() {
        TerminalEquipmentPanel first = panel(0);
        TerminalEquipmentPanel second = panel(10);
        var modules = modules(first, second);
        Slot firstSlot = mock(Slot.class);
        Slot secondSlot = mock(Slot.class);
        Slot terminalSlot = mock(Slot.class);
        when(first.ownsSlot(firstSlot)).thenReturn(true);
        when(second.ownsSlot(secondSlot)).thenReturn(true);
        when(second.isMoving()).thenReturn(true);
        Point overlap = new Point(15, 15);

        assertFalse(modules.isSlotAccessible(secondSlot, overlap));
        assertFalse(modules.isSlotAccessible(firstSlot, overlap));
        assertFalse(modules.isSlotAccessible(terminalSlot, overlap));
        assertTrue(modules.isSlotAccessible(firstSlot, new Point(5, 15)));
        assertTrue(modules.isSlotAccessible(terminalSlot, new Point(100, 100)));
        assertFalse(modules.isSlotAccessible(secondSlot, new Point(100, 100)));
        assertEquals(Boolean.TRUE, modules.onMouseWheel(overlap, -1));
        when(second.isMoving()).thenReturn(false);
        assertTrue(modules.isSlotAccessible(secondSlot, overlap));
        assertFalse(modules.isSlotAccessible(terminalSlot, overlap));
    }

    @Test
    void dragStaysCapturedAcrossAnotherPanelAndOutsideUntilLeftRelease() {
        TerminalEquipmentPanel first = panel(0);
        TerminalEquipmentPanel second = panel(10);
        var modules = modules(first, second);
        Point grip = new Point(5, 2);
        Point overSecond = new Point(35, 15);
        Point outside = new Point(100, 100);
        when(first.onMouseDown(grip, 0)).thenReturn(true);
        assertEquals(Boolean.TRUE, modules.onMouseDown(grip, 0));

        assertEquals(Boolean.TRUE, modules.onMouseDrag(overSecond, 0));
        assertEquals(Boolean.TRUE, modules.onMouseDrag(outside, 0));
        verify(first).onMouseDrag(overSecond, 0);
        verify(first).onMouseDrag(outside, 0);
        verify(second, never()).onMouseDrag(overSecond, 0);
        assertNull(modules.onMouseUp(outside, 1));
        assertEquals(Boolean.TRUE, modules.onMouseUp(outside, 0));
        verify(first).onMouseUp(outside, 0);
        assertNull(modules.onMouseDrag(overSecond, 0));
        assertNull(modules.onMouseUp(outside, 0));
    }

    @Test
    void wheelOverNonScrollingPanelIsConsumedWithoutLeakingToBackPanel() {
        TerminalEquipmentPanel first = panel(0);
        TerminalEquipmentPanel second = panel(10);
        var modules = modules(first, second);
        Point overlap = new Point(15, 15);
        assertEquals(Boolean.TRUE, modules.onMouseWheel(overlap, -1));
        verify(second).onMouseWheel(overlap, -1);
        verify(first, never()).onMouseWheel(overlap, -1);
        assertNull(modules.onMouseWheel(new Point(100, 100), -1));
    }

    @Test
    void removedOrHiddenPanelReleasesCaptureWithoutBlockingFurtherInput() {
        Item firstCard = card();
        Item secondCard = card();
        TerminalEquipmentPanel first = panel(0);
        TerminalEquipmentPanel second = panel(10);
        TerminalModuleWidgets.register(firstCard, (screen, stack) -> first);
        TerminalModuleWidgets.register(secondCard, (screen, stack) -> second);
        var modules = new TerminalModuleWidgets();
        Map<String, ICompositeWidget> widgets = new LinkedHashMap<>();
        Map<String, WidgetStyle> styles = new LinkedHashMap<>();
        modules.update(null, List.of(stack(firstCard), stack(secondCard)), widgets, styles);
        Point firstGrip = new Point(5, 2);
        when(first.onMouseDown(firstGrip, 0)).thenReturn(true);
        assertEquals(Boolean.TRUE, modules.onMouseDown(firstGrip, 0));
        assertTrue(modules.update(null, List.of(stack(secondCard)), widgets, styles));
        verify(first).onMouseUp(Point.ZERO, 0);
        assertEquals(List.of(second), modules.getEquipmentPanels());
        assertNull(modules.onMouseDrag(firstGrip, 0));
        assertNull(modules.onMouseUp(firstGrip, 0));

        Point secondGrip = new Point(15, 2);
        when(second.onMouseDown(secondGrip, 0)).thenReturn(true);
        assertEquals(Boolean.TRUE, modules.onMouseDown(secondGrip, 0));
        when(second.isVisible()).thenReturn(false);
        assertNull(modules.onMouseDrag(secondGrip, 0));
        verify(second).onMouseUp(Point.ZERO, 0);
        assertNull(modules.getPanelAt(secondGrip));
        assertNull(modules.onMouseUp(secondGrip, 0));
    }

    private static TerminalModuleWidgets modules(TerminalEquipmentPanel first, TerminalEquipmentPanel second) {
        Item firstCard = card();
        Item secondCard = card();
        TerminalModuleWidgets.register(firstCard, (screen, stack) -> first);
        TerminalModuleWidgets.register(secondCard, (screen, stack) -> second);
        var modules = new TerminalModuleWidgets();
        modules.update(null, List.of(stack(firstCard), stack(secondCard)), new LinkedHashMap<>(), new LinkedHashMap<>());
        return modules;
    }

    private static TerminalEquipmentPanel panel(int x) {
        TerminalEquipmentPanel panel = mock(TerminalEquipmentPanel.class);
        when(panel.getBounds()).thenReturn(new Rect2i(x, 0, 30, 30));
        when(panel.isVisible()).thenReturn(true);
        return panel;
    }

    private static Item card() {
        Item card = mock(Item.class, withSettings().extraInterfaces(ITerminalUpgradeCard.class));
        when(((ITerminalCard) card).isAvailable()).thenReturn(true);
        return card;
    }

    private static ItemStack stack(Item item) {
        ItemStack stack = mock(ItemStack.class);
        when(stack.getItem()).thenReturn(item);
        when(stack.copy()).thenReturn(mock(ItemStack.class));
        return stack;
    }

    private static ICompositeWidget widget() {
        ICompositeWidget widget = mock(ICompositeWidget.class);
        when(widget.getBounds()).thenReturn(new Rect2i(-40, 10, 30, 20));
        return widget;
    }
}
