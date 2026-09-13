package me.myogoo.myotus.client.gui.widgets.button;

import appeng.util.Icon;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.ITooltip;
import me.myogoo.myotus.client.gui.MyoIcon;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CustomImageButton extends Button implements ITooltip {
    private boolean halfSize = false;
    private boolean disableClickSound = false;
    private boolean disableBackground = false;
    private final Blitter blitter;

    public CustomImageButton(OnPress onPress) {
        super(0, 0, 16, 16, Component.empty(), onPress, Button.DEFAULT_NARRATION);
        this.blitter = null;
    }


    public CustomImageButton(Blitter blitter, OnPress onPress) {
        super(0, 0, 16, 16, Component.empty(), onPress, Button.DEFAULT_NARRATION);
        this.blitter = blitter;
    }

    public CustomImageButton(MyoIcon icon, OnPress onPress) {
        this(icon.getBlitter(), onPress);
    }

    public CustomImageButton(Identifier path, OnPress onPress) {
        this(Blitter.guiSprite(path), onPress);
    }

    public CustomImageButton(Identifier path, int x, int y, OnPress onPress) {
        this(Blitter.texture(path, Icon.TEXTURE_HEIGHT, Icon.TEXTURE_WIDTH)
                .src(x,y, 16,16), onPress);
    }

    public CustomImageButton(Identifier path, int x, int y, int width, int height, OnPress onPress) {
        this(Blitter.texture(path, Icon.TEXTURE_HEIGHT, Icon.TEXTURE_WIDTH)
                        .src(x,y,width,height)
                , onPress);
    }

    public void setVisibility(boolean vis) {
        this.visible = vis;
        this.active = vis;
    }

    @Override
    public void playDownSound(SoundManager soundHandler) {
        if (!disableClickSound) {
            super.playDownSound(soundHandler);
        }
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partial) {
        if (this.visible) {
            var item = this.getItemOverlay();
            var blitter = getIcon();
            if (this.halfSize) {
                this.width = 8;
                this.height = 8;
            }

            var yOffset = isHovered() ? 1 : 0;

            if (this.halfSize) {
                if (!disableBackground) {
                    Blitter.icon(Icon.TOOLBAR_BUTTON_BACKGROUND).dest(getX(), getY()).blit(guiGraphics);
                }
                if (item != null) {
                    guiGraphics.item(new ItemStack(item), getX(), getY());
                } else if(blitter != null) {
                    if (!this.active) {
                        blitter.opacity(0.5f);
                    }
                    blitter.dest(getX(), getY()).blit(guiGraphics);
                }
            } else {
                if (!disableBackground) {
                    Icon bgIcon = isHovered() ? Icon.TOOLBAR_BUTTON_BACKGROUND_HOVER
                            : isFocused() ? Icon.TOOLBAR_BUTTON_BACKGROUND_FOCUS : Icon.TOOLBAR_BUTTON_BACKGROUND;

                    Blitter.icon(bgIcon)
                            .dest(getX() - 1, getY() + yOffset, 18, 20)
                            .blit(guiGraphics);
                }
                if (item != null) {
                    guiGraphics.item(new ItemStack(item), getX(), getY() + 1 + yOffset);
                } else if(blitter != null)  {
                    blitter.dest(getX(), getY() + 1 + yOffset).blit(guiGraphics);
                }
            }
        }
    }

    protected Blitter getIcon() {
        return this.blitter;
    }

    @Nullable
    protected Item getItemOverlay() {
        return null;
    }

    @Override
    public List<Component> getTooltipMessage() {
        return Collections.singletonList(getMessage());
    }

    @Override
    public Rect2i getTooltipArea() {
        return new Rect2i(
                getX(),
                getY(),
                this.halfSize ? 8 : 16,
                this.halfSize ? 8 : 16);
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return this.visible;
    }

    public boolean isHalfSize() {
        return this.halfSize;
    }

    public void setHalfSize(boolean halfSize) {
        this.halfSize = halfSize;
    }

    public boolean isDisableClickSound() {
        return disableClickSound;
    }

    public void setDisableClickSound(boolean disableClickSound) {
        this.disableClickSound = disableClickSound;
    }

    public boolean isDisableBackground() {
        return disableBackground;
    }

    public void setDisableBackground(boolean disableBackground) {
        this.disableBackground = disableBackground;
    }
}
