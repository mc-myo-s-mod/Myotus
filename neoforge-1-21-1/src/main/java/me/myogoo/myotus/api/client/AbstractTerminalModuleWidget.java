package me.myogoo.myotus.api.client;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Predicate;

import appeng.client.Point;
import appeng.client.gui.ICompositeWidget;
import me.myogoo.myotus.config.MyotusConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

/**
 * Client-only base for movable terminal modules, with no menu or slots required.
 * Register a fresh instance through {@link me.myogoo.myotus.api.MyotusAPI.Client#registerTerminalModule}.
 * Bounds, mouse positions and content rendering use dialog-relative coordinates, including this widget's x/y.
 * Constructors never call extension hooks.
 *
 * <p>Use the content hooks for managed rendering and input. Vanilla children added through
 * {@link #populateScreen} belong to the whole screen and do not inherit module movement, layering or input
 * occlusion. Ordinary {@link ICompositeWidget} registrations remain supported independently.</p>
 *
 * <p>Position storage accepts an empty list for automatic placement or two integers for x/y.
 * Register addon-owned storage with
 * {@link me.myogoo.myotus.api.MyotusAPI.Client#registerTerminalModulePosition} to include it in position resets.</p>
 *
 * @since 19.2.1-SNAPSHOT (NeoForge 1.21.1)
 */
public abstract class AbstractTerminalModuleWidget implements ICompositeWidget {
    private static final long MOVE_DURATION_NANOS = 160_000_000L;
    private static final int ANIMATED_MOVE_DISTANCE = 36;
    private static final int SNAP_DISTANCE = 6;

    private final ModConfigSpec.ConfigValue<List<Integer>> savedPosition;
    private final BooleanSupplier animateMovement;
    private final BooleanSupplier snapMovement;
    private final LongSupplier nanoTime;
    private int x;
    private int y;
    private int width;
    private int height;
    private Point draggedPosition;
    private Point dragOffset;
    private Point dragStartPosition;
    private Rect2i dragExclusionBounds;
    private boolean movedDuringDrag;
    private boolean blockedDuringDrag;
    private Point animationStart;
    private Point animationTarget;
    private long animationStartedAt;
    private Rect2i movementBounds;
    private List<Rect2i> snapTargets = List.of();
    private Predicate<Rect2i> canPlace = bounds -> true;
    private boolean visible = true;

    protected AbstractTerminalModuleWidget(int width, int height) {
        this(width, height, null);
    }

    protected AbstractTerminalModuleWidget(int width, int height,
            @Nullable ModConfigSpec.ConfigValue<List<Integer>> savedPosition) {
        this(width, height, savedPosition, MyotusConfig.CLIENT.animatePanelMovement::get, System::nanoTime,
                MyotusConfig.CLIENT.snapPanelMovement::get);
    }

    protected AbstractTerminalModuleWidget(int width, int height,
            @Nullable ModConfigSpec.ConfigValue<List<Integer>> savedPosition, BooleanSupplier animateMovement,
            LongSupplier nanoTime, BooleanSupplier snapMovement) {
        checkSize(width, height);
        this.width = width;
        this.height = height;
        this.savedPosition = savedPosition;
        this.animateMovement = Objects.requireNonNull(animateMovement, "animateMovement");
        this.nanoTime = Objects.requireNonNull(nanoTime, "nanoTime");
        this.snapMovement = Objects.requireNonNull(snapMovement, "snapMovement");
    }

