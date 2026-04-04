package me.myogoo.myotus.client.gui.widgets.button;

import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.ITooltip;
import com.mojang.blaze3d.systems.RenderSystem;
import me.myogoo.myotus.client.gui.MyoIcon;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

    public CustomImageButton(ResourceLocation path, OnPress onPress) {
        this(Blitter.texture(path), onPress);
    }

    public CustomImageButton(ResourceLocation path, int x, int y, OnPress onPress) {
        this(Blitter.texture(path, Icon.TEXTURE_HEIGHT, Icon.TEXTURE_WIDTH)
                .src(x,y, 16,16), onPress);
    }

    public CustomImageButton(ResourceLocation path, int x, int y, int width, int height, OnPress onPress) {
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
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
        if (!this.visible) {
            return;
        }

        var item = this.getItemOverlay();
        var blitter = getIcon();

        if (this.halfSize) {
            this.width = 8;
            this.height = 8;
        } else {
            this.width = 16;
            this.height = 16;
        }

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();

        if (isFocused()) {
            guiGraphics.fill(getX() - 1, getY() - 1, getX() + width + 1, getY(), 0xFFFFFFFF);
            guiGraphics.fill(getX() - 1, getY(), getX(), getY() + height, 0xFFFFFFFF);
            guiGraphics.fill(getX() + width, getY(), getX() + width + 1, getY() + height, 0xFFFFFFFF);
            guiGraphics.fill(getX() - 1, getY() + height, getX() + width + 1, getY() + height + 1, 0xFFFFFFFF);
        }

        if (this.halfSize) {
            var pose = guiGraphics.pose();
            pose.pushPose();
            pose.translate(getX(), getY(), 0.0F);
            pose.scale(0.5f, 0.5f, 1.f);

            if (!disableBackground) {
                Icon.TOOLBAR_BUTTON_BACKGROUND.getBlitter().dest(0, 0).blit(guiGraphics);
            }
            if (blitter != null) {
                if (!this.active) {
                    blitter.opacity(0.5f);
                }
                blitter.dest(0, 0).blit(guiGraphics);
            }
            pose.popPose();
        } else {
            if (!disableBackground) {
                Icon.TOOLBAR_BUTTON_BACKGROUND.getBlitter().dest(getX(), getY()).blit(guiGraphics);
            }
            if (blitter != null) {
                if (!this.active) {
                    blitter.opacity(0.5f);
                }
                blitter.dest(getX(), getY()).blit(guiGraphics);
            }
        }

        RenderSystem.enableDepthTest();

        if (item != null) {
            guiGraphics.renderItem(new ItemStack(item), getX(), getY());
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
