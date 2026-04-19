package me.myogoo.myotus.menu;

import appeng.api.inventories.InternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.world.item.ItemStack;

/**
 * ITerminalUpgradeCard를 구현한 아이템만 업그레이드 슬롯에 삽입을 허용하는 필터.
 * 외부 모드 아이템이 로드되지 않은 경우 자연스럽게 false를 반환합니다.
 */
public class TerminalUpgradeSlotFilter implements IAEItemFilter {

    public static final TerminalUpgradeSlotFilter INSTANCE = new TerminalUpgradeSlotFilter();

    private TerminalUpgradeSlotFilter() {}

    @Override
    public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
        return TerminalUpgradeHelper.canInsertUpgrade(inv, slot, stack);
    }

}
