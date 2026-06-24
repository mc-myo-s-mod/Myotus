package me.myogoo.myotus.client.gui.widgets.button;

import appeng.client.gui.Icon;
import appeng.client.gui.widgets.IconButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.function.Supplier;

public class MyoCycleOverlayButton extends IconButton {
    private final Runnable action;
    private final Runnable secondaryAction;
    private final Supplier<Item> itemOverlay;
    private final Supplier<List<Component>> tooltip;
    private final Supplier<Icon> icon;

    public MyoCycleOverlayButton(
            Runnable action,
            Supplier<Item> itemOverlay,
            Supplier<List<Component>> tooltip
    ) {
        this(() -> Icon.ARROW_RIGHT, action, action, itemOverlay, tooltip);
    }

    public MyoCycleOverlayButton(
            Runnable action,
            Runnable secondaryAction,
            Supplier<Item> itemOverlay,
            Supplier<List<Component>> tooltip
    ) {
        this(() -> Icon.ARROW_RIGHT, action, secondaryAction, itemOverlay, tooltip);
    }

    public MyoCycleOverlayButton(
            Supplier<Icon> icon,
            Runnable action,
            Supplier<Item> itemOverlay,
            Supplier<List<Component>> tooltip
    ) {
        this(icon, action, action, itemOverlay, tooltip);
    }

    public MyoCycleOverlayButton(
            Supplier<Icon> icon,
            Runnable action,
            Runnable secondaryAction,
            Supplier<Item> itemOverlay,
            Supplier<List<Component>> tooltip
    ) {
        super(button -> action.run());
        this.icon = icon;
        this.action = action;
        this.secondaryAction = secondaryAction;
        this.itemOverlay = itemOverlay;
        this.tooltip = tooltip;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible || !this.isMouseOver(mouseX, mouseY)) {
            return false;
        }

        Runnable clickAction = switch (button) {
            case 0 -> this.action;
            case 1 -> this.secondaryAction;
            default -> null;
        };
        if (clickAction == null) {
            return false;
        }

        this.playDownSound(net.minecraft.client.Minecraft.getInstance().getSoundManager());
        clickAction.run();
        return true;
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
}
