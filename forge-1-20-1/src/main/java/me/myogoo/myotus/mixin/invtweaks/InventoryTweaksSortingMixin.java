package me.myogoo.myotus.mixin.invtweaks;

import me.myogoo.myotus.integration.sort.SortBlacklistRegistration;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "invtweaks.util.Sorting", remap = false)
public abstract class InventoryTweaksSortingMixin {
    @Inject(method = "executeSort", at = @At("HEAD"), cancellable = true)
    private static void myotus$skipBlacklistedMenu(Player player, boolean isPlayer, String screenName, CallbackInfo ci) {
        if (player instanceof ServerPlayer serverPlayer
                && SortBlacklistRegistration.isMenuBlacklisted(serverPlayer.containerMenu)) {
            ci.cancel();
        }
    }
}
