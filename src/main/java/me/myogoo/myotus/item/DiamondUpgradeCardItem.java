package me.myogoo.myotus.item;

import appeng.menu.me.common.MEStorageMenu;
import me.myogoo.myotus.api.ITerminalUpgradeCard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 테스트용 업그레이드 카드: 터미널을 열 때마다 다이아몬드 1개를 지급합니다.
 */
public class DiamondUpgradeCardItem extends Item implements ITerminalUpgradeCard {

    public DiamondUpgradeCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public void onTerminalOpen(MEStorageMenu menu, ItemStack stack) {
        if (menu.getPlayer() instanceof ServerPlayer player) {
            player.getInventory().add(new ItemStack(Items.DIAMOND));
        }
    }
}
