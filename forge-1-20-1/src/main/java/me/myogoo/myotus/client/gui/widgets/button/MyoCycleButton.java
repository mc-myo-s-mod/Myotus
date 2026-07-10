package me.myogoo.myotus.client.gui.widgets.button;

import appeng.client.gui.Icon;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.widgets.IconButton;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MyoCycleButton extends IconButton {
    private final Consumer<MouseButton> clickAction;
    private final Supplier<Item> itemOverlay;
    private final Supplier<List<Component>> tooltip;
    private final Supplier<Icon> icon;

    public MyoCycleButton(
            Consumer<MouseButton> clickAction,
            Supplier<Item> itemOverlay,
            Supplier<List<Component>> tooltip
    ) {
        this(() -> Icon.ARROW_RIGHT, clickAction, itemOverlay, tooltip);
    }

    public MyoCycleButton(
            Runnable leftClickAction,
            Runnable rightClickAction,
            Supplier<Item> itemOverlay,
            Supplier<List<Component>> tooltip
    ) {
        this(() -> Icon.ARROW_RIGHT, actions(leftClickAction, rightClickAction), itemOverlay, tooltip);
    }

    public MyoCycleButton(
            Supplier<Icon> icon,
            Consumer<MouseButton> clickAction,
            Supplier<Item> itemOverlay,
            Supplier<List<Component>> tooltip
    ) {
        super(button -> clickAction.accept(MouseButton.LEFT));
        this.icon = Objects.requireNonNull(icon, "icon");
        this.clickAction = Objects.requireNonNull(clickAction, "clickAction");
        this.itemOverlay = Objects.requireNonNull(itemOverlay, "itemOverlay");
        this.tooltip = Objects.requireNonNull(tooltip, "tooltip");
    }

    public MyoCycleButton(
            Supplier<Icon> icon,
            Runnable leftClickAction,
            Runnable rightClickAction,
            Supplier<Item> itemOverlay,
            Supplier<List<Component>> tooltip
    ) {
        this(icon, actions(leftClickAction, rightClickAction), itemOverlay, tooltip);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible || !this.isMouseOver(mouseX, mouseY)) {
            return false;
        }

        MouseButton mouseButton = resolveMouseButton(button, isHandlingRightClick());
        if (mouseButton == null) {
            return false;
        }

        this.playDownSound(Minecraft.getInstance().getSoundManager());
        this.clickAction.accept(mouseButton);
        return true;
    }

    static @Nullable MouseButton resolveMouseButton(int button, boolean handlingRightClick) {
        if (button == InputConstants.MOUSE_BUTTON_RIGHT
                || button == InputConstants.MOUSE_BUTTON_LEFT && handlingRightClick) {
            return MouseButton.RIGHT;
        }
        return button == InputConstants.MOUSE_BUTTON_LEFT ? MouseButton.LEFT : null;
    }

    private static boolean isHandlingRightClick() {
        return Minecraft.getInstance().screen instanceof AEBaseScreen<?> screen && screen.isHandlingRightClick();
    }

    private static Consumer<MouseButton> actions(Runnable leftClickAction, Runnable rightClickAction) {
        Objects.requireNonNull(leftClickAction, "leftClickAction");
        Objects.requireNonNull(rightClickAction, "rightClickAction");
        return mouseButton -> {
            if (mouseButton == MouseButton.LEFT) {
                leftClickAction.run();
            } else {
                rightClickAction.run();
            }
        };
    }

    @Override
    protected Icon getIcon() {
        return this.icon.get();
    }

    @Override
    protected Item getItemOverlay() {
        return this.itemOverlay.get();
    }

    @Override
    public List<Component> getTooltipMessage() {
        return this.tooltip.get();
    }

    public enum MouseButton {
        LEFT,
        RIGHT
    }
}
