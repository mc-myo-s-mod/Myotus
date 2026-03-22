package me.myogoo.myotus.mixin.ae2;

import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.me.common.TerminalSettingsScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.me.common.MEStorageMenu;
import appeng.client.gui.widgets.UpgradesPanel;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.InputConstants;
import me.myogoo.myotus.client.KeyBindings;
import me.myogoo.myotus.client.gui.config.TabbedTerminalSettingsScreen;
import me.myogoo.myotus.client.SidePanelSubScreen;
import me.myogoo.myotus.client.gui.widgets.SidePanelToggleButton;
import me.myogoo.myotus.config.MyotusClientConfig;
import me.myogoo.myotus.menu.MyoSlotSemantics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.gui.style.WidgetStyle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MEStorageScreen.class, remap = false)
public class MEStorageScreenMixin extends AEBaseScreen<AEBaseMenu> {

    @Unique
    private SidePanelSubScreen myocertus$floatingSubScreen;

    @Unique
    private SidePanelToggleButton myocertus$toggleButton;

    public MEStorageScreenMixin(AEBaseMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void myocertus$addFloatingSubScreen(CallbackInfo ci) {
        // AE2의 WidgetContainer.add() 시 스타일이 없으면 예외를 던지므로, 런타임에 스타일을 주입합니다.
        WidgetStyle customStyle = new WidgetStyle();
        customStyle.setRight(-3);
        ((ScreenStyleAccessor) (Object) style).getWidgets().put("myocertus_floating_sub_screen", customStyle);

        if (this.menu instanceof MEStorageMenu storageMenu) {
            myocertus$floatingSubScreen = new SidePanelSubScreen(storageMenu);
            myocertus$floatingSubScreen.setPosition(new Point(this.imageWidth, 0));

            myocertus$toggleButton = new SidePanelToggleButton(myocertus$floatingSubScreen);
            this.addToLeftToolbar(myocertus$toggleButton);

            if (myocertus$floatingSubScreen != null) {
                myocertus$floatingSubScreen.setVisible(MyotusClientConfig.CLIENT.openSidePanel.get());
                Rect2i screenBounds = new Rect2i(this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
                myocertus$floatingSubScreen.populateScreen(this::addRenderableWidget, screenBounds, this);

                this.widgets.add("myocertus_floating_sub_screen", myocertus$floatingSubScreen);
                this.addRenderableWidget(myocertus$toggleButton);
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @WrapOperation(method = "showSettings", at = @At(value = "INVOKE", target = "Lappeng/client/gui/me/common/MEStorageScreen;switchToScreen(Lappeng/client/gui/AEBaseScreen;)V"), remap = false)
    private void myocertus$redirectToTabbedSettings(MEStorageScreen instance, AEBaseScreen screen,
            Operation<Void> original) {
        if (screen instanceof TerminalSettingsScreen) {
            original.call(instance, new TabbedTerminalSettingsScreen(instance));
        } else {
            original.call(instance, screen);
        }
    }

    @WrapOperation(method = "onReturnFromSubScreen", constant = @Constant(classValue = TerminalSettingsScreen.class), remap = false)
    private boolean myocertus$extendInstanceCheck(Object obj, Operation<Boolean> original) {
        if (obj instanceof TabbedTerminalSettingsScreen) {
            return true;
        }
        return original.call(obj);
    }

    @Inject(method = "keyPressed", at = @At(value = "HEAD"), cancellable = true)
    public void registerKeyHandler(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (KeyBindings.OPEN_TERMINAL_SETTING.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode))) {
            if (Minecraft.getInstance().screen instanceof MEStorageScreen<?> storageScreen) {
                this.switchToScreen(new TabbedTerminalSettingsScreen<>(storageScreen));
                cir.setReturnValue(true);
            }
        }
        if (KeyBindings.TOGGLE_SUB_SIDE_PANEL.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode))) {
            if (Minecraft.getInstance().screen instanceof MEStorageScreen<?> storageScreen) {
                this.myocertus$toggleButton.run();
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "init", at = @At("HEAD"))
    protected void repositionSubSidePanel(CallbackInfo ci) {
        if(myocertus$floatingSubScreen != null) {
            myocertus$floatingSubScreen.setVisible(MyotusClientConfig.CLIENT.openSidePanel.get());

        }
        WidgetStyle sidePanelStyle = ((ScreenStyleAccessor) (Object) style).getWidgets().get("myocertus_floating_sub_screen");
        WidgetContainerAccessor widget = (WidgetContainerAccessor) this.widgets;
        sidePanelStyle.setRight(-3);

        if(widget.getCompositeWidgets().containsKey("scrollingUpgrades")) {
            sidePanelStyle.setRight(-32); //default value: -20
        }
        //wtf?
        if(widget.getCompositeWidgets().containsKey("upgradeScrollbar") && widget.getCompositeWidgets().get("upgradeScrollbar").isVisible()) {
            sidePanelStyle.setRight(-32);
        }
        ((ScreenStyleAccessor) (Object) style).getWidgets().put("myocertus_floating_sub_screen", sidePanelStyle);
    }
}
