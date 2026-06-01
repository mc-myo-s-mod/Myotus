package me.myogoo.myotus.item;

import appeng.menu.me.common.MEStorageMenu;
import me.myogoo.myotus.Myotus;
import me.myogoo.myotus.api.ITerminalUpgradeCard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 테스트용 업그레이드 카드: 터미널 콜백이 호출될 때마다 다이아몬드 1개를 지급합니다.
 */
public class MyotusUpgradeCardItem extends Item implements ITerminalUpgradeCard {

    public MyotusUpgradeCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public void onTerminalOpen(MEStorageMenu menu, ItemStack stack) {
        giveDiamond(menu);
    }

    @Override
    public void onTerminalTick(MEStorageMenu menu, ItemStack stack) {
        giveDiamond(menu);
    }

    @Override
    public void onTerminalClose(MEStorageMenu menu, ItemStack stack) {
        giveDiamond(menu);
    }

    @Override
    public boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return Myotus.DEV_MODE && super.isEnabled(enabledFeatures);
    }

    private static void giveDiamond(MEStorageMenu menu) {
        if (Myotus.DEV_MODE && menu.getPlayer() instanceof ServerPlayer player) {
            player.getInventory().add(new ItemStack(Items.DIAMOND));
        }
    }
}
