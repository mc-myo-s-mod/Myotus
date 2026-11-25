package me.myogoo.myotus.mixin.ipn;

import me.myogoo.myotus.client.SortBlacklist;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "org.anti_ad.mc.ipnext.inventory.GeneralInventoryActions", remap = false)
public abstract class GeneralInventoryActionsMixin {
    @Inject(method = "doSort(Lnet/minecraft/world/inventory/AbstractContainerMenu;ZZ)V",
            at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private void myotus$preventSort(AbstractContainerMenu menu, boolean firstOption, boolean secondOption, CallbackInfo ci) {
        if (SortBlacklist.isBlacklisted(menu, Minecraft.getInstance().screen)) {
            ci.cancel();
        }
    }

    @Inject(method = "doSortInColumns(Lnet/minecraft/world/inventory/AbstractContainerMenu;ZZ)V",
            at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private void myotus$preventColumnSort(AbstractContainerMenu menu, boolean firstOption, boolean secondOption, CallbackInfo ci) {
        if (SortBlacklist.isBlacklisted(menu, Minecraft.getInstance().screen)) {
            ci.cancel();
        }
    }

    @Inject(method = "doSortInRows(Lnet/minecraft/world/inventory/AbstractContainerMenu;ZZ)V",
            at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private void myotus$preventRowSort(AbstractContainerMenu menu, boolean firstOption, boolean secondOption, CallbackInfo ci) {
        if (SortBlacklist.isBlacklisted(menu, Minecraft.getInstance().screen)) {
            ci.cancel();
        }
    }
}

