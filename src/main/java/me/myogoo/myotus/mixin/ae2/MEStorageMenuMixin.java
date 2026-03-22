package me.myogoo.myotus.mixin.ae2;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import appeng.client.gui.Icon;
import appeng.menu.slot.AppEngSlot;
import appeng.util.inv.AppEngInternalInventory;
import me.myogoo.myotus.api.ITerminalUpgradeCard;
import me.myogoo.myotus.menu.MyoSlotSemantics;
import me.myogoo.myotus.menu.PlayerUpgradeContainer;
import me.myogoo.myotus.menu.TerminalUpgradeStorageKey;
import me.myogoo.myotus.menu.TerminalUpgradeSlotFilter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
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
    private final Map<Slot, Item> myocertus$prevUpgradeItems = new IdentityHashMap<>();

    private MEStorageMenuMixin(MenuType<?> menuType, int id, Inventory playerInventory, Object host) {
        super(menuType, id, playerInventory, host);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lappeng/api/storage/ITerminalHost;Z)V", at = @At("TAIL"))
    private void myocertus$ensureSidePanelSlots(MenuType<?> menuType, int id, Inventory ip, ITerminalHost host,
            boolean bindInventory, CallbackInfo ci) {

        if (this.viewCellSlots == null || this.viewCellSlots.isEmpty()) {
            myocertus$addCustomViewCellSlots(host);
        }

        if (this.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT).isEmpty()) {
            myocertus$addCustomUpgradeSlots(host);
        }

        // 이전 슬롯 상태 초기화 (삽입/제거 감지용, broadcastChanges에서 비교)
        if (!this.getPlayer().level().isClientSide()) {
            for (Slot slot : this.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT)) {
                myocertus$prevUpgradeItems.put(slot, slot.getItem().getItem());
            }
            myocertus$dispatchUpgradeOpen();
        }
    }

    @Override
    public void removed(Player player) {
        if (!player.level().isClientSide()) {
            myocertus$dispatchUpgradeClose();
        }
        super.removed(player);
    }

    @Inject(method = "broadcastChanges", at = @At("HEAD"))
    private void myocertus$onBroadcastChanges(CallbackInfo ci) {
        if (this.getPlayer().level().isClientSide()) return;
        myocertus$checkUpgradeSlotChanges();
        myocertus$dispatchUpgradeTick();
    }

    @Unique
    private void myocertus$checkUpgradeSlotChanges() {
        MEStorageMenu menu = (MEStorageMenu) (Object) this;
        for (Slot slot : this.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT)) {
            Item prevItem = myocertus$prevUpgradeItems.get(slot);
            ItemStack nowStack = slot.getItem();
            Item nowItem = nowStack.getItem();

            if (prevItem == nowItem) continue;

            // 이전 카드 제거 → close 이벤트
            if (prevItem instanceof ITerminalUpgradeCard oldCard) {
                oldCard.onTerminalClose(menu, new ItemStack(prevItem));
            }
            // 새 카드 삽입 → open 이벤트
            if (!nowStack.isEmpty() && nowItem instanceof ITerminalUpgradeCard newCard) {
                newCard.onTerminalOpen(menu, nowStack);
            }

            myocertus$prevUpgradeItems.put(slot, nowItem);
        }
    }

    @Unique
    private void myocertus$addCustomViewCellSlots(ITerminalHost host) {
        GenericStackInv stackInv = new GenericStackInv(null, GenericStackInv.Mode.CONFIG_TYPES, 5);
        ConfigMenuInventory menuInv = new ConfigMenuInventory(stackInv);
        for (int i = 0; i < 5; i++) {
            var slot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.VIEW_CELL, menuInv, i);
            this.addSlot(slot, SlotSemantics.VIEW_CELL);
        }
    }

    @Unique
    private void myocertus$addCustomUpgradeSlots(ITerminalHost host) {
        // 서버 측: 플레이어 persistentData에 저장되는 컨테이너 사용 (GUI 닫아도 유지)
        // 클라이언트 측: 서버에서 슬롯 내용이 자동 동기화되므로 빈 컨테이너로 충분
        AppEngInternalInventory upgradeInv = (this.getPlayer() instanceof ServerPlayer serverPlayer)
                ? new PlayerUpgradeContainer(serverPlayer, TerminalUpgradeStorageKey.of(host))
                : new AppEngInternalInventory(null, PlayerUpgradeContainer.SIZE, 1, TerminalUpgradeSlotFilter.INSTANCE);

        for (int i = 0; i < PlayerUpgradeContainer.SIZE; i++) {
            var slot = new AppEngSlot(upgradeInv, i);
            slot.setIcon(Icon.BACKGROUND_UPGRADE);
            this.addSlot(slot, MyoSlotSemantics.MYO_UPGRADE_SLOT);
        }
    }

    @Unique
    private void myocertus$dispatchUpgradeOpen() {
        MEStorageMenu menu = (MEStorageMenu) (Object) this;
        for (Slot slot : this.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT)) {
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty() && stack.getItem() instanceof ITerminalUpgradeCard card) {
                card.onTerminalOpen(menu, stack);
            }
        }
    }

    @Unique
    private void myocertus$dispatchUpgradeClose() {
        MEStorageMenu menu = (MEStorageMenu) (Object) this;
        for (Slot slot : this.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT)) {
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty() && stack.getItem() instanceof ITerminalUpgradeCard card) {
                card.onTerminalClose(menu, stack);
            }
        }
    }

    @Unique
    private void myocertus$dispatchUpgradeTick() {
        MEStorageMenu menu = (MEStorageMenu) (Object) this;
        for (Slot slot : this.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT)) {
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty() && stack.getItem() instanceof ITerminalUpgradeCard card) {
                card.onTerminalTick(menu, stack);
            }
        }
    }
}
