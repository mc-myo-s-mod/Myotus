package me.myogoo.myotus.mixin.inventorysorter;

import me.myogoo.myotus.integration.sort.SortBlacklistRegistration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "cpw.mods.inventorysorter.SortingHandler", remap = false)
abstract class InventorySorterServerMixin {
    @Inject(method = "accept(Lcpw/mods/inventorysorter/ContainerContext;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void myotus$blockBlacklistedMenu(@Coerce Object context, CallbackInfo ci) {
        if (SortBlacklistRegistration.isMenuBlacklisted(((InventorySorterContextAccessor) context).myotus$getPlayer().containerMenu)) {
            ci.cancel();
        }
    }
}
