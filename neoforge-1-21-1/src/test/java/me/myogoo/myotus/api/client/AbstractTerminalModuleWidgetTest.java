package me.myogoo.myotus.api.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import appeng.client.Point;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AbstractTerminalModuleWidgetTest {
    @Test
    void constructionDoesNotDispatchSubclassHooksAndDragHandleSkipsContentInput() {
        var widget = new Probe(40, 30, null, false, new AtomicLong());

        assertEquals(0, widget.updates.get());
        assertThrows(IllegalArgumentException.class, () -> widget.setSize(0, 30));
        assertThrows(IllegalArgumentException.class, () -> new Probe(40, -1, null, false, new AtomicLong()));
        widget.setPosition(new Point(10, 20));
        assertEquals(1, widget.updates.get());

        assertTrue(widget.onMouseDown(new Point(20, 35), 0));
        assertEquals(1, widget.contentDowns.get());
        assertTrue(widget.onMouseDown(new Point(20, 25), 0));
        assertEquals(1, widget.contentDowns.get());
    }

    @Test
    void hiddenAndMovingWidgetsDoNotDispatchContentInput() {
        var hidden = new Probe(40, 30, null, true, new AtomicLong());
        hidden.layout(Point.ZERO, new Rect2i(0, 0, 10, 10), bounds -> false);
        assertFalse(hidden.isVisible());
        assertFalse(hidden.onMouseDown(Point.ZERO, 0));
        assertFalse(hidden.onMouseWheel(Point.ZERO, 1));
        assertFalse(hidden.onMouseDrag(Point.ZERO, 0));
        assertEquals(0, hidden.contentDowns.get());

        var widget = new Probe(40, 30, null, true, new AtomicLong());
        widget.layout(Point.ZERO, new Rect2i(0, 0, 300, 100), bounds -> true);
        assertTrue(widget.onMouseDown(new Point(2, 2), 0));
        assertTrue(widget.onMouseDrag(new Point(100, 2), 0));
        assertTrue(widget.isMoving());
        widget.onMouseUp(Point.ZERO, 0);
        assertTrue(widget.onMouseDown(new Point(20, 15), 0));
        assertFalse(widget.onMouseWheel(new Point(20, 15), 1));
        assertFalse(widget.onMouseDrag(new Point(20, 15), 0));
        assertEquals(0, widget.contentDowns.get());
    }

    @Test
    void layoutMovementAndResizeDoNotPersistTemporaryPositions() {
        var savedPosition = mockConfigValue(List.of(350, 80));
        var widget = new Probe(40, 30, savedPosition, false, new AtomicLong());

        widget.layout(Point.ZERO, new Rect2i(0, 0, 400, 120), bounds -> true);
        assertEquals(350, widget.getBounds().getX());
        widget.setSize(100, 80);
        widget.layout(Point.ZERO, new Rect2i(0, 0, 400, 120), bounds -> true);
        assertEquals(300, widget.getBounds().getX());
        widget.setPosition(new Point(100, 20));

        verify(savedPosition, never()).set(org.mockito.ArgumentMatchers.anyList());
        verify(savedPosition, never()).save();
    }

    @Test
    void exclusionBoundsStayAtDragStartThroughMovementAnimation() {
        var clock = new AtomicLong();
        var widget = new Probe(40, 30, null, true, clock);
        widget.layout(new Point(-1, 20), new Rect2i(-20, 0, 300, 100), bounds -> true);
        widget.onMouseDown(new Point(4, 22), 0);
        widget.onMouseDrag(new Point(100, 22), 0);
        widget.onMouseUp(Point.ZERO, 0);
        assertTrue(widget.isMoving());

        List<Rect2i> zones = new ArrayList<>();
        widget.addExclusionZones(zones, new Rect2i(100, 50, 200, 100));
        assertEquals(1, zones.size());
        var zone = zones.getFirst();
        assertEquals(List.of(99, 70, 40, 30),
                List.of(zone.getX(), zone.getY(), zone.getWidth(), zone.getHeight()));

        clock.set(160_000_000L);
        widget.updateBeforeRender();
        zones.clear();
        widget.addExclusionZones(zones, new Rect2i(100, 50, 200, 100));
        assertTrue(zones.isEmpty());
    }

    @Test
    void slotlessWidgetDoesNotClaimNativeSlots() {
        var widget = new Probe(40, 30, null, false, new AtomicLong());
        assertFalse(widget.ownsSlot(mock(Slot.class)));
    }

    @SuppressWarnings("unchecked")
    private static ModConfigSpec.ConfigValue<List<Integer>> mockConfigValue(List<Integer> value) {
        var configValue = mock(ModConfigSpec.ConfigValue.class);
        when(configValue.get()).thenReturn(value);
        return configValue;
    }

    private static final class Probe extends AbstractTerminalModuleWidget {
        private final AtomicInteger updates = new AtomicInteger();
        private final AtomicInteger contentDowns = new AtomicInteger();
        private boolean initialized = true;

        private Probe(int width, int height, ModConfigSpec.ConfigValue<List<Integer>> savedPosition,
                boolean animate, AtomicLong clock) {
            super(width, height, savedPosition, () -> animate, clock::get, () -> false);
        }

        @Override
        protected void updateContent() {
            assertTrue(initialized, "subclass hooks must not run during base construction");
            updates.incrementAndGet();
        }

        @Override
        protected int dragHandleHeight() {
            return 10;
        }

        @Override
        protected boolean onContentMouseDown(Point mouse, int button) {
            contentDowns.incrementAndGet();
            return true;
        }

        @Override
        protected boolean onContentMouseDrag(Point mouse, int button) {
            return true;
        }

        @Override
        protected boolean onContentMouseWheel(Point mouse, double delta) {
            return true;
        }
    }
}
