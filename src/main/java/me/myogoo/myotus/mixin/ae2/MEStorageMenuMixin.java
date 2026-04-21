package me.myogoo.myotus.mixin.ae2;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import appeng.menu.slot.AppEngSlot;
import appeng.util.inv.AppEngInternalInventory;
import me.myogoo.myotus.api.ITerminalUpgradeCard;
import me.myogoo.myotus.menu.MyoSlotSemantics;
import me.myogoo.myotus.menu.PlayerUpgradeContainer;
import me.myogoo.myotus.menu.TerminalUpgradeHost;
import me.myogoo.myotus.menu.TerminalUpgradeStorageKey;
import me.myogoo.myotus.menu.TerminalUpgradeSlotFilter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import appeng.api.storage.ITerminalHost;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.slot.RestrictedInputSlot;
import appeng.util.ConfigMenuInventory;
import appeng.helpers.externalstorage.GenericStackInv;


@Mixin(value = MEStorageMenu.class, remap = false)
public abstract class MEStorageMenuMixin extends AEBaseMenu {

    @Shadow
    @Final
    private List<RestrictedInputSlot> viewCellSlots;

    // 업그레이드 슬롯의 이전 아이템 상태 추적 (삽입/제거 이벤트 감지용)
    @Unique
    private final Map<Slot, ItemStack> myotus$prevUpgradeItems = new IdentityHashMap<>();

    private MEStorageMenuMixin(MenuType<?> menuType, int id, Inventory playerInventory, Object host) {
        super(menuType, id, playerInventory, host);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lappeng/api/storage/ITerminalHost;Z)V", at = @At("TAIL"))
    private void myotus$ensureSidePanelSlots(MenuType<?> menuType, int id, Inventory ip, ITerminalHost host,
            boolean bindInventory, CallbackInfo ci) {

        if (this.viewCellSlots == null || this.viewCellSlots.isEmpty()) {
            myotus$addCustomViewCellSlots(host);
        }

        if (this.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT).isEmpty()) {
            myotus$addCustomUpgradeSlots(host);
        }

        // 이전 슬롯 상태 초기화 (삽입/제거 감지용, broadcastChanges에서 비교)
        if (!this.getPlayer().level().isClientSide()) {
            for (Slot slot : this.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT)) {
                myotus$prevUpgradeItems.put(slot, slot.getItem().copy());
            }
            myotus$dispatchUpgradeOpen();
        }
    }

    @Inject(method = "broadcastChanges", at = @At("HEAD"))
    private void myotus$onBroadcastChanges(CallbackInfo ci) {
        if (this.getPlayer().level().isClientSide()) return;
        myotus$checkUpgradeSlotChanges();
        myotus$dispatchUpgradeTick();
    }

    @Unique
    private void myotus$checkUpgradeSlotChanges() {
        MEStorageMenu menu = (MEStorageMenu) (Object) this;
        for (Slot slot : this.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT)) {
            ItemStack prevStack = myotus$prevUpgradeItems.getOrDefault(slot, ItemStack.EMPTY);
            ItemStack nowStack = slot.getItem();

            if (ItemStack.isSameItemSameComponents(prevStack, nowStack)) {
                continue;
            }

            // 이전 카드 제거 → close 이벤트
            if (!prevStack.isEmpty() && prevStack.getItem() instanceof ITerminalUpgradeCard oldCard) {
                oldCard.onTerminalClose(menu, prevStack.copy());
            }
            // 새 카드 삽입 → open 이벤트
            if (!nowStack.isEmpty() && nowStack.getItem() instanceof ITerminalUpgradeCard newCard) {
                newCard.onTerminalOpen(menu, nowStack.copy());
            }

            myotus$prevUpgradeItems.put(slot, nowStack.copy());
        }
    }

    @Unique
    private void myotus$addCustomViewCellSlots(ITerminalHost host) {
        GenericStackInv stackInv = new GenericStackInv(null, GenericStackInv.Mode.CONFIG_TYPES, 5);
        ConfigMenuInventory menuInv = new ConfigMenuInventory(stackInv);
        for (int i = 0; i < 5; i++) {
            var slot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.VIEW_CELL, menuInv, i);
            this.addSlot(slot, SlotSemantics.VIEW_CELL);
        }
    }

    @Unique
    private void myotus$addCustomUpgradeSlots(ITerminalHost host) {
        // Placed terminal parts keep upgrades in the part NBT so AE2 can drop them when removed.
        // Item-backed terminals keep the previous per-player storage.
        AppEngInternalInventory upgradeInv;
        if (host instanceof TerminalUpgradeHost upgradeHost) {
            upgradeInv = upgradeHost.myotus$getUpgradeInventory();
        } else {
            upgradeInv = (this.getPlayer() instanceof ServerPlayer serverPlayer)
                    ? new PlayerUpgradeContainer(serverPlayer, TerminalUpgradeStorageKey.of(host))
                    : new AppEngInternalInventory(null, PlayerUpgradeContainer.SIZE, 1, TerminalUpgradeSlotFilter.INSTANCE);
        }

        for (int i = 0; i < PlayerUpgradeContainer.SIZE; i++) {
            var slot = new AppEngSlot(upgradeInv, i);
            this.addSlot(slot, MyoSlotSemantics.MYO_UPGRADE_SLOT);
        }
    }

    @Unique
    private void myotus$dispatchUpgradeOpen() {
        MEStorageMenu menu = (MEStorageMenu) (Object) this;
        for (Slot slot : this.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT)) {
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty() && stack.getItem() instanceof ITerminalUpgradeCard card) {
                card.onTerminalOpen(menu, stack);
            }
        }
    }

    @Unique
    private void myotus$dispatchUpgradeTick() {
        MEStorageMenu menu = (MEStorageMenu) (Object) this;
        for (Slot slot : this.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT)) {
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty() && stack.getItem() instanceof ITerminalUpgradeCard card) {
                card.onTerminalTick(menu, stack);
            }
        }
    }
}
