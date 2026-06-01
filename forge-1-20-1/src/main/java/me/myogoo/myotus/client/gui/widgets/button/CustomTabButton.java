package me.myogoo.myotus.client.gui.widgets.button;

import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.TabButton;
import me.myogoo.myotus.client.gui.MyoIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CustomTabButton extends TabButton {
    @Nullable
    private Blitter blitter;
    @Nullable
    private ItemStack stack;

    public CustomTabButton(MyoIcon icon, Component message, OnPress onPress) {
        this(icon.getBlitter(), message, onPress);
    }

    public CustomTabButton(@Nullable Blitter blitter, Component message, OnPress onPress) {
        super(ItemStack.EMPTY, message, onPress);
        this.blitter = blitter;
    }

    public CustomTabButton(Icon icon, Component message, OnPress onPress) {
        super(icon, message, onPress);
    }

    public CustomTabButton(ItemStack stack, Component message, OnPress onPress) {
        super(stack, message, onPress);
        this.stack =  stack;

    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int x, int y, float partial) {
        if (this.visible) {
            var backdrop = switch (this.getStyle()) {
                case CORNER -> this.isFocused() ? Icon.TAB_BUTTON_BACKGROUND_BORDERLESS_FOCUS
                        : Icon.TAB_BUTTON_BACKGROUND_BORDERLESS;
                case BOX -> this.isFocused() ? Icon.TAB_BUTTON_BACKGROUND_FOCUS : Icon.TAB_BUTTON_BACKGROUND;
                case HORIZONTAL -> {
                    if (this.isFocused()) {
                        yield Icon.HORIZONTAL_TAB_FOCUS;
                    } else if (this.isSelected()) {
                        yield Icon.HORIZONTAL_TAB_SELECTED;
                    }
                    yield Icon.HORIZONTAL_TAB;
                }
            };
            backdrop.getBlitter().dest(getX(), getY()).blit(guiGraphics);

            var iconX = switch (this.getStyle()) {
                case CORNER -> 1;
                case BOX -> 2;
                case HORIZONTAL -> 3;
            };
            var iconY = switch (this.getStyle()) {
                case CORNER -> 1;
                case BOX -> 2;
                case HORIZONTAL -> 3;
            };

            if (blitter != null) {
                blitter.dest(getX() + iconX, getY() + iconY - 1).blit(guiGraphics);
            } else if (stack != null && !stack.isEmpty()) {
                var pose = guiGraphics.pose();
                pose.pushPose();
                pose.translate(0f, -1f, 100);
                guiGraphics.renderItem(this.stack, getX() + iconX, getY() + iconY);
                var font = Minecraft.getInstance().font;
                guiGraphics.renderItemDecorations(font, this.stack, getX() + iconX, getY() + iconY);
                pose.popPose();
            } else {
                super.renderWidget(guiGraphics, x, y, partial);
            }
        }
    }
}
