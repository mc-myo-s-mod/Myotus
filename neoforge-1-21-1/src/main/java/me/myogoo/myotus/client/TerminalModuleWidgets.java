package me.myogoo.myotus.client;

import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.AESubScreen;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.style.WidgetStyle;
import me.myogoo.myotus.api.ITerminalCard;
import me.myogoo.myotus.api.client.AbstractTerminalModuleWidget;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

/** Client-only registration and per-screen lifetime of installed terminal module widgets. */
public final class TerminalModuleWidgets {
    private static final Map<Item, Registration> FACTORIES = new LinkedHashMap<>();

    private final Map<Item, ActiveWidget> activeWidgets = new LinkedHashMap<>();
    private final List<AbstractTerminalModuleWidget> panels = new ArrayList<>();
    private AbstractTerminalModuleWidget capturedPanel;
    private Item lastClickedModuleCard;

    private record Registration(String id, BiFunction<AEBaseScreen<?>, ItemStack, ICompositeWidget> factory) {}

    private record ActiveWidget(String id, WidgetStyle style, ICompositeWidget widget) {}

    /** Internal link from an AE2 container to its owning screen's module selection. */
    public interface ContainerAccess {
        void myotus$setModuleWidgets(TerminalModuleWidgets modules);
    }

    public void recordDispatchedClick(ICompositeWidget widget, Point mouse) {
        if (!widget.isVisible() || !mouse.isIn(widget.getBounds())) {
            return;
        }
        for (var entry : activeWidgets.entrySet()) {
            if (entry.getValue().widget() == widget) {
                lastClickedModuleCard = entry.getKey();
                return;
            }
        }
    }

    /** Selection belongs to this screen and is discarded when the selected card is removed. */
    public @Nullable Item getLastClickedModuleCard() {
        return lastClickedModuleCard;
    }

    /** Managed modules in bottom-to-top order, independent of AE2's widget and slot ordering. */
    public List<AbstractTerminalModuleWidget> getModuleWidgets() {
        return List.copyOf(panels);
    }

    /** Native-slot panels only. Rendering and input should use {@link #getModuleWidgets()}. */
    public List<TerminalEquipmentPanel> getEquipmentPanels() {
        return panels.stream().filter(TerminalEquipmentPanel.class::isInstance)
                .map(TerminalEquipmentPanel.class::cast).toList();
    }

    /** Returns a slot panel only when it is the topmost module at the given point. */
    public TerminalEquipmentPanel getPanelAt(Point mouse) {
        return getModuleAt(mouse) instanceof TerminalEquipmentPanel panel ? panel : null;
    }

    public AbstractTerminalModuleWidget getModuleAt(Point mouse) {
        for (int i = panels.size() - 1; i >= 0; i--) {
            AbstractTerminalModuleWidget panel = panels.get(i);
            if (panel.isVisible() && mouse.isIn(panel.getBounds())) {
                return panel;
            }
        }
        return null;
    }

    public boolean ownsSlot(Slot slot) {
        return panels.stream().anyMatch(panel -> panel.ownsSlot(slot));
    }

    public boolean isSlotAccessible(Slot slot, Point mouse) {
        AbstractTerminalModuleWidget panel = getModuleAt(mouse);
        return panel != null ? !panel.isMoving() && panel.ownsSlot(slot) : !ownsSlot(slot);
    }

    /** Null delegates to AE2's other widgets; false bypasses them for the front panel's native slots. */
    public Boolean onMouseDown(Point mouse, int button) {
        releaseInvalidCapture();
        AbstractTerminalModuleWidget panel = getModuleAt(mouse);
        if (panel == null) {
            return null;
        }
        for (var entry : activeWidgets.entrySet()) {
            if (entry.getValue().widget() == panel) {
                lastClickedModuleCard = entry.getKey();
                break;
            }
        }
        panels.remove(panel);
        panels.add(panel);
        boolean handled = panel.onMouseDown(mouse, button);
        if (button == 0 && handled) {
            capturedPanel = panel;
        }
        return handled;
    }

