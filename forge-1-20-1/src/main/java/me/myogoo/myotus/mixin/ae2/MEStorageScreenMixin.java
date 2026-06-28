package me.myogoo.myotus.mixin.ae2;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.me.common.TerminalSettingsScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.AEBaseMenu;
import appeng.menu.me.common.MEStorageMenu;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.InputConstants;
import me.myogoo.myotus.client.KeyBindings;
import me.myogoo.myotus.client.gui.config.TabbedTerminalSettingsScreen;
import me.myogoo.myotus.client.TerminalUpgradePanel;
import me.myogoo.myotus.client.gui.widgets.SidePanelToggleButton;
import me.myogoo.myotus.config.MyotusClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.gui.style.WidgetStyle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MEStorageScreen.class, remap = false)
public abstract class MEStorageScreenMixin extends AEBaseScreen<AEBaseMenu> {

    @Unique
    private TerminalUpgradePanel myotus$floatingSubScreen;

    @Unique
    private SidePanelToggleButton myotus$toggleButton;

    public MEStorageScreenMixin(AEBaseMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }


    @Inject(method = "<init>", at = @At("TAIL"))
    private void myotus$addFloatingSubScreen(CallbackInfo ci) {
        WidgetStyle customStyle = new WidgetStyle();
        ((ScreenStyleAccessor) (Object) style).getWidgets().put(TerminalUpgradePanel.WIDGET_ID, customStyle);

        if (this.menu instanceof MEStorageMenu storageMenu) {
            myotus$floatingSubScreen = new TerminalUpgradePanel(storageMenu, this.imageWidth);

            myotus$toggleButton = new SidePanelToggleButton(myotus$floatingSubScreen);
            myotus$updateToggleButtonVisibility();
            this.addToLeftToolbar(myotus$toggleButton);

            myotus$floatingSubScreen.setVisible(MyotusClientConfig.CLIENT.openSidePanel.get());
            Rect2i screenBounds = new Rect2i(this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
            myotus$floatingSubScreen.populateScreen(this::addRenderableWidget, screenBounds, this);

            this.widgets.add(TerminalUpgradePanel.WIDGET_ID, myotus$floatingSubScreen);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @WrapOperation(method = "showSettings", at = @At(value = "INVOKE", target = "Lappeng/client/gui/me/common/MEStorageScreen;switchToScreen(Lappeng/client/gui/AEBaseScreen;)V"), remap = false)
    private void myotus$redirectToTabbedSettings(MEStorageScreen instance, AEBaseScreen screen,
                                                 Operation<Void> original) {
        if (screen instanceof TerminalSettingsScreen) {
            original.call(instance, new TabbedTerminalSettingsScreen(instance));
        } else {
            original.call(instance, screen);
        }
    }

    @WrapOperation(method = "onReturnFromSubScreen", constant = @Constant(classValue = TerminalSettingsScreen.class), remap = false)
    private boolean myotus$extendInstanceCheck(Object obj, Operation<Boolean> original) {
        if (obj instanceof TabbedTerminalSettingsScreen) {
            return true;
        }
        return original.call(obj);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true, remap = true)
    private void myotus$onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (KeyBindings.OPEN_TERMINAL_SETTING.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode))) {
            if (Minecraft.getInstance().screen instanceof MEStorageScreen<?> storageScreen) {
                this.switchToScreen(new TabbedTerminalSettingsScreen<>(storageScreen));
                cir.setReturnValue(true);
            }
        }
        if (KeyBindings.TOGGLE_UPGRADE_TERMINAL_PANEL.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode))) {
            if (Minecraft.getInstance().screen instanceof MEStorageScreen<?> storageScreen) {
                if (this.myotus$toggleButton != null) {
                    this.myotus$toggleButton.run();
                }
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "init", at = @At("TAIL"), remap = true)
    public void myotus$init(CallbackInfo ci) {
        myotus$updateToggleButtonVisibility();
        if (myotus$floatingSubScreen != null) {
            myotus$floatingSubScreen.setVisible(MyotusClientConfig.CLIENT.openSidePanel.get());
        }
        WidgetStyle sidePanelStyle = ((ScreenStyleAccessor) (Object) style).getWidgets().get(TerminalUpgradePanel.WIDGET_ID);
        sidePanelStyle.setRight(-37);

        ((ScreenStyleAccessor) (Object) style).getWidgets().put(TerminalUpgradePanel.WIDGET_ID, sidePanelStyle);

        super.init();
    }

    @Unique
    private void myotus$updateToggleButtonVisibility() {
        if (myotus$toggleButton != null) {
            myotus$toggleButton.visible = MyotusClientConfig.CLIENT.showUpgradePanelButton.get();
        }
    }
}
