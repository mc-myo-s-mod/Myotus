package me.myogoo.myotus.mixin;

import java.util.List;

import me.myogoo.myotus.menu.MyoSlotSemantics;
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

    private MEStorageMenuMixin(MenuType<?> menuType, int id, Inventory playerInventory, Object host) {
        super(menuType, id, playerInventory, host);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lappeng/api/storage/ITerminalHost;Z)V", at = @At("TAIL"))
    private void myocertus$ensureSidePanelSlots(MenuType<?> menuType, int id, Inventory ip, ITerminalHost host,
            boolean bindInventory, CallbackInfo ci) {

        // 만약 host가 IViewCellStorage를 구현하지 않아 viewCellSlots가 비어있다면,
        // TerminalSidePanel에서 사용할 5개의 가상 슬롯을 강제로 추가합니다.
        // 이때, 실제 저장소는 없으므로 ConfigMenuInventory 등을 활용하거나,
        // AE2의 RestrictedInputSlot이 요구하는 인벤토리를 가짜로 생성합니다.

        if (this.viewCellSlots == null || this.viewCellSlots.isEmpty()) {
            myocertus$addCustomViewCellSlots(host);
        }

        // 업그레이드 슬롯 확인
        if (this.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT).isEmpty()) {
            myocertus$addCustomUpgradeSlots(host);
        }
    }

    @Unique
    private void myocertus$addCustomViewCellSlots(ITerminalHost host) {
        // 가상의 5칸 인벤토리 생성 (ViewCell 용)
        GenericStackInv stackInv = new GenericStackInv(null, GenericStackInv.Mode.CONFIG_TYPES, 5);
        ConfigMenuInventory menuInv = new ConfigMenuInventory(stackInv);
        for (int i = 0; i < 5; i++) {
            var slot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.VIEW_CELL, menuInv, i);
            this.addSlot(slot, SlotSemantics.VIEW_CELL);
        }
    }

    @Unique
    private void myocertus$addCustomUpgradeSlots(ITerminalHost host) {
        // 가상의 5칸 인벤토리 생성 (Upgrade 용)
        GenericStackInv stackInv = new GenericStackInv(null, GenericStackInv.Mode.CONFIG_TYPES, 5);
        ConfigMenuInventory menuInv = new ConfigMenuInventory(stackInv);
        for (int i = 0; i < 5; i++) {
            var slot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, menuInv, i) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            };
            slot.setStackLimit(1);
            this.addSlot(slot, MyoSlotSemantics.MYO_UPGRADE_SLOT);
        }
    }
}
