package me.myogoo.myotus.mixin.ae2;

import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.StyleManager;
import me.myogoo.myotus.client.TerminalWidthStyles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = StyleManager.class, remap = false)
public abstract class StyleManagerMixin {
    @Inject(method = "loadStyleDoc", at = @At("RETURN"))
    private static void myotus$recordStylePath(String path, CallbackInfoReturnable<ScreenStyle> cir) {
        TerminalWidthStyles.recordPath(cir.getReturnValue(), path);
    }
}
