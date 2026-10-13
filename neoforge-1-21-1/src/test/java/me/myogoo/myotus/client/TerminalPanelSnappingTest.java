package me.myogoo.myotus.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.stream.IntStream;

import appeng.client.Point;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TerminalPanelSnappingTest {
    private static final SlotSemantic MODULE = SlotSemantics.register("myotus_test_snapping_slots", false);

    @Test
    void exactAlignmentDoesNotJumpBetweenUnequalPanelEdges() {
        TerminalEquipmentPanel panel = panel(5, () -> true);
        panel.setSnapTargets(List.of(new Rect2i(100, 100, 33, 100)));
        panel.onMouseDown(new Point(2, 2), 0);
        for (int x : new int[] {101, 100, 99, 100, 101}) {
            panel.onMouseDrag(new Point(x + 2, 2), 0);
            assertPosition(panel, 100, 0);
        }
    }

    @Test
    void snapsFlushOnAllFourSidesAtInclusiveSixPixelThreshold() {
        for (int[] position : new int[][] {
                {66, 120, 72, 120}, {166, 120, 160, 120},
                {110, -6, 110, 0}, {110, 246, 110, 240}
        }) {
            TerminalEquipmentPanel panel = panel(5, () -> true);
            panel.setSnapTargets(List.of(new Rect2i(100, 100, 60, 140)));
            panel.onMouseDown(new Point(2, 2), 0);
            panel.onMouseDrag(new Point(position[0] + 2, position[1] + 2), 0);
            assertPosition(panel, position[2], position[3]);
        }
    }

    @Test
    void alignsTopBottomLeftAndRightEdgesWhileBesideAnotherPanel() {
        for (int[] position : new int[][] {
                {70, 104, 72, 100}, {70, 144, 72, 140},
                {104, -3, 100, 0}, {114, -3, 112, 0}
        }) {
            TerminalEquipmentPanel panel = panel(5, () -> true);
            panel.setSnapTargets(List.of(new Rect2i(100, 100, 40, 140)));
            panel.onMouseDown(new Point(2, 2), 0);
            panel.onMouseDrag(new Point(position[0] + 2, position[1] + 2), 0);
            assertPosition(panel, position[2], position[3]);
        }
    }

    @Test
    void ignoresTargetsBeyondThresholdOrFarAwayOnOtherAxis() {
        for (Point raw : List.of(new Point(65, 120), new Point(167, 120), new Point(66, 400),
                new Point(400, -6))) {
            TerminalEquipmentPanel panel = panel(5, () -> true);
            panel.setSnapTargets(List.of(new Rect2i(100, 100, 60, 140)));
            panel.onMouseDown(new Point(2, 2), 0);
            panel.onMouseDrag(new Point(raw.getX() + 2, raw.getY() + 2), 0);
            assertPosition(panel, raw.getX(), raw.getY());
        }
    }

    @Test
    void accountsForCuriosScrollbarAndEmptyPanelHeight() {
        TerminalEquipmentPanel curios = panel(9, () -> true);
        curios.setSnapTargets(List.of(new Rect2i(100, 100, 28, 100)));
        curios.onMouseDown(new Point(2, 2), 0);
        curios.onMouseDrag(new Point(65, 106), 0);
        assertPosition(curios, 67, 100);
        assertEquals(33, curios.getBounds().getWidth());

        TerminalEquipmentPanel empty = panel(0, () -> true);
        empty.setSnapTargets(List.of(new Rect2i(100, 100, 28, 100)));
        empty.onMouseDown(new Point(2, 2), 0);
        empty.onMouseDrag(new Point(106, 70), 0);
        assertPosition(empty, 100, 72);
    }

    @Test
    void blockedCornerStillAllowsValidSingleAxisSnapWithoutClampingIt() {
        TerminalEquipmentPanel panel = panel(5, () -> true);
        panel.layout(new Point(0, 120), new Rect2i(-300, 104, 1000, 500), bounds -> true);
        panel.setSnapTargets(List.of(new Rect2i(100, 100, 60, 140)));
        panel.onMouseDown(new Point(2, 122), 0);
        panel.onMouseDrag(new Point(68, 106), 0);
        assertPosition(panel, 72, 104);
    }

    @Test
    void nativeObstacleBlocksTerminalSnapButItsOuterEdgeCanBeSnappedTo() {
        TerminalEquipmentPanel panel = panel(5, () -> true);
        Rect2i toolbar = new Rect2i(70, 100, 30, 140);
        panel.layout(Point.ZERO, new Rect2i(-300, -300, 1000, 1000), bounds ->
                !(bounds.getX() < 100 && bounds.getX() + bounds.getWidth() > 70
                        && bounds.getY() < 240 && bounds.getY() + bounds.getHeight() > 100));
        panel.setSnapTargets(List.of(new Rect2i(100, 100, 60, 140), toolbar));
        panel.onMouseDown(new Point(2, 2), 0);
        panel.onMouseDrag(new Point(68, 122), 0);
        assertPosition(panel, 0, 0);
        panel.onMouseDrag(new Point(40, 122), 0);
        assertPosition(panel, 42, 120);
    }

    @Test
    void snappingNeverStacksModulesButManualOverlapStillWorks() {
        TerminalEquipmentPanel panel = panel(5, () -> true);
        panel.setSnapTargets(List.of(new Rect2i(100, 100, 28, 100)));
        panel.onMouseDown(new Point(2, 2), 0);
        panel.onMouseDrag(new Point(104, 104), 0);
        assertPosition(panel, 102, 102);
    }

    @Test
    void toggleAffectsNextDragOnlyAndClearedTargetsStopSnapping() {
        AtomicBoolean enabled = new AtomicBoolean(true);
        TerminalEquipmentPanel panel = panel(5, enabled::get);
        panel.setSnapTargets(List.of(new Rect2i(100, 100, 60, 140)));
        panel.onMouseDown(new Point(2, 2), 0);
        panel.onMouseDrag(new Point(68, 122), 0);
        assertPosition(panel, 72, 120);
        enabled.set(false);
        panel.updateBeforeRender();
        assertPosition(panel, 72, 120);
        panel.onMouseDrag(new Point(68, 122), 0);
        assertPosition(panel, 66, 120);
        enabled.set(true);
        panel.updateBeforeRender();
        assertPosition(panel, 66, 120);
        panel.onMouseDrag(new Point(68, 122), 0);
        assertPosition(panel, 72, 120);
        panel.setSnapTargets(List.of());
        panel.onMouseDrag(new Point(68, 122), 0);
        assertPosition(panel, 66, 120);
    }

    @Test
    @SuppressWarnings("unchecked")
    void snappedTargetIsSavedAndExclusionStaysFrozenUntilAnimationFinishes() {
        AtomicLong clock = new AtomicLong();
        var saved = new AtomicReference<List<Integer>>(List.of());
        ModConfigSpec.ConfigValue<List<Integer>> setting = mock(ModConfigSpec.ConfigValue.class);
        when(setting.get()).thenAnswer(invocation -> saved.get());
        doAnswer(invocation -> {
            saved.set(invocation.getArgument(0));
            return null;
        }).when(setting).set(anyList());
        MEStorageMenu menu = menu(5);
        TerminalEquipmentPanel panel = new TerminalEquipmentPanel(menu, MODULE, setting,
                () -> true, clock::get, () -> true);
        panel.setPosition(new Point(-100, 20));
        panel.setSnapTargets(List.of(new Rect2i(100, 20, 60, 140)));
        panel.onMouseDown(new Point(-98, 22), 0);
        panel.onMouseDrag(new Point(68, 26), 0);
        assertTrue(panel.isMoving());
        assertPosition(panel, -100, 20);
        panel.onMouseUp(Point.ZERO, 0);
        assertEquals(List.of(72, 20), saved.get());
        verify(setting, times(1)).save();

        Rect2i screen = new Rect2i(200, 50, 60, 200);
        List<Rect2i> zones = new ArrayList<>();
        clock.set(80_000_000L);
        panel.updateBeforeRender();
        panel.addExclusionZones(zones, screen);
        assertEquals(100, zones.getFirst().getX());
        clock.set(160_000_000L);
        panel.updateBeforeRender();
        assertPosition(panel, 72, 20);
        assertFalse(panel.isMoving());
        assertEquals(78, menu.getSlots(MODULE).getFirst().x);
        zones.clear();
        panel.addExclusionZones(zones, screen);
        assertEquals(272, zones.getFirst().getX());

        TerminalEquipmentPanel reopened = new TerminalEquipmentPanel(menu, MODULE,
                setting, () -> false, clock::get, () -> true);
        reopened.layout(Point.ZERO, new Rect2i(-300, -300, 1000, 1000), bounds -> true);
        assertPosition(reopened, 72, 20);
    }

    private static TerminalEquipmentPanel panel(int slotCount, BooleanSupplier snapping) {
        return new TerminalEquipmentPanel(menu(slotCount), MODULE, null,
                () -> false, System::nanoTime, snapping);
    }

    private static MEStorageMenu menu(int count) {
        MEStorageMenu menu = mock(MEStorageMenu.class);
        List<Slot> slots = IntStream.range(0, count).mapToObj(i -> {
            Slot slot = mock(Slot.class);
            when(slot.isActive()).thenReturn(true);
            return slot;
        }).toList();
        when(menu.getSlots(MODULE)).thenReturn(slots);
        return menu;
    }

    private static void assertPosition(TerminalEquipmentPanel panel, int x, int y) {
        assertEquals(x, panel.getBounds().getX());
        assertEquals(y, panel.getBounds().getY());
    }
}