    private static void checkSize(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Module dimensions must be positive");
        }
    }

    protected final int getX() {
        return x;
    }

    protected final int getY() {
        return y;
    }

    @Override
    public final void setPosition(Point position) {
        if (draggedPosition != null) {
            position = clamp(draggedPosition);
        }
        settlePosition(position);
        updateBeforeRender();
    }

    // AE2 can lay widgets out again during a drag. Only a different destination cancels the movement.
    private void settlePosition(Point position) {
        if (animationTarget == null || animationTarget.getX() != position.getX()
                || animationTarget.getY() != position.getY()) {
            animationTarget = null;
            x = position.getX();
            y = position.getY();
        }
    }

    public final boolean isMoving() {
        return animationTarget != null;
    }

    private void updateMovement() {
        if (animationTarget == null) {
            return;
        }
        double progress = animateMovement.getAsBoolean()
                ? Math.clamp((nanoTime.getAsLong() - animationStartedAt) / (double) MOVE_DURATION_NANOS, 0, 1)
                : 1;
        double eased = 1 - Math.pow(1 - progress, 3);
        x = (int) Math.round(animationStart.getX() + (animationTarget.getX() - animationStart.getX()) * eased);
        y = (int) Math.round(animationStart.getY() + (animationTarget.getY() - animationStart.getY()) * eased);
        if (progress >= 1) {
            animationTarget = null;
        }
    }

    /** Keep the user's preferred position intact when the viewport needs a temporary fallback. */
    public final void layout(Point defaultPosition, Rect2i movementBounds, Predicate<Rect2i> canPlace) {
        if (this.movementBounds != null && (this.movementBounds.getX() != movementBounds.getX()
                || this.movementBounds.getY() != movementBounds.getY()
                || this.movementBounds.getWidth() != movementBounds.getWidth()
                || this.movementBounds.getHeight() != movementBounds.getHeight())) {
            animationTarget = null;
            dragExclusionBounds = null;
        }
        this.movementBounds = movementBounds;
        this.canPlace = canPlace;
        if (savedPosition != null && dragOffset == null) {
            List<Integer> preferred = savedPosition.get();
            draggedPosition = preferred.isEmpty() ? null : new Point(preferred.get(0), preferred.get(1));
        }
        Point position = clamp(draggedPosition != null ? draggedPosition : defaultPosition);
        if (!canPlace(position)) {
            position = java.util.stream.Stream.of(defaultPosition, new Point(x, y),
                            new Point(movementBounds.getX(), movementBounds.getY()),
                            new Point(movementBounds.getX() + movementBounds.getWidth(), movementBounds.getY()),
                            new Point(movementBounds.getX(), movementBounds.getY() + movementBounds.getHeight()),
                            new Point(movementBounds.getX() + movementBounds.getWidth(),
                                    movementBounds.getY() + movementBounds.getHeight()))
                    .map(this::clamp).filter(this::canPlace).findFirst().orElse(null);
            // Do not overlap the terminal: an impossibly small window must be enlarged to reveal the panel.
            visible = position != null;
            if (!visible) {
                dragOffset = null;
                animationTarget = null;
                updateBeforeRender();
                return;
            }
        }
        visible = true;
        settlePosition(position);
        updateBeforeRender();
    }

    private boolean canPlace(Point position) {
        Rect2i bounds = getBounds();
        return (movementBounds == null || position.getX() >= movementBounds.getX()
                && position.getY() >= movementBounds.getY()
                && position.getX() + bounds.getWidth() <= movementBounds.getX() + movementBounds.getWidth()
                && position.getY() + bounds.getHeight() <= movementBounds.getY() + movementBounds.getHeight())
                && canPlace.test(new Rect2i(position.getX(), position.getY(), bounds.getWidth(), bounds.getHeight()));
    }

    /** Dialog-relative edges, refreshed after all module panels have been laid out. Excludes this panel. */
    public final void setSnapTargets(List<Rect2i> targets) {
        snapTargets = List.copyOf(targets);
    }

    private Point snapPosition(Point raw) {
        if (snapTargets.isEmpty() || !snapMovement.getAsBoolean()) {
            return raw;
        }
        int width = getBounds().getWidth();
        int height = getBounds().getHeight();
        var xs = new LinkedHashSet<Integer>();
        var ys = new LinkedHashSet<Integer>();
        for (Rect2i target : snapTargets) {
            if (target.getWidth() <= 0 || target.getHeight() <= 0) {
                continue;
            }
            int right = target.getX() + target.getWidth();
            int bottom = target.getY() + target.getHeight();
            if (raw.getY() + height + SNAP_DISTANCE >= target.getY() && raw.getY() <= bottom + SNAP_DISTANCE) {
                for (int edge : new int[] {target.getX() - width, right, target.getX(), right - width}) {
                    if (Math.abs(edge - raw.getX()) <= SNAP_DISTANCE) {
                        xs.add(edge);
                    }
                }
            }
            if (raw.getX() + width + SNAP_DISTANCE >= target.getX() && raw.getX() <= right + SNAP_DISTANCE) {
                for (int edge : new int[] {target.getY() - height, bottom, target.getY(), bottom - height}) {
                    if (Math.abs(edge - raw.getY()) <= SNAP_DISTANCE) {
                        ys.add(edge);
                    }
                }
            }
        }
        // An edge that is already aligned must beat a nearby alternative, not count as "unsnapped".
        boolean alignedX = xs.contains(raw.getX());
        boolean alignedY = ys.contains(raw.getY());
        xs.add(raw.getX());
        ys.add(raw.getY());
        Point best = raw;
        int bestAxes = 0;
        int bestDistance = Integer.MAX_VALUE;
        for (int snapX : xs) {
            for (int snapY : ys) {
                int axes = (snapX != raw.getX() || alignedX ? 1 : 0)
                        + (snapY != raw.getY() || alignedY ? 1 : 0);
                int distance = Math.abs(snapX - raw.getX()) + Math.abs(snapY - raw.getY());
                if (axes == 0 || axes < bestAxes || axes == bestAxes && distance >= bestDistance) {
                    continue;
                }
                Point candidate = new Point(snapX, snapY);
                if (!canPlace(candidate) || snapTargets.stream().anyMatch(target -> target.getWidth() > 0
                        && target.getHeight() > 0 && snapX < target.getX() + target.getWidth()
                        && snapX + width > target.getX() && snapY < target.getY() + target.getHeight()
                        && snapY + height > target.getY())) {
                    continue;
                }
                best = candidate;
                bestAxes = axes;
                bestDistance = distance;
            }
        }
        return best;
    }

    @Override
    public final boolean isVisible() {
        return visible;
    }

    private Point clamp(Point position) {
        if (movementBounds == null) {
            return position;
        }
        return new Point(
                Math.clamp(position.getX(), movementBounds.getX(), Math.max(movementBounds.getX(),
                        movementBounds.getX() + movementBounds.getWidth() - getBounds().getWidth())),
                Math.clamp(position.getY(), movementBounds.getY(), Math.max(movementBounds.getY(),
                        movementBounds.getY() + movementBounds.getHeight() - getBounds().getHeight())));
    }

    @Override
    public void setSize(int width, int height) {
        checkSize(width, height);
        this.width = width;
        this.height = height;
    }

    @Override
    public Rect2i getBounds() {
        return new Rect2i(x, y, width, height);
    }

    @Override
    public final void addExclusionZones(List<Rect2i> exclusionZones, Rect2i screenBounds) {
        if (!visible) {
            return;
        }
        // Moving exclusion edges make recipe viewers repeatedly reflow their bookmark grid.
        // Keep its layout stable until both the drag and its final animation have finished.
        Rect2i bounds = dragExclusionBounds != null && (dragOffset != null || isMoving())
                ? dragExclusionBounds : getBounds();
        if (bounds.getX() < 0 || bounds.getY() < 0
                || bounds.getX() + bounds.getWidth() > screenBounds.getWidth()
                || bounds.getY() + bounds.getHeight() > screenBounds.getHeight()) {
            exclusionZones.add(new Rect2i(screenBounds.getX() + bounds.getX(), screenBounds.getY() + bounds.getY(),
                    bounds.getWidth(), bounds.getHeight()));
        }
    }

    /** Whether this module owns a native menu slot; slotless modules leave this unchanged. */
    public boolean ownsSlot(Slot slot) {
        return false;
    }

    @Override
    public final void updateBeforeRender() {
        updateMovement();
        if (dragOffset == null && !isMoving()) {
            dragExclusionBounds = null;
        }
        updateContent();
    }

    /** Update content after movement, including when hidden so native slots can be hidden too. */
    protected void updateContent() {
    }

    /** Height of the top-edge drag handle, in pixels; zero disables dragging from the content. */
    protected int dragHandleHeight() {
        return 5;
    }

    protected void drawPanelBackground(GuiGraphics graphics) {
    }

    /**
     * Draw this module in its assigned layer. The pose is translated to the dialog origin and this module's
     * Z layer, not to its x/y. Content must stay within its bounds and Z offsets [0, layerHeight).
     * Slotless implementations ignore the native slot callbacks.
     */
    public final void render(GuiGraphics graphics, Point mouse, float layerHeight,
            Consumer<Slot> renderSlot, Consumer<Slot> renderHighlight) {
        if (!visible) return;
        drawPanelBackground(graphics);
        renderContent(graphics, mouse, layerHeight, renderSlot, renderHighlight);
    }

    /** Render content within the coordinate and depth contract of {@link #render}. */
    protected void renderContent(GuiGraphics graphics, Point mouse, float layerHeight,
            Consumer<Slot> renderSlot, Consumer<Slot> renderHighlight) {
    }

    /** Return false only to let the screen handle native slots or other native screen input. */
    protected boolean onContentMouseDown(Point mouse, int button) {
        return true;
    }

    /** Also called when capture is cancelled or the module is hidden, to release content gestures. */
    protected boolean onContentMouseUp(Point mouse, int button) {
        return false;
    }

    protected boolean onContentMouseDrag(Point mouse, int button) {
        return false;
    }

    protected boolean onContentMouseWheel(Point mouse, double delta) {
        return false;
    }

    @Override
    public final boolean onMouseDown(Point mouse, int button) {
        if (!visible || !mouse.isIn(getBounds())) {
            return false;
        }
        // A travelling panel may briefly pass over native slots. Do not interact until it arrives.
        if (isMoving()) {
            return true;
        }
        if (button == 0 && mouse.getY() < y + dragHandleHeight()) {
            dragOffset = new Point(mouse.getX() - x, mouse.getY() - y);
            dragStartPosition = new Point(x, y);
            dragExclusionBounds = getBounds();
            movedDuringDrag = false;
            blockedDuringDrag = false;
            return true;
        }
        return onContentMouseDown(mouse, button);
    }

    @Override
    public final boolean wantsAllMouseUpEvents() {
        return true;
    }

    @Override
    public final boolean onMouseUp(Point mouse, int button) {
        boolean wasDragging = button == 0 && dragOffset != null;
        if (wasDragging && movedDuringDrag && savedPosition != null
                && (draggedPosition.getX() != dragStartPosition.getX()
                || draggedPosition.getY() != dragStartPosition.getY())) {
            List<Integer> position = List.of(draggedPosition.getX(), draggedPosition.getY());
            if (!position.equals(savedPosition.get())) {
                savedPosition.set(position);
                savedPosition.save();
            }
        }
        if (button == 0) {
            dragOffset = null;
        }
        boolean handled = onContentMouseUp(mouse, button);
        return wasDragging || handled;
    }

    @Override
    public final boolean onMouseDrag(Point mouse, int button) {
        if (button == 0 && dragOffset != null) {
            Point position = snapPosition(clamp(
                    new Point(mouse.getX() - dragOffset.getX(), mouse.getY() - dragOffset.getY())));
            if (canPlace(position)) {
                updateMovement();
                boolean changedPosition = position.getX() != x || position.getY() != y;
                movedDuringDrag |= changedPosition;
                draggedPosition = position;
                if (changedPosition && animateMovement.getAsBoolean() && (isMoving() || blockedDuringDrag
                        || Math.abs(position.getX() - x) > ANIMATED_MOVE_DISTANCE
                        || Math.abs(position.getY() - y) > ANIMATED_MOVE_DISTANCE)) {
                    if (animationTarget == null || animationTarget.getX() != position.getX()
                            || animationTarget.getY() != position.getY()) {
                        animationStart = new Point(x, y);
                        animationTarget = position;
                        animationStartedAt = nanoTime.getAsLong();
                    }
                } else {
                    animationTarget = null;
                    x = position.getX();
                    y = position.getY();
                }
                blockedDuringDrag = false;
                updateBeforeRender();
            } else {
                blockedDuringDrag = true;
            }
            return true;
        }
        return visible && !isMoving() && onContentMouseDrag(mouse, button);
    }

    @Override
    public final boolean onMouseWheel(Point mouse, double delta) {
        return visible && !isMoving() && mouse.isIn(getBounds()) && onContentMouseWheel(mouse, delta);
    }
}
