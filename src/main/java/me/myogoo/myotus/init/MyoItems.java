package me.myogoo.myotus.init;

import me.myogoo.myotus.Myotus;
import me.myogoo.myotus.api.MyotusAPI;
import me.myogoo.myotus.item.DiamondUpgradeCardItem;
import net.minecraft.world.item.Item;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class MyoItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Myotus.MODID);

    public static final DeferredItem<DiamondUpgradeCardItem> DIAMOND_UPGRADE_CARD = registerItem(
            "diamond_upgrade_card",() -> new DiamondUpgradeCardItem(new Item.Properties().stacksTo(16)));


    private static <T extends Item> DeferredItem<T> registerItem(String name, Supplier<T> item) {
        if(FMLLoader.isProduction()) return null;
        DeferredItem<T> registeredItem = ITEMS.register(name, item);
        MyotusAPI.creativeTabRegistrar().creativeTabItem(registeredItem);
        return registeredItem;
    }
}
