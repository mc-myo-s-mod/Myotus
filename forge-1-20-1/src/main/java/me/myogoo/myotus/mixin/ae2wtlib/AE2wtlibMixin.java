package me.myogoo.myotus.mixin.ae2wtlib;

import me.myogoo.myotus.api.wt.AddTerminalEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "de.mari_023.ae2wtlib.AE2wtlib", remap = false)
public abstract class AE2wtlibMixin {
    @Inject(method = "onAe2Initialized()V", at = @At(value = "INVOKE",
            target = "Lde/mari_023/ae2wtlib/UpgradeHelper;addUpgrades()V",
            shift = At.Shift.BEFORE, remap = false), require = 1, remap = false)
    private static void myotus$runAddTerminalEvent(CallbackInfo ci) {
        if (!AddTerminalEvent.didRun()) {
            AddTerminalEvent.run();
        }
    }
}
