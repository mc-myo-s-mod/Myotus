package me.myogoo.myotus.init;

import me.myogoo.myotus.Myotus;
import me.myogoo.myotus.item.DiamondUpgradeCardItem;
import net.minecraft.world.item.Item;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MyoItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Myotus.MODID);

    public static final DeferredItem<DiamondUpgradeCardItem> DIAMOND_UPGRADE_CARD = FMLLoader.isProduction()
            ? null
            : ITEMS.register("diamond_upgrade_card",
                    () -> new DiamondUpgradeCardItem(new Item.Properties().stacksTo(1)));
}
