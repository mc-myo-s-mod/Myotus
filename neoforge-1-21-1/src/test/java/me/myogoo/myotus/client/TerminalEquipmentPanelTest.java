package me.myogoo.myotus.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.stream.IntStream;

import appeng.client.Point;
import appeng.menu.SlotSemantic;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.SlotSemantics;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TerminalEquipmentPanelTest {
    private static final SlotSemantic FIVE_SLOT_MODULE = SlotSemantics.register("myotus_test_five_slots", false);
    private static final SlotSemantic SCROLLING_MODULE = SlotSemantics.register("myotus_test_scrolling_slots", false);

    @Test
    void customGridSharesAnimationAndRendersEverySlotIncludingOutput() {
        var grid = slots(10);
        var clock = new AtomicLong();
        var menu = mock(MEStorageMenu.class);
        when(menu.getSlots(FIVE_SLOT_MODULE)).thenReturn(grid);
        var panel = new TerminalEquipmentPanel(menu, FIVE_SLOT_MODULE, null, () -> true, clock::get) {
            private final Object initialized = new Object();
            @Override public Rect2i getBounds() {
                var bounds = super.getBounds();
                return new Rect2i(bounds.getX(), bounds.getY(), 104, 84);
            }
            @Override protected int dragHandleHeight() { return 20; }
            @Override protected boolean scrolling() { return false; }
            @Override protected List<Slot> visibleSlots() { return getSlots(); }
            @Override protected void drawPanelBackground(GuiGraphics graphics) {}
            @Override protected void updateSlotPositions() {
                assertNotNull(initialized, "Must not dispatch layout hooks during construction");
                var bounds = getBounds();
                for (int i = 0; i < getSlots().size(); i++) {
                    getSlots().get(i).x = bounds.getX() + (i < 9 ? 6 + i % 3 * 18 : 80);
                    getSlots().get(i).y = bounds.getY() + (i < 9 ? 23 + i / 3 * 18 : 41);
                }
            }
        };
        panel.layout(new Point(10, 20), new Rect2i(0, 0, 500, 300), bounds -> true);
        assertFalse(panel.onMouseDown(new Point(17, 44), 0));
        assertTrue(panel.onMouseDown(new Point(18, 28), 0));
        panel.onMouseDrag(new Point(118, 28), 0);
        assertEquals(10, panel.getBounds().getX(), "A large drag must animate, not jump");
        clock.set(80_000_000L);
        panel.updateBeforeRender();
        assertEquals(98, panel.getBounds().getX());
        assertEquals(178, grid.getLast().x);
        assertTrue(panel.onMouseDown(new Point(grid.getLast().x, grid.getLast().y), 0));
        panel.onMouseUp(Point.ZERO, 0);
        clock.set(160_000_000L);
        panel.updateBeforeRender();
        assertFalse(panel.onMouseDown(new Point(grid.getLast().x, grid.getLast().y), 0));
        var graphics = mock(GuiGraphics.class);
        when(graphics.pose()).thenReturn(new PoseStack());
        var rendered = new ArrayList<Slot>();
        var highlighted = new ArrayList<Slot>();
        panel.render(graphics, Point.ZERO, 50, rendered::add, highlighted::add);
        assertEquals(grid, rendered);
        assertEquals(grid, highlighted);
        when(grid.getLast().isActive()).thenReturn(false);
        panel.updateBeforeRender();
        assertEquals(-10000, grid.getLast().x);
        panel.layout(Point.ZERO, new Rect2i(0, 0, 20, 20), bounds -> true);
        assertFalse(panel.isVisible());
        assertTrue(grid.stream().allMatch(slot -> slot.x == -10000 && slot.y == -10000));
    }

    @Test
    void equipmentColumnsKeepRealSlotClicksAndRepositionWithoutOverlapping() {
        List<Slot> equipment = slots(5);
        List<Slot> curios = slots(9);
        TerminalEquipmentPanel equipmentPanel = panel(FIVE_SLOT_MODULE, equipment);
        TerminalEquipmentPanel curiosPanel = panel(SCROLLING_MODULE, curios);

        for (int start : new int[] {202, 300}) {
            equipmentPanel.setPosition(new Point(start, 0));
            curiosPanel.setPosition(new Point(equipmentPanel.getBounds().getX()
                    + equipmentPanel.getBounds().getWidth() + 2, 0));
            assertEquals(28, equipmentPanel.getBounds().getWidth());
            assertEquals(100, equipmentPanel.getBounds().getHeight());
            assertEquals(33, curiosPanel.getBounds().getWidth());
            assertEquals(100, curiosPanel.getBounds().getHeight());
            assertEquals(start + 6, equipment.getFirst().x);
            assertEquals(start + 6, equipment.getLast().x);
            assertEquals(78, equipment.getLast().y);
            assertTrue(equipmentPanel.getBounds().getX() + equipmentPanel.getBounds().getWidth()
                    < curiosPanel.getBounds().getX());
            assertNull(equipmentPanel.getTooltip(start + 2, 2));
            assertNull(curiosPanel.getTooltip(curiosPanel.getBounds().getX() + 2, 2));
            assertFalse(equipmentPanel.onMouseDown(new Point(start + 6, 6), 0));
            assertTrue(equipmentPanel.onMouseDown(new Point(start, 0), 0));
            assertFalse(equipmentPanel.onMouseDown(new Point(start - 2, 0), 0));
            assertFalse(equipmentPanel.onMouseWheel(new Point(start + 6, 6), -1));
        }

        List<Rect2i> exclusions = new ArrayList<>();
        equipmentPanel.addExclusionZones(exclusions, new Rect2i(10, 20, 300, 200));
        assertEquals(1, exclusions.size());
        assertEquals(310, exclusions.getFirst().getX());
        assertEquals(20, exclusions.getFirst().getY());
    }

    @Test
    void curiosScrollClampsAndHidesOffscreenOrInactiveSlots() {
        List<Slot> curios = slots(9);
        TerminalEquipmentPanel panel = panel(SCROLLING_MODULE, curios);
        panel.setPosition(new Point(240, 10));
        Point mouse = new Point(246, 16);
        assertEquals(16, curios.getFirst().y);
        assertEquals(88, curios.get(4).y);
        assertEquals(-10000, curios.get(5).x);
        assertFalse(panel.onMouseWheel(Point.ZERO, -1));
        assertTrue(panel.onMouseWheel(mouse, -1));
        panel.updateBeforeRender();
        assertEquals(-10000, curios.getFirst().x);
        assertEquals(16, curios.get(1).y);
        assertEquals(88, curios.get(5).y);
        for (int i = 0; i < 20; i++) {
            panel.onMouseWheel(mouse, -1);
        }
        panel.updateBeforeRender();
        assertEquals(-10000, curios.get(3).x);
        assertEquals(16, curios.get(4).y);
        assertEquals(88, curios.getLast().y);
        for (int i = 0; i < 20; i++) {
            panel.onMouseWheel(mouse, 1);
        }
        when(curios.getFirst().isActive()).thenReturn(false);
        panel.updateBeforeRender();
        assertEquals(-10000, curios.getFirst().x);
        assertEquals(34, curios.get(1).y);
        assertEquals(-10000, curios.get(5).x);
    }

    @Test
    void emptyCuriosPanelHasTooltipAndNoScrollbar() {
        TerminalEquipmentPanel panel = panel(SCROLLING_MODULE, List.of());
        panel.setPosition(new Point(200, 0));
        assertEquals(28, panel.getBounds().getHeight());
        assertEquals(28, panel.getBounds().getWidth());
        assertNull(panel.getTooltip(202, 2));
        assertNotNull(panel.getTooltip(206, 6));
        assertTrue(panel.onMouseDown(new Point(206, 6), 0));
        assertFalse(panel.onMouseWheel(new Point(206, 6), -1));
    }

    @Test
    void headerDragMovesSlotsAndMouseUpEndsDrag() {
        List<Slot> equipment = slots(5);
        TerminalEquipmentPanel panel = panel(FIVE_SLOT_MODULE, equipment);
        panel.setPosition(new Point(100, 20));

        assertTrue(panel.onMouseDown(new Point(102, 22), 0));
        assertTrue(panel.onMouseDrag(new Point(152, 62), 0));
        assertEquals(150, panel.getBounds().getX());
        assertEquals(60, panel.getBounds().getY());
        for (int i = 0; i < equipment.size(); i++) {
            assertEquals(156, equipment.get(i).x);
            assertEquals(66 + i * 18, equipment.get(i).y);
        }

        assertTrue(panel.onMouseUp(Point.ZERO, 0));
        assertFalse(panel.onMouseDrag(new Point(200, 100), 0));
        assertEquals(150, panel.getBounds().getX());
        assertEquals(60, panel.getBounds().getY());
    }

    @Test
    void slotClicksAndRightClickDoNotStartDragging() {
        List<Slot> equipment = slots(5);
        TerminalEquipmentPanel panel = panel(FIVE_SLOT_MODULE, equipment);
        panel.setPosition(new Point(100, 20));

        assertFalse(panel.onMouseDown(new Point(107, 27), 0));
        assertFalse(panel.onMouseDrag(new Point(157, 67), 0));
        assertTrue(panel.onMouseDown(new Point(102, 22), 1));
        assertFalse(panel.onMouseDrag(new Point(152, 62), 1));
        assertEquals(100, panel.getBounds().getX());
        assertEquals(20, panel.getBounds().getY());
    }

    @Test
    void movedPositionSurvivesLayoutAndSetPosition() {
        TerminalEquipmentPanel panel = panel(FIVE_SLOT_MODULE, slots(5));
        Rect2i movementBounds = new Rect2i(0, 0, 400, 200);
        panel.layout(new Point(100, 20), movementBounds, bounds -> true);
        assertTrue(panel.onMouseDown(new Point(102, 22), 0));
        assertTrue(panel.onMouseDrag(new Point(152, 62), 0));
        panel.onMouseUp(Point.ZERO, 0);

        panel.setPosition(new Point(180, 70));
        assertEquals(150, panel.getBounds().getX());
        assertEquals(60, panel.getBounds().getY());
        panel.layout(new Point(200, 80), movementBounds, bounds -> true);
        assertEquals(150, panel.getBounds().getX());
        assertEquals(60, panel.getBounds().getY());
    }

    @Test
    void dragClampsToViewportAndRejectedPlacementKeepsOldPosition() {
        TerminalEquipmentPanel panel = panel(FIVE_SLOT_MODULE, slots(5));
        Rect2i movementBounds = new Rect2i(10, 20, 120, 100);
        panel.layout(new Point(50, 25), movementBounds, bounds -> true);
        assertTrue(panel.onMouseDown(new Point(51, 21), 0));
        assertTrue(panel.onMouseDrag(new Point(500, 500), 0));
        assertEquals(102, panel.getBounds().getX());
        assertEquals(20, panel.getBounds().getY());
        panel.onMouseUp(Point.ZERO, 0);

        panel.layout(new Point(50, 25), movementBounds, bounds -> bounds.getX() >= 100);
        assertTrue(panel.onMouseDown(new Point(103, 21), 0));
        assertTrue(panel.onMouseDrag(new Point(30, 30), 0));
        assertEquals(102, panel.getBounds().getX());
        assertEquals(20, panel.getBounds().getY());
    }

    @Test
    void layoutMovesPanelToDefaultWhenResizeMakesDraggedPositionInvalid() {
        TerminalEquipmentPanel panel = panel(FIVE_SLOT_MODULE, slots(5));
        Rect2i movementBounds = new Rect2i(0, 0, 300, 150);
        panel.layout(new Point(20, 20), movementBounds, bounds -> true);
        assertTrue(panel.onMouseDown(new Point(22, 22), 0));
        assertTrue(panel.onMouseDrag(new Point(152, 42), 0));
        panel.onMouseUp(Point.ZERO, 0);
        assertEquals(150, panel.getBounds().getX());

        panel.layout(new Point(40, 30), new Rect2i(0, 0, 200, 150), bounds -> bounds.getX() < 100);
        assertEquals(40, panel.getBounds().getX());
        assertEquals(30, panel.getBounds().getY());
        assertTrue(panel.getBounds().getX() < 100);

        panel.layout(new Point(20, 20), movementBounds, bounds -> true);
        assertEquals(150, panel.getBounds().getX());
        assertEquals(40, panel.getBounds().getY());
    }

    @Test
    @SuppressWarnings("unchecked")
    void positionSavesOnlyAtDragEndAndRestoresAcrossPanelsResizeAndReset() {
        var preferred = new AtomicReference<List<Integer>>(List.of());
        ModConfigSpec.ConfigValue<List<Integer>> setting = mock(ModConfigSpec.ConfigValue.class);
        when(setting.get()).thenAnswer(invocation -> preferred.get());
        doAnswer(invocation -> {
            preferred.set(invocation.getArgument(0));
            return null;
        }).when(setting).set(anyList());
        MEStorageMenu menu = mock(MEStorageMenu.class);
        List<Slot> equipment = slots(5);
        when(menu.getSlots(FIVE_SLOT_MODULE)).thenReturn(equipment);
        TerminalEquipmentPanel panel = new TerminalEquipmentPanel(menu, FIVE_SLOT_MODULE, setting,
                () -> false, System::nanoTime);
        Rect2i viewport = new Rect2i(0, 0, 400, 250);
        Point automatic = new Point(20, 20);
        panel.layout(automatic, viewport, bounds -> true);

        // A header click is not a position edit, even if layout changes before release.
        assertTrue(panel.onMouseDown(new Point(22, 22), 0));
        panel.layout(new Point(40, 20), viewport, bounds -> true);
        panel.onMouseUp(Point.ZERO, 0);
        verify(setting, never()).save();
        panel.layout(automatic, viewport, bounds -> true);

        assertTrue(panel.onMouseDown(new Point(22, 22), 0));
        assertTrue(panel.onMouseDrag(new Point(202, 82), 0));
        panel.layout(automatic, viewport, bounds -> true);
        verify(setting, never()).set(anyList());
        verify(setting, never()).save();
        assertTrue(panel.onMouseUp(Point.ZERO, 0));
        assertEquals(List.of(200, 80), preferred.get());
        verify(setting, times(1)).save();

        TerminalEquipmentPanel reopened = new TerminalEquipmentPanel(menu, FIVE_SLOT_MODULE, setting,
                () -> false, System::nanoTime);
        reopened.layout(automatic, viewport, bounds -> true);
        assertEquals(200, reopened.getBounds().getX());
        assertEquals(80, reopened.getBounds().getY());

        reopened.layout(automatic, new Rect2i(0, 0, 100, 150), bounds -> true);
        assertEquals(72, reopened.getBounds().getX());
        assertEquals(50, reopened.getBounds().getY());
        assertEquals(List.of(200, 80), preferred.get());
        reopened.layout(automatic, viewport, bounds -> true);
        assertEquals(200, reopened.getBounds().getX());
        assertEquals(80, reopened.getBounds().getY());
        reopened.layout(automatic, viewport, bounds -> bounds.getX() < 100);
        assertEquals(20, reopened.getBounds().getX());
        reopened.layout(automatic, viewport, bounds -> true);
        assertEquals(200, reopened.getBounds().getX());
        verify(setting, times(1)).save();

        setting.set(List.of());
        // Both already-created screens consult the cleared preference on their next layout.
        panel.layout(new Point(40, 30), viewport, bounds -> true);
        reopened.layout(automatic, viewport, bounds -> true);
        assertEquals(40, panel.getBounds().getX());
        assertEquals(30, panel.getBounds().getY());
        assertEquals(20, reopened.getBounds().getX());
        assertEquals(20, reopened.getBounds().getY());
        verify(setting, times(1)).save();
    }

    @Test
    void panelHidesWhenNoPlacementFitsAndReappearsWhenLayoutBecomesValid() {
        List<Slot> equipment = slots(5);
        TerminalEquipmentPanel panel = panel(FIVE_SLOT_MODULE, equipment);
        panel.layout(new Point(0, 0), new Rect2i(0, 0, 20, 20), bounds -> false);
        assertFalse(panel.isVisible());
        assertEquals(-10000, equipment.getFirst().x);

        panel.layout(new Point(10, 15), new Rect2i(0, 0, 100, 100), bounds -> true);
        assertTrue(panel.isVisible());
        assertEquals(16, equipment.getFirst().x);
        assertEquals(6, equipment.getFirst().y);
    }

    @Test
    void draggingAfterScrollKeepsTheSameSlotsVisible() {
        List<Slot> curios = slots(9);
        TerminalEquipmentPanel panel = panel(SCROLLING_MODULE, curios);
        panel.setPosition(new Point(240, 10));
        Point mouse = new Point(246, 16);
        assertTrue(panel.onMouseWheel(mouse, -1));
        panel.updateBeforeRender();
        assertEquals(-10000, curios.getFirst().x);
        assertEquals(16, curios.get(1).y);

        assertTrue(panel.onMouseDown(new Point(242, 12), 0));
        assertTrue(panel.onMouseDrag(new Point(272, 52), 0));
        panel.updateBeforeRender();
        assertEquals(-10000, curios.getFirst().x);
        assertEquals(276, curios.get(1).x);
        assertEquals(56, curios.get(1).y);
        assertEquals(276, curios.get(5).x);
        assertEquals(128, curios.get(5).y);
        assertEquals(-10000, curios.get(6).x);
    }

    @Test
    void largeDragUsesElapsedTimeAndKeepsSlotsAlignedAcrossRepeatedLayout() {
        AtomicLong clock = new AtomicLong();
        List<Slot> equipment = slots(5);
        TerminalEquipmentPanel panel = animatedPanel(equipment, () -> true, clock);
        Point initial = new Point(20, 20);
        Rect2i viewport = new Rect2i(0, 0, 400, 250);
        panel.layout(initial, viewport, bounds -> true);
        assertFalse(panel.isMoving());
        assertTrue(panel.onMouseDown(new Point(22, 22), 0));
        assertTrue(panel.onMouseDrag(new Point(182, 102), 0));
        assertTrue(panel.isMoving());
        assertPositionAndSlots(panel, equipment, 20, 20);

        clock.set(80_000_000L);
        panel.updateBeforeRender();
        assertPositionAndSlots(panel, equipment, 160, 90);
        panel.layout(initial, viewport, bounds -> true);
        panel.setPosition(new Point(300, 140));
        panel.onMouseDrag(new Point(182, 102), 0);
        assertPositionAndSlots(panel, equipment, 160, 90);
        assertTrue(panel.isMoving());

        clock.set(160_000_000L);
        panel.layout(initial, viewport, bounds -> true);
        assertPositionAndSlots(panel, equipment, 180, 100);
        assertFalse(panel.isMoving());
    }

    @Test
    void smallDragIsImmediateButRetargetingAnActiveMovementStaysContinuous() {
        AtomicLong clock = new AtomicLong();
        List<Slot> equipment = slots(5);
        TerminalEquipmentPanel panel = animatedPanel(equipment, () -> true, clock);
        panel.setPosition(new Point(20, 20));
        panel.onMouseDown(new Point(22, 22), 0);
        panel.onMouseDrag(new Point(58, 42), 0);
        assertPositionAndSlots(panel, equipment, 56, 40);
        assertFalse(panel.isMoving());

        panel.onMouseDrag(new Point(218, 42), 0);
        clock.set(80_000_000L);
        panel.updateBeforeRender();
        assertPositionAndSlots(panel, equipment, 196, 40);
        panel.onMouseDrag(new Point(228, 42), 0);
        assertPositionAndSlots(panel, equipment, 196, 40);
        assertTrue(panel.isMoving());
        clock.set(160_000_000L);
        panel.updateBeforeRender();
        assertPositionAndSlots(panel, equipment, 222, 40);
        clock.set(240_000_000L);
        panel.updateBeforeRender();
        assertPositionAndSlots(panel, equipment, 226, 40);
        assertFalse(panel.isMoving());
    }

    @Test
    void rejectedDragKeepsPositionAndRecoveryAnimatesEvenBelowDistanceThreshold() {
        AtomicLong clock = new AtomicLong();
        List<Slot> equipment = slots(5);
        TerminalEquipmentPanel panel = animatedPanel(equipment, () -> true, clock);
        panel.layout(new Point(10, 20), new Rect2i(0, 0, 400, 200), bounds -> bounds.getX() < 50);
        panel.onMouseDown(new Point(12, 22), 0);
        panel.onMouseDrag(new Point(102, 22), 0);
        assertPositionAndSlots(panel, equipment, 10, 20);
        assertFalse(panel.isMoving());
        panel.onMouseDrag(new Point(12, 22), 0);
        assertFalse(panel.isMoving());
        panel.onMouseDrag(new Point(102, 22), 0);
        panel.onMouseDrag(new Point(30, 22), 0);
        assertTrue(panel.isMoving());
        assertPositionAndSlots(panel, equipment, 10, 20);
        clock.set(80_000_000L);
        panel.updateBeforeRender();
        assertPositionAndSlots(panel, equipment, 26, 20);
        clock.set(160_000_000L);
        panel.updateBeforeRender();
        assertPositionAndSlots(panel, equipment, 28, 20);
        assertFalse(panel.isMoving());
    }

    @Test
    @SuppressWarnings("unchecked")
    void releaseSavesOnlyValidTargetAndResetCancelsInFlightMovement() {
        AtomicLong clock = new AtomicLong();
        var preferred = new AtomicReference<List<Integer>>(List.of());
        ModConfigSpec.ConfigValue<List<Integer>> setting = mock(ModConfigSpec.ConfigValue.class);
        when(setting.get()).thenAnswer(invocation -> preferred.get());
        doAnswer(invocation -> {
            preferred.set(invocation.getArgument(0));
            return null;
        }).when(setting).set(anyList());
        MEStorageMenu menu = mock(MEStorageMenu.class);
        List<Slot> equipment = slots(5);
        when(menu.getSlots(FIVE_SLOT_MODULE)).thenReturn(equipment);
        TerminalEquipmentPanel panel = new TerminalEquipmentPanel(menu, FIVE_SLOT_MODULE, setting,
                () -> true, clock::get);
        Rect2i viewport = new Rect2i(0, 0, 400, 200);
        Point initial = new Point(20, 20);
        panel.layout(initial, viewport, bounds -> bounds.getX() < 50 || bounds.getX() >= 200);
        panel.onMouseDown(new Point(22, 22), 0);
        panel.onMouseDrag(new Point(102, 22), 0);
        panel.onMouseUp(Point.ZERO, 0);
        verify(setting, never()).save();

        panel.onMouseDown(new Point(22, 22), 0);
        panel.onMouseDrag(new Point(202, 22), 0);
        clock.set(40_000_000L);
        panel.updateBeforeRender();
        assertTrue(panel.getBounds().getX() > 50 && panel.getBounds().getX() < 200);
        panel.onMouseDrag(new Point(102, 22), 0);
        assertTrue(panel.onMouseUp(Point.ZERO, 0));
        assertEquals(List.of(200, 20), preferred.get());
        verify(setting, times(1)).set(List.of(200, 20));
        verify(setting, times(1)).save();
        assertFalse(panel.onMouseUp(Point.ZERO, 0));
        panel.layout(initial, viewport, bounds -> bounds.getX() < 50 || bounds.getX() >= 200);
        assertTrue(panel.isMoving());

        preferred.set(List.of());
        panel.layout(initial, viewport, bounds -> true);
        assertFalse(panel.isMoving());
        assertPositionAndSlots(panel, equipment, 20, 20);
        verify(setting, times(1)).save();
    }

    @Test
    void disablingAnimationSnapsToTargetAndLeavesFollowingDragsImmediate() {
        AtomicLong clock = new AtomicLong();
        AtomicBoolean enabled = new AtomicBoolean(true);
        List<Slot> equipment = slots(5);
        TerminalEquipmentPanel panel = animatedPanel(equipment, enabled::get, clock);
        panel.setPosition(new Point(20, 20));
        panel.onMouseDown(new Point(22, 22), 0);
        panel.onMouseDrag(new Point(182, 102), 0);
        assertTrue(panel.isMoving());
        enabled.set(false);
        panel.updateBeforeRender();
        assertFalse(panel.isMoving());
        assertPositionAndSlots(panel, equipment, 180, 100);
        panel.onMouseDrag(new Point(302, 102), 0);
        assertFalse(panel.isMoving());
        assertPositionAndSlots(panel, equipment, 300, 100);
    }

    @Test
    void resizeAndHideCancelMotionWithoutResumingFromAnInvalidIntermediatePosition() {
        AtomicLong clock = new AtomicLong();
        List<Slot> equipment = slots(5);
        TerminalEquipmentPanel panel = animatedPanel(equipment, () -> true, clock);
        Rect2i viewport = new Rect2i(0, 0, 400, 250);
        panel.layout(new Point(20, 20), viewport, bounds -> true);
        panel.onMouseDown(new Point(22, 22), 0);
        panel.onMouseDrag(new Point(182, 102), 0);
        panel.onMouseUp(Point.ZERO, 0);
        panel.layout(new Point(20, 20), new Rect2i(0, 0, 100, 150), bounds -> true);
        assertFalse(panel.isMoving());
        assertPositionAndSlots(panel, equipment, 72, 50);
        panel.layout(new Point(20, 20), viewport, bounds -> true);
        assertPositionAndSlots(panel, equipment, 180, 100);
        assertFalse(panel.isMoving());

        panel.onMouseDown(new Point(182, 102), 0);
        panel.onMouseDrag(new Point(302, 102), 0);
        assertTrue(panel.isMoving());
        panel.layout(Point.ZERO, new Rect2i(0, 0, 20, 20), bounds -> false);
        assertFalse(panel.isVisible());
        assertFalse(panel.isMoving());
        equipment.forEach(slot -> {
            assertEquals(-10000, slot.x);
            assertEquals(-10000, slot.y);
        });
        assertFalse(panel.onMouseDrag(new Point(202, 102), 0));
        clock.set(160_000_000L);
        panel.layout(new Point(20, 20), viewport, bounds -> true);
        assertTrue(panel.isVisible());
        assertFalse(panel.isMoving());
        assertPositionAndSlots(panel, equipment, 300, 100);
    }

    @Test
    void movingCuriosPanelConsumesClicksWithoutRegrabAndPreservesScroll() {
        AtomicLong clock = new AtomicLong();
        List<Slot> curios = slots(9);
        TerminalEquipmentPanel panel = animatedPanel(curios, () -> true, clock);
        panel.setPosition(new Point(20, 20));
        assertTrue(panel.onMouseWheel(new Point(26, 26), -1));
        panel.updateBeforeRender();
        panel.onMouseDown(new Point(22, 22), 0);
        panel.onMouseDrag(new Point(182, 102), 0);
        panel.onMouseUp(Point.ZERO, 0);
        clock.set(80_000_000L);
        panel.updateBeforeRender();
        assertTrue(panel.onMouseDown(new Point(162, 92), 0));
        assertTrue(panel.onMouseDown(new Point(166, 96), 0));
        assertTrue(panel.onMouseDown(new Point(166, 96), 1));
        assertTrue(panel.onMouseDown(new Point(166, 96), 2));
        assertFalse(panel.onMouseDown(Point.ZERO, 0));
        assertFalse(panel.onMouseDrag(new Point(250, 120), 0));
        assertFalse(panel.onMouseUp(Point.ZERO, 0));
        assertFalse(panel.onMouseWheel(new Point(166, 96), -1));
        assertNull(panel.getTooltip(162, 92));
        panel.updateBeforeRender();
        assertEquals(-10000, curios.getFirst().x);
        assertEquals(166, curios.get(1).x);
        assertEquals(96, curios.get(1).y);
        assertEquals(168, curios.get(5).y);
        assertEquals(-10000, curios.get(6).x);
        clock.set(160_000_000L);
        panel.updateBeforeRender();
        assertFalse(panel.isMoving());
        assertEquals(186, curios.get(1).x);
        assertEquals(106, curios.get(1).y);
        assertFalse(panel.onMouseDown(new Point(186, 106), 0));
        assertTrue(panel.onMouseWheel(new Point(186, 106), -1));
    }

    @Test
    void exclusionStaysAtDragStartWhileSlotsMoveAndCommitsOnReleaseWithoutRender() {
        List<Slot> equipment = slots(5);
        TerminalEquipmentPanel panel = panel(FIVE_SLOT_MODULE, equipment);
        Rect2i screen = new Rect2i(200, 50, 180, 220);
        panel.setPosition(new Point(-80, 20));
        panel.onMouseDown(new Point(-78, 22), 0);

        // The first exclusion query may happen after the panel has already moved.
        for (int x : new int[] {-70, -60, -50}) {
            panel.onMouseDrag(new Point(x + 2, 42), 0);
            panel.updateBeforeRender();
            assertPositionAndSlots(panel, equipment, x, 40);
            assertExclusion(panel, screen, 120, 70, 28, 100);
        }
        panel.onMouseUp(Point.ZERO, 0);
        assertExclusion(panel, screen, 150, 90, 28, 100);
        assertExclusion(panel, screen, 150, 90, 28, 100);

        // A later drag snapshots the new position rather than retaining the previous one.
        panel.onMouseDown(new Point(-48, 42), 0);
        panel.onMouseDrag(new Point(-78, 22), 0);
        assertExclusion(panel, screen, 150, 90, 28, 100);
        panel.onMouseUp(Point.ZERO, 0);
        assertExclusion(panel, screen, 120, 70, 28, 100);
    }

    @Test
    void exclusionCommitsOnlyAfterBothDragAndFinalAnimationFinish() {
        AtomicLong clock = new AtomicLong();
        List<Slot> equipment = slots(5);
        TerminalEquipmentPanel panel = animatedPanel(equipment, () -> true, clock);
        Rect2i screen = new Rect2i(200, 50, 180, 220);
        panel.setPosition(new Point(-80, 20));
        panel.onMouseDown(new Point(-78, 22), 0);
        panel.onMouseDrag(new Point(202, 102), 0);
        clock.set(80_000_000L);
        panel.updateBeforeRender();
        assertPositionAndSlots(panel, equipment, 165, 90);
        assertExclusion(panel, screen, 120, 70, 28, 100);
        panel.onMouseUp(Point.ZERO, 0);
        assertExclusion(panel, screen, 120, 70, 28, 100);
        clock.set(160_000_000L);
        panel.updateBeforeRender();
        assertExclusion(panel, screen, 400, 150, 28, 100);

        panel.onMouseDown(new Point(202, 102), 0);
        panel.onMouseDrag(new Point(-78, 22), 0);
        clock.set(320_000_000L);
        panel.updateBeforeRender();
        assertFalse(panel.isMoving());
        // Finishing animation alone does not reflow bookmarks while the mouse is still held.
        assertExclusion(panel, screen, 400, 150, 28, 100);
        panel.onMouseUp(Point.ZERO, 0);
        assertExclusion(panel, screen, 120, 70, 28, 100);
    }

    @Test
    void resizeAndHideDiscardFrozenExclusionBounds() {
        TerminalEquipmentPanel panel = panel(SCROLLING_MODULE, slots(9));
        Rect2i screen = new Rect2i(200, 50, 180, 220);
        Rect2i viewport = new Rect2i(-200, -50, 600, 300);
        panel.layout(new Point(-80, 20), viewport, bounds -> true);
        panel.onMouseDown(new Point(-78, 22), 0);
        panel.onMouseDrag(new Point(202, 42), 0);
        assertExclusion(panel, screen, 120, 70, 33, 100);
        panel.layout(new Point(-80, 20), new Rect2i(-180, -50, 550, 300), bounds -> true);
        assertExclusion(panel, new Rect2i(180, 50, 180, 220), 380, 90, 33, 100);
        panel.onMouseUp(Point.ZERO, 0);

        panel.onMouseDown(new Point(202, 42), 0);
        panel.onMouseDrag(new Point(-78, 22), 0);
        panel.layout(Point.ZERO, new Rect2i(0, 0, 20, 20), bounds -> false);
        List<Rect2i> hiddenZones = new ArrayList<>();
        panel.addExclusionZones(hiddenZones, screen);
        assertTrue(hiddenZones.isEmpty());
        panel.layout(new Point(-80, 20), viewport, bounds -> true);
        assertExclusion(panel, screen, 120, 70, 33, 100);
    }

    @Test
    void ordinaryClicksDoNotFreezeExclusionsAndInsideScreenNeedsNoExtraZone() {
        TerminalEquipmentPanel panel = panel(SCROLLING_MODULE, slots(9));
        Rect2i screen = new Rect2i(200, 50, 180, 220);
        panel.setPosition(new Point(-80, 20));
        panel.onMouseDown(new Point(-74, 26), 0);
        panel.onMouseDown(new Point(-78, 22), 1);
        panel.onMouseUp(Point.ZERO, 0);
        panel.setPosition(new Point(-60, 40));
        assertExclusion(panel, screen, 140, 90, 33, 100);

        panel.setPosition(new Point(20, 20));
        List<Rect2i> zones = new ArrayList<>();
        panel.addExclusionZones(zones, screen);
        assertTrue(zones.isEmpty());
    }

    private static void assertExclusion(TerminalEquipmentPanel panel, Rect2i screen,
            int x, int y, int width, int height) {
        List<Rect2i> zones = new ArrayList<>();
        panel.addExclusionZones(zones, screen);
        assertEquals(1, zones.size());
        Rect2i zone = zones.getFirst();
        assertEquals(x, zone.getX());
        assertEquals(y, zone.getY());
        assertEquals(width, zone.getWidth());
        assertEquals(height, zone.getHeight());
    }

    private static TerminalEquipmentPanel animatedPanel(List<Slot> slots, BooleanSupplier enabled, AtomicLong clock) {
        MEStorageMenu menu = mock(MEStorageMenu.class);
        when(menu.getSlots(FIVE_SLOT_MODULE)).thenReturn(slots);
        return new TerminalEquipmentPanel(menu, FIVE_SLOT_MODULE, null, enabled, clock::get);
    }

    private static void assertPositionAndSlots(TerminalEquipmentPanel panel, List<Slot> slots, int x, int y) {
        assertEquals(x, panel.getBounds().getX());
        assertEquals(y, panel.getBounds().getY());
        for (int i = 0; i < slots.size(); i++) {
            assertEquals(x + 6, slots.get(i).x);
            assertEquals(y + 6 + i * 18, slots.get(i).y);
        }
    }

    private static List<Slot> slots(int count) {
        return IntStream.range(0, count).mapToObj(i -> {
            Slot slot = mock(Slot.class);
            when(slot.isActive()).thenReturn(true);
            return slot;
        }).toList();
    }

    private static TerminalEquipmentPanel panel(SlotSemantic semantic, List<Slot> slots) {
        MEStorageMenu menu = mock(MEStorageMenu.class);
        when(menu.getSlots(semantic)).thenReturn(slots);
        return new TerminalEquipmentPanel(menu, semantic, null, () -> false, System::nanoTime);
    }
}
