package me.myogoo.myotus.mixin.ae2;

import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.me.common.MEStorageMenu;
import me.myogoo.myotus.api.ITerminalCard;
import me.myogoo.myotus.menu.MyoSlotSemantics;
import me.myogoo.myotus.menu.TerminalModuleSlots;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = AEBaseMenu.class, remap = false)
public abstract class AEBaseMenuMixin extends AbstractContainerMenu {
    protected AEBaseMenuMixin(MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    private void myotus$rejectDisabledModuleClick(int index, int button, ClickType clickType, Player player,
            CallbackInfo ci) {
        if ((Object) this instanceof MEStorageMenu menu && index >= 0 && index < slots.size()) {
            Slot slot = slots.get(index);
            if (TerminalModuleSlots.isModuleSlot(menu, slot)
                    && !TerminalModuleSlots.isInteractionAllowed(menu, slot, player)) {
                resetQuickCraft();
                ci.cancel();
            } else if (clickType == ClickType.CLONE && player.hasInfiniteMaterials() && getCarried().isEmpty()
                    && TerminalModuleSlots.isModuleSlot(menu, slot)) {
                setCarried(TerminalModuleSlots.getCloneStack(menu, slot));
                ci.cancel();
            }
        }
    }

    @Inject(method = "isValidQuickMoveDestination", at = @At("HEAD"), cancellable = true)
    private void myotus$excludeModuleQuickMoveDestination(Slot slot, ItemStack stack, boolean fromPlayerSide,
            CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof MEStorageMenu menu && TerminalModuleSlots.isModuleSlot(menu, slot)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void myotus$quickMoveTerminalUpgradeToUpgradeSlot(Player player, int index,
            CallbackInfoReturnable<ItemStack> cir) {
        if (!((Object) this instanceof MEStorageMenu menu) || index < 0 || index >= this.slots.size()) {
            return;
        }

        Slot sourceSlot = this.slots.get(index);
        if (TerminalModuleSlots.isModuleSlot(menu, sourceSlot)) {
            if (!player.level().isClientSide() && sourceSlot.isActive() && sourceSlot.mayPickup(player)) {
                ItemStack source = sourceSlot.getItem();
                var destinations = new java.util.ArrayList<>(menu.getSlots(SlotSemantics.PLAYER_INVENTORY));
                destinations.addAll(menu.getSlots(SlotSemantics.PLAYER_HOTBAR));
                int capacity = 0;
                for (Slot target : destinations) {
                    if (target.mayPlace(source) && (target.getItem().isEmpty()
                            || ItemStack.isSameItemSameComponents(source, target.getItem()))) {
                        capacity += Math.max(0, target.getMaxStackSize(source) - target.getItem().getCount());
                    }
                }
                // Curios can reject a real extraction even after mayPickup's simulation succeeds.
                ItemStack remaining = sourceSlot.safeTake(Math.min(capacity, source.getCount()),
                        Integer.MAX_VALUE, player);
                for (boolean merge : new boolean[] { true, false }) {
                    for (Slot target : destinations) {
                        if (target.hasItem() == merge) {
                            remaining = target.safeInsert(remaining);
                        }
                    }
                }
                if (!remaining.isEmpty()) {
                    remaining = sourceSlot.safeInsert(remaining);
                    player.getInventory().placeItemBackInInventory(remaining);
                }
            }
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }
        ItemStack sourceStack = sourceSlot.getItem();
        if (player.level().isClientSide() || sourceSlot.container != player.getInventory()
                || !sourceSlot.mayPickup(player) || sourceStack.isEmpty()
                || !(sourceStack.getItem() instanceof ITerminalCard)) {
            return;
        }

        List<Slot> upgradeSlots = menu.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT);
        if (upgradeSlots.contains(sourceSlot)) {
            return;
        }

        ItemStack original = sourceStack.copy();
        if (myotus$moveOneStackToEmptyUpgradeSlot(player, sourceSlot, sourceStack, upgradeSlots)) {
            cir.setReturnValue(original);
        }
    }

    @Unique
    private static boolean myotus$moveOneStackToEmptyUpgradeSlot(Player player, Slot sourceSlot, ItemStack sourceStack,
            List<Slot> upgradeSlots) {
        for (Slot targetSlot : upgradeSlots) {
            if (targetSlot.hasItem() || !targetSlot.mayPlace(sourceStack)) {
                continue;
            }

            int moveCount = Math.min(sourceStack.getCount(), targetSlot.getMaxStackSize(sourceStack));
            if (moveCount <= 0) {
                continue;
            }

            ItemStack moving = sourceStack.copy();
            moving.setCount(moveCount);
            ItemStack remainder = targetSlot.safeInsert(moving, moveCount);
            int inserted = moveCount - remainder.getCount();
            if (inserted <= 0) {
                continue;
            }

            ItemStack removed = sourceSlot.remove(inserted);
            sourceSlot.setChanged();
            sourceSlot.onTake(player, removed);
            return true;
        }
        return false;
    }
}