    public Boolean onMouseDrag(Point mouse, int button) {
        releaseInvalidCapture();
        if (button != 0 || capturedPanel == null) {
            return null;
        }
        capturedPanel.onMouseDrag(mouse, button);
        return true;
    }

    public Boolean onMouseUp(Point mouse, int button) {
        releaseInvalidCapture();
        if (button != 0 || capturedPanel == null) {
            return null;
        }
        capturedPanel.onMouseUp(mouse, button);
        capturedPanel = null;
        return true;
    }

    public Boolean onMouseWheel(Point mouse, double delta) {
        AbstractTerminalModuleWidget panel = getModuleAt(mouse);
        if (panel == null) {
            return null;
        }
        panel.onMouseWheel(mouse, delta);
        return true;
    }

    private void releaseInvalidCapture() {
        if (capturedPanel != null && (!panels.contains(capturedPanel) || !capturedPanel.isVisible())) {
            capturedPanel.onMouseUp(Point.ZERO, 0);
            capturedPanel = null;
        }
    }

    public static void register(Item card, BiFunction<AEBaseScreen<?>, ItemStack, ICompositeWidget> factory) {
        Objects.requireNonNull(card, "card");
        Objects.requireNonNull(factory, "factory");
        if (!(card instanceof ITerminalCard)) {
            throw new IllegalArgumentException("Terminal widgets require an ITerminalCard item");
        }
        if (FACTORIES.containsKey(card)) {
            throw new IllegalArgumentException("A terminal module is already registered for this card");
        }
        FACTORIES.put(card, new Registration("myotus_terminal_module_" + FACTORIES.size(), factory));
    }

    /** Returns whether screen children must be repopulated after changing the composite widgets. */
    public boolean update(AEBaseScreen<?> screen, Iterable<ItemStack> installed,
            Map<String, ICompositeWidget> widgets, Map<String, WidgetStyle> styles) {
        // Settings reuse the terminal menu, but must not recreate panels or reposition its hidden slots.
        if (screen instanceof AESubScreen<?, ?>) {
            return false;
        }
        Map<Item, ItemStack> present = new LinkedHashMap<>();
        for (ItemStack stack : installed) {
            if (!stack.isEmpty() && stack.getItem() instanceof ITerminalCard card && card.isAvailable()
                    && FACTORIES.containsKey(stack.getItem())) {
                present.putIfAbsent(stack.getItem(), stack);
            }
        }

        boolean changed = false;
        var iterator = activeWidgets.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (!present.containsKey(entry.getKey())) {
                if (lastClickedModuleCard == entry.getKey()) lastClickedModuleCard = null;
                widgets.remove(entry.getValue().id());
                styles.remove(entry.getValue().id());
                iterator.remove();
                changed = true;
            }
        }

        for (var entry : present.entrySet()) {
            ActiveWidget active = activeWidgets.get(entry.getKey());
            if (active != null) {
                // AE2 caches styles across screens; restore this screen's layout when returning from another screen.
                styles.put(active.id(), active.style());
                continue;
            }
            Registration registration = FACTORIES.get(entry.getKey());
            ICompositeWidget widget = Objects.requireNonNull(
                    registration.factory().apply(screen, entry.getValue().copy()), "module widget");
            var bounds = Objects.requireNonNull(widget.getBounds(), "module bounds");
            WidgetStyle style = new WidgetStyle();
            style.setLeft(bounds.getX());
            style.setTop(bounds.getY());
            style.setWidth(bounds.getWidth());
            style.setHeight(bounds.getHeight());
            String id = registration.id();
            if (widgets.containsKey(id)) {
                throw new IllegalStateException("Terminal module widget id already in use: " + id);
            }
            widgets.put(id, widget);
            styles.put(id, style);
            activeWidgets.put(entry.getKey(), new ActiveWidget(id, style, widget));
            changed = true;
        }
        panels.removeIf(panel -> !widgets.containsValue(panel));
        for (ActiveWidget active : activeWidgets.values()) {
            if (widgets.get(active.id()) instanceof AbstractTerminalModuleWidget panel && !panels.contains(panel)) {
                panels.add(panel);
            }
        }
        releaseInvalidCapture();
        return changed;
    }
}
