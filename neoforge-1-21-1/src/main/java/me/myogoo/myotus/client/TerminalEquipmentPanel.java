package me.myogoo.myotus.client;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

import appeng.client.Point;
import appeng.client.gui.Tooltip;
import appeng.client.gui.widgets.Scrollbar;
import appeng.menu.SlotSemantic;
import appeng.menu.me.common.MEStorageMenu;
import me.myogoo.myotus.api.client.AbstractTerminalModuleWidget;
import me.myogoo.myotus.config.MyotusConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Client-only native-slot panel with shared movement, input and layering.
 * Addons may override the protected layout hooks and {@link #getBounds()} for other slot layouts.
 * Hooks run after construction, in terminal-relative coordinates; slots keep their menu registration order.
 * Available as an extension API in NeoForge 1.21.1 since 19.2.1-SNAPSHOT.
 */
public class TerminalEquipmentPanel extends AbstractTerminalModuleWidget {
    private static final int SLOT_SIZE = 18;
    private static final int PADDING = 5;
    private static final int VISIBLE_ROWS = 5;
    private static final int SCROLLBAR_WIDTH = 7;
    private static final int SCROLLBAR_INSET = 2;
    private static final int COLUMN_WIDTH = SLOT_SIZE + PADDING * 2;

    private final List<Slot> slots;
    private final Scrollbar scrollbar = new Scrollbar(Scrollbar.SMALL);

    public TerminalEquipmentPanel(MEStorageMenu menu, SlotSemantic semantic) {
        this(menu, semantic, null);
    }

    public TerminalEquipmentPanel(MEStorageMenu menu, SlotSemantic semantic,
            ModConfigSpec.ConfigValue<List<Integer>> savedPosition) {
        this(menu, semantic, savedPosition, MyotusConfig.CLIENT.animatePanelMovement::get, System::nanoTime,
                MyotusConfig.CLIENT.snapPanelMovement::get);
    }

    TerminalEquipmentPanel(MEStorageMenu menu, SlotSemantic semantic,
            ModConfigSpec.ConfigValue<List<Integer>> savedPosition, BooleanSupplier animateMovement,
            LongSupplier nanoTime) {
        this(menu, semantic, savedPosition, animateMovement, nanoTime, () -> false);
    }

    TerminalEquipmentPanel(MEStorageMenu menu, SlotSemantic semantic,
            ModConfigSpec.ConfigValue<List<Integer>> savedPosition, BooleanSupplier animateMovement,
            LongSupplier nanoTime, BooleanSupplier snapMovement) {
        super(COLUMN_WIDTH, PADDING * 2 + VISIBLE_ROWS * SLOT_SIZE, savedPosition, animateMovement,
                nanoTime, snapMovement);
        slots = List.copyOf(menu.getSlots(semantic));
        scrollbar.setCaptureMouseWheel(false);
    }

    @Override
    public void setSize(int width, int height) {
    }

    @Override
    public Rect2i getBounds() {
        return new Rect2i(getX(), getY(), COLUMN_WIDTH + (scrolling() ? SCROLLBAR_WIDTH - SCROLLBAR_INSET : 0),
                PADDING * 2 + rows() * SLOT_SIZE);
    }

    @Override
    public final boolean ownsSlot(Slot slot) {
        return slots.contains(slot);
    }

    @Override
    protected void updateContent() {
        scrollbar.setRange(0, Math.max(0, slots.size() - VISIBLE_ROWS), 1);
        scrollbar.setHeight(rows() * SLOT_SIZE - 2);
        scrollbar.setPosition(new Point(getX() + COLUMN_WIDTH - SCROLLBAR_INSET, getY() + PADDING + 1));
        for (Slot slot : slots) {
            slot.x = slot.y = -10000;
        }
        if (isVisible()) {
            updateSlotPositions();
            for (Slot slot : slots) {
                if (!slot.isActive()) slot.x = slot.y = -10000;
            }
        }
    }

    /** Immutable list of native slots; their coordinates may be changed by the layout hook. */
    protected final List<Slot> getSlots() {
        return slots;
    }

    /** Position visible slots. Hidden panels and inactive slots are handled by the shared update. */
    protected void updateSlotPositions() {
        for (int i = 0; i < slots.size(); i++) {
            Slot slot = slots.get(i);
            int row = i - scrollbar.getCurrentScroll();
            if (row >= 0 && row < VISIBLE_ROWS) {
                slot.x = getX() + PADDING + 1;
                slot.y = getY() + PADDING + row * SLOT_SIZE + 1;
            }
        }
    }

    /** Slots to render in this panel's layer, including item decorations and highlights. */
    protected List<Slot> visibleSlots() {
        int first = scrollbar.getCurrentScroll();
        return slots.subList(first, Math.min(slots.size(), first + VISIBLE_ROWS));
    }

    @Override
    protected int dragHandleHeight() {
        return PADDING;
    }

    @Override
    protected void drawPanelBackground(GuiGraphics graphics) {
        int left = getX();
        int top = getY();
        int middle = left + PADDING;
        int right = middle + SLOT_SIZE;
        TerminalUpgradePanel.MyoSlotBlitter.topLeft(graphics, left, top, true);
        TerminalUpgradePanel.MyoSlotBlitter.topMiddle(graphics, middle, top);
        TerminalUpgradePanel.MyoSlotBlitter.topRight(graphics, right, top);
        graphics.fill(left + 9, top + 2, left + COLUMN_WIDTH - 9, top + 3, 0xff777787);
        graphics.fill(left + 9, top + 3, left + COLUMN_WIDTH - 9, top + 4, 0xffeeeeff);
        for (int row = 0; row < rows(); row++) {
            int rowY = top + PADDING + row * SLOT_SIZE;
            TerminalUpgradePanel.MyoSlotBlitter.leftMiddle(graphics, left, rowY, true);
            TerminalUpgradePanel.MyoSlotBlitter.equipmentSlot(graphics, middle, rowY);
            TerminalUpgradePanel.MyoSlotBlitter.rightMiddle(graphics, right, rowY);
        }
        int bottom = top + PADDING + rows() * SLOT_SIZE;
        TerminalUpgradePanel.MyoSlotBlitter.bottomLeft(graphics, left, bottom, true);
        TerminalUpgradePanel.MyoSlotBlitter.bottomMiddle(graphics, middle, bottom);
        TerminalUpgradePanel.MyoSlotBlitter.bottomRight(graphics, right, bottom);
        if (scrolling()) {
            int trackX = left + COLUMN_WIDTH - SCROLLBAR_INSET;
            int trackY = top + PADDING + 1;
            graphics.fill(trackX, trackY, trackX + SCROLLBAR_WIDTH,
                    trackY + rows() * SLOT_SIZE - 2, 0xff363636);
        }
    }

    @Override
    protected void renderContent(GuiGraphics graphics, Point mouse, float layerHeight,
            Consumer<Slot> renderSlot, Consumer<Slot> renderHighlight) {
        var visibleSlots = visibleSlots();
        graphics.pose().pushPose();
        try {
            // Native items/decorations add up to 300 Z; keep them below the next panel.
            graphics.pose().scale(1, 1, layerHeight / 1000);
            for (Slot slot : visibleSlots) {
                if (slot.isActive()) {
                    renderSlot.accept(slot);
                }
            }
            graphics.flush();
        } finally {
            graphics.pose().popPose();
        }
        graphics.pose().pushPose();
        try {
            graphics.pose().translate(0, 0, layerHeight * 0.4f);
            for (Slot slot : visibleSlots) {
                if (slot.isActive()) {
                    renderHighlight.accept(slot);
                }
            }
            if (scrolling()) {
                scrollbar.drawForegroundLayer(graphics, getBounds(), mouse);
            }
            graphics.flush();
        } finally {
            graphics.pose().popPose();
        }
    }

    @Override
    public void tick() {
        if (scrolling()) {
            scrollbar.tick();
        }
    }

    @Override
    protected boolean onContentMouseDown(Point mouse, int button) {
        if (scrolling() && mouse.getX() >= getX() + COLUMN_WIDTH - SCROLLBAR_INSET
                && mouse.getY() >= getY() + PADDING + 1
                && mouse.getY() < getY() + PADDING + rows() * SLOT_SIZE - 1) {
            scrollbar.onMouseDown(mouse, button);
            return true;
        }
        for (Slot slot : slots) {
            if (slot.isActive() && mouse.getX() >= slot.x && mouse.getX() < slot.x + 16
                    && mouse.getY() >= slot.y && mouse.getY() < slot.y + 16) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean onContentMouseUp(Point mouse, int button) {
        scrollbar.onMouseUp(mouse, button);
        return false;
    }

    @Override
    protected boolean onContentMouseDrag(Point mouse, int button) {
        return scrolling() && scrollbar.onMouseDrag(mouse, button);
    }

    @Override
    protected boolean onContentMouseWheel(Point mouse, double delta) {
        return scrolling() && scrollbar.onMouseWheel(mouse, delta);
    }

    @Override
    public Tooltip getTooltip(int mouseX, int mouseY) {
        if (isMoving() || mouseY < getY() + dragHandleHeight()) {
            return null;
        }
        return slots.isEmpty() && new Point(mouseX, mouseY).isIn(getBounds())
                ? new Tooltip(Component.translatable("gui.myotus.terminal_module.no_slots")) : null;
    }

    private int rows() {
        return Math.max(1, Math.min(VISIBLE_ROWS, slots.size()));
    }

    protected boolean scrolling() {
        return slots.size() > VISIBLE_ROWS;
    }
}
