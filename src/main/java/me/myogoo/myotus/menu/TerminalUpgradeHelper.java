package me.myogoo.myotus.menu;

import appeng.menu.me.common.MEStorageMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 터미널 업그레이드 슬롯에 꽂혀 있는 아이템을 조회하는 유틸리티.
 *
 * 사용 예시:
 * <pre>
 *   // 직접 Item 참조 (같은 모드 내부)
 *   if (TerminalUpgradeHelper.hasUpgrade(menu, MyItems.MY_CARD.get())) { ... }
 *
 *   // ResourceLocation 기반 (외부 모드 카드, 모드 미로드 시 false 반환)
 *   if (TerminalUpgradeHelper.hasUpgrade(menu, ResourceLocation.fromNamespaceAndPath("somemod", "upgrade_card"))) { ... }
 * </pre>
 */
public class TerminalUpgradeHelper {

    private TerminalUpgradeHelper() {}

    /**
     * 현재 설치된 업그레이드 아이템 목록을 반환합니다 (빈 슬롯 제외).
     */
    public static List<ItemStack> getInstalledUpgrades(MEStorageMenu menu) {
        return menu.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT).stream()
                .map(slot -> slot.getItem())
                .filter(stack -> !stack.isEmpty())
                .toList();
    }

    /**
     * 특정 아이템이 업그레이드 슬롯에 설치되어 있는지 확인합니다.
     */
    public static boolean hasUpgrade(MEStorageMenu menu, Item upgradeItem) {
        return menu.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT).stream()
                .anyMatch(slot -> slot.getItem().is(upgradeItem));
    }

    /**
     * ResourceLocation으로 업그레이드 카드를 확인합니다.
     * 해당 모드가 로드되지 않아 아이템이 존재하지 않으면 false를 반환합니다.
     */
    public static boolean hasUpgrade(MEStorageMenu menu, ResourceLocation itemId) {
        return BuiltInRegistries.ITEM.getOptional(itemId)
                .map(item -> hasUpgrade(menu, item))
                .orElse(false);
    }

    /**
     * 특정 업그레이드의 설치 개수를 반환합니다.
     */
    public static int countUpgrade(MEStorageMenu menu, Item upgradeItem) {
        return (int) menu.getSlots(MyoSlotSemantics.MYO_UPGRADE_SLOT).stream()
                .filter(slot -> slot.getItem().is(upgradeItem))
                .count();
    }

    /**
     * ResourceLocation으로 업그레이드 설치 개수를 확인합니다.
     * 해당 모드가 로드되지 않으면 0을 반환합니다.
     */
    public static int countUpgrade(MEStorageMenu menu, ResourceLocation itemId) {
        return BuiltInRegistries.ITEM.getOptional(itemId)
                .map(item -> countUpgrade(menu, item))
                .orElse(0);
    }
}
