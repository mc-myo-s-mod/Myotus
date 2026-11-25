package me.myogoo.myotus.mixin.ae2;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.WidgetContainer;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.AEBaseMenu;
import me.myogoo.myotus.api.client.ITerminalWidthScreen;
import me.myogoo.myotus.api.client.TerminalWidthMode;
import me.myogoo.myotus.client.TerminalWidthStyleAccess;
import me.myogoo.myotus.config.MyotusConfig;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(value = AEBaseScreen.class, remap = false)
public abstract class AEBaseScreenWidthMixin extends AbstractContainerScreen<AEBaseMenu>
        implements ITerminalWidthScreen, TerminalWidthStyleAccess {
    @Shadow @Final @Mutable protected ScreenStyle style;
    @Shadow @Final protected WidgetContainer widgets;

    protected AEBaseScreenWidthMixin(AEBaseMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void myotus$setScreenStyle(ScreenStyle style) {
        this.style = style;
        ((WidgetContainerAccessor) widgets).myotus$setStyle(style);
    }

    @Override
    public TerminalWidthMode getTerminalWidthMode() {
        return MyotusConfig.CLIENT.terminalWidthMode.get();
    }

    @Override
    public void setTerminalWidthMode(TerminalWidthMode mode) {
        MyotusConfig.CLIENT.terminalWidthMode.set(Objects.requireNonNull(mode, "mode"));
        MyotusConfig.CLIENT.save();
        rebuildWidgets();
    }
}
