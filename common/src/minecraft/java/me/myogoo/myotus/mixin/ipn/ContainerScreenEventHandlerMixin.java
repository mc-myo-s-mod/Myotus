package me.myogoo.myotus.mixin.ipn;

import me.myogoo.myotus.client.SortBlacklist;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.anti_ad.mc.ipnext.gui.inject.InsertWidgetHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Pseudo
@Mixin(targets = "org.anti_ad.mc.ipnext.gui.inject.ContainerScreenEventHandler", remap = false)
public abstract class ContainerScreenEventHandlerMixin {
    @Shadow private static List<?> currentWidgets;

    @Inject(method = "onScreenInit(Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;Lkotlin/jvm/functions/Function1;)V",
            at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private void myotus$preventWidgets(AbstractContainerScreen<?> screen, @Coerce Object widgetAdder, CallbackInfo ci) {
        if (Minecraft.getInstance().screen == screen && SortBlacklist.isBlacklisted(screen)) {
            currentWidgets = null;
            InsertWidgetHandler.INSTANCE.insertWidget(null);
            ci.cancel();
        }
    }
}

