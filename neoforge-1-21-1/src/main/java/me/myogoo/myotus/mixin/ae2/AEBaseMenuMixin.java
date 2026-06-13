package me.myogoo.myotus.mixin.ae2;

import appeng.menu.AEBaseMenu;
import appeng.menu.me.common.MEStorageMenu;
import me.myogoo.myotus.api.ITerminalUpgradeCard;
import me.myogoo.myotus.menu.MyoSlotSemantics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = AEBaseMenu.class, remap = false)
public abstract class AEBaseMenuMixin extends AbstractContainerMenu {
    protected AEBaseMenuMixin(MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void myotus$quickMoveTerminalUpgradeToUpgradeSlot(Player player, int index,
            CallbackInfoReturnable<ItemStack> cir) {
        if (!((Object) this instanceof MEStorageMenu menu) || index < 0 || index >= this.slots.size()) {
            return;
        }

        Slot sourceSlot = this.slots.get(index);
        ItemStack sourceStack = sourceSlot.getItem();
        if (sourceStack.isEmpty() || !(sourceStack.getItem() instanceof ITerminalUpgradeCard)) {
            return;
        }

        List<Slot> upgradeSlots = menu.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT);
        if (upgradeSlots.contains(sourceSlot)) {
            return;
        }

        ItemStack original = sourceStack.copy();
        if (myotus$moveOneStackToEmptyUpgradeSlot(sourceSlot, sourceStack, upgradeSlots)) {
            cir.setReturnValue(original);
        }
    }

    @Unique
    private static boolean myotus$moveOneStackToEmptyUpgradeSlot(Slot sourceSlot, ItemStack sourceStack, List<Slot> upgradeSlots) {
        for (Slot targetSlot : upgradeSlots) {
            if (targetSlot.hasItem() || !targetSlot.mayPlace(sourceStack)) {
                continue;
            }

            int moveCount = Math.min(sourceStack.getCount(), targetSlot.getMaxStackSize(sourceStack));
            if (moveCount <= 0) {
                continue;
            }

            ItemStack movedStack = sourceStack.copy();
            movedStack.setCount(moveCount);
            targetSlot.set(movedStack);
            targetSlot.setChanged();

            sourceStack.shrink(moveCount);
            if (sourceStack.isEmpty()) {
                sourceSlot.set(ItemStack.EMPTY);
            }
            sourceSlot.setChanged();
            return true;
        }
        return false;
    }
}
