package me.myogoo.myotus.mixin.ae2;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.me.common.TerminalSettingsScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.Scrollbar;
import appeng.menu.AEBaseMenu;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.InputConstants;
import me.myogoo.myotus.client.KeyBindings;
import me.myogoo.myotus.client.gui.config.TabbedTerminalSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MEStorageScreen.class, remap = false)
public class MEStorageScreenMixin extends AEBaseScreen<AEBaseMenu> {
    @Shadow
    @Final
    private Scrollbar scrollbar;
    @Unique
    private static final String MYOTUS$AE2WTLIB_UPGRADES_ID = "scrollingUpgrades";

    public MEStorageScreenMixin(AEBaseMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
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

    @Inject(method = "keyPressed", at = @At(value = "HEAD"), cancellable = true)
    public void registerKeyHandler(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (KeyBindings.OPEN_TERMINAL_SETTING.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode))) {
            if (Minecraft.getInstance().screen instanceof MEStorageScreen<?> storageScreen) {
                this.switchToScreen(new TabbedTerminalSettingsScreen<>(storageScreen));
                cir.setReturnValue(true);
            }
        }
    }

    @Unique
    private int myotus$getVisibleRowsForNextInit() {
        var terminalStyle = this.style.getTerminalStyle();
        if (terminalStyle == null) {
            return 2;
        }
        int availableHeight = this.height - 2 * this.config.getTerminalMargin();
        return Math.max(2, this.config.getTerminalStyle().getRows(terminalStyle.getPossibleRows(availableHeight)));
    }
}
