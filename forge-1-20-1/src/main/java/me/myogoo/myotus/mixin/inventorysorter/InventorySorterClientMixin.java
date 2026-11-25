package me.myogoo.myotus.mixin.inventorysorter;

import cpw.mods.inventorysorter.Action;
import me.myogoo.myotus.client.SortBlacklist;
import net.minecraftforge.client.event.ScreenEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.BiPredicate;

@Pseudo
@Mixin(targets = "cpw.mods.inventorysorter.KeyHandler", remap = false)
abstract class InventorySorterClientMixin {
    @Redirect(method = "onInputEvent", at = @At(value = "INVOKE", target = "Lcpw/mods/inventorysorter/Action;isActive()Z", remap = false), remap = false)
    private boolean myotus$blockBlacklistedSort(Action action, ScreenEvent event, BiPredicate<?, ?> predicate) {
        return action.isActive() && (action != Action.SORT || !SortBlacklist.isBlacklisted(event.getScreen()));
    }
}
