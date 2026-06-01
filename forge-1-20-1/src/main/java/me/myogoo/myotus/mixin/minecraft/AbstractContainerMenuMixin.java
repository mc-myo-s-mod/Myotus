package me.myogoo.myotus.mixin.minecraft;

import appeng.menu.me.common.MEStorageMenu;
import me.myogoo.myotus.api.ITerminalUpgradeCard;
import me.myogoo.myotus.menu.MyoSlotSemantics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
    @Inject(method = "removed", at = @At("HEAD"))
    void myotus$dispatchUpgradeClose(Player player, CallbackInfo ci) {
        if(player.containerMenu instanceof MEStorageMenu menu) {
            for (Slot slot : menu.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT)) {
                ItemStack stack = slot.getItem();
                if (!stack.isEmpty() && stack.getItem() instanceof ITerminalUpgradeCard card) {
                    card.onTerminalClose(menu, stack);
                }
            }
        }
    }
}
