package me.myogoo.myotus.mixin.ae2;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.AESubScreen;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.me.common.Repo;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.TerminalStyle;
import appeng.menu.AEBaseMenu;
import appeng.menu.me.common.MEStorageMenu;
import me.myogoo.myotus.api.client.ITerminalWidthScreen;
import me.myogoo.myotus.client.TerminalWidthStyleAccess;
import me.myogoo.myotus.client.TerminalWidthStyles;
import me.myogoo.myotus.client.gui.widgets.TerminalWidthToggleButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.IdentityHashMap;
import java.util.Map;

@Mixin(value = MEStorageScreen.class, remap = false)
public abstract class MEStorageScreenWidthMixin extends AEBaseScreen<AEBaseMenu> {
    @Shadow @Final @Mutable private TerminalStyle style;
    @Shadow @Final protected Repo repo;
    @Unique private ScreenStyle myotus$nativeStyle;
    @Unique private TerminalWidthToggleButton myotus$widthButton;
    @Unique private final Map<AbstractWidget, TerminalWidthStyles.WidgetSize> myotus$nativeWidgetSizes =
            new IdentityHashMap<>();

    protected MEStorageScreenWidthMixin(AEBaseMenu menu, Inventory inventory, Component title, ScreenStyle style) {
        super(menu, inventory, title, style);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void myotus$rememberNativeStyle(MEStorageMenu menu, Inventory inventory, Component title, ScreenStyle style,
            CallbackInfo ci) {
        myotus$nativeStyle = style;
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void myotus$applyWidthStyle(CallbackInfo ci) {
        var widthScreen = (ITerminalWidthScreen) this;
        var wideStyle = TerminalWidthStyles.findWideStyle(myotus$nativeStyle, Minecraft.getInstance().getResourceManager())
                .filter(candidate -> TerminalWidthStyles.supportsWidgets(widgets, super.style, candidate));
        if (myotus$widthButton == null) {
            myotus$widthButton = TerminalWidthToggleButton.forWideStyle(widthScreen, wideStyle)
                    .map(this::addToLeftToolbar).orElse(null);
        } else {
            myotus$widthButton.setVisibility(wideStyle.isPresent());
        }
        if (myotus$widthButton == null) {
            return;
        }

        var activeStyle = TerminalWidthStyles.selectStyle(myotus$nativeStyle, wideStyle, widthScreen.getTerminalWidthMode());
        if (activeStyle != super.style) {
            TerminalWidthStyles.resizeWidgets(widgets, super.style, activeStyle, myotus$nativeWidgetSizes);
            ((TerminalWidthStyleAccess) this).myotus$setScreenStyle(activeStyle);
            style = activeStyle.getTerminalStyle();
        }
        imageWidth = style.getScreenWidth();

        if (repo.getRowSize() != style.getSlotsPerRow()) {
            repo.setRowSize(style.getSlotsPerRow());
            if (repo.isPaused()) {
                repo.setPaused(false); // Unpausing rebuilds the pinned row for the new column count.
                repo.setPaused(true);
            } else {
                repo.updateView();
            }
        }
    }

    @Inject(method = "onReturnFromSubScreen", at = @At("TAIL"))
    private void myotus$applyWidthAfterSettings(AESubScreen<?, ?> subScreen, CallbackInfo ci) {
        if (myotus$widthButton != null) {
            // AE2 restores the old slot positions after the parent screen's first init.
            ((MEStorageScreen<?>) (Object) this).storeState();
            rebuildWidgets();
        }
    }
}
