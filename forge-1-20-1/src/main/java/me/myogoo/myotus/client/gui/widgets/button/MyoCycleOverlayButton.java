package me.myogoo.myotus.client.gui.widgets.button;

import appeng.client.gui.Icon;
import appeng.client.gui.widgets.IconButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.function.Supplier;

public class MyoCycleOverlayButton extends IconButton {
    private final Runnable action;
    private final Supplier<Item> itemOverlay;
    private final Supplier<List<Component>> tooltip;
    private final Supplier<Icon> icon;

    public MyoCycleOverlayButton(
            Runnable action,
            Supplier<Item> itemOverlay,
            Supplier<List<Component>> tooltip
    ) {
        this(() -> Icon.ARROW_RIGHT, action, itemOverlay, tooltip);
    }

    public MyoCycleOverlayButton(
            Supplier<Icon> icon,
            Runnable action,
            Supplier<Item> itemOverlay,
            Supplier<List<Component>> tooltip
    ) {
        super(button -> action.run());
        this.icon = icon;
        this.action = action;
        this.itemOverlay = itemOverlay;
        this.tooltip = tooltip;
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
