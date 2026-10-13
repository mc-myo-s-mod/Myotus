package me.myogoo.myotus.client.dev;

import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.ICompositeWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/** Development-only fixture for attachment, removal, resize and child-widget cleanup. */
public final class TerminalModuleTestWidget implements ICompositeWidget {
    private int x;
    private int y;
    private int clicks;

    @Override
    public void setPosition(Point position) {
        x = position.getX();
        y = position.getY();
    }

    @Override
    public void setSize(int width, int height) {
    }

    @Override
    public Rect2i getBounds() {
        return new Rect2i(x, y, 120, 20);
    }

    @Override
    public void populateScreen(Consumer<AbstractWidget> addWidget, Rect2i bounds, AEBaseScreen<?> screen) {
        setPosition(new Point(bounds.getWidth() + 4, 104));
        addWidget.accept(Button.builder(label(), button -> {
            clicks++;
            button.setMessage(label());
        }).bounds(bounds.getX() + x, bounds.getY() + y, 120, 20).build());
    }

    private Component label() {
        return Component.literal("Test module: " + clicks);
    }
}
