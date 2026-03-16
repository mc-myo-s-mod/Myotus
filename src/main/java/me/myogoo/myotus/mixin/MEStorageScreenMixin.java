package me.myogoo.myotus.mixin;

import appeng.client.Point;
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
import me.myogoo.myotus.client.SidePanelSubScreen;
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
            // 초기 위치 설정 (메인 스크린 오른쪽에 이음새 없이 연결)
            myocertus$floatingSubScreen.setPosition(new Point(this.imageWidth, 0));

            // 토글 버튼 생성 (FloatingSubScreen을 받는 방향으로 수정 필요할 수 있으나 일단 유지)
            myocertus$toggleButton = new SidePanelToggleButton(myocertus$floatingSubScreen);
            this.addToLeftToolbar(myocertus$toggleButton);

            if (myocertus$floatingSubScreen != null) {
                // config 값으로 가시성 설정
                myocertus$floatingSubScreen.setVisible(MyotusClientConfig.INSTANCE.openSidePanel.get());
                // 스크린 원점 정보 전달 및 초기화
                Rect2i screenBounds = new Rect2i(this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
                myocertus$floatingSubScreen.populateScreen(this::addRenderableWidget, screenBounds, this);

                // WidgetContainer에 등록하여 update, render, mouseEvent 처리를 전부 위임합니다.
                this.widgets.add("myocertus_floating_sub_screen", myocertus$floatingSubScreen);

                // 툴바 버튼 등록
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
    }
}
