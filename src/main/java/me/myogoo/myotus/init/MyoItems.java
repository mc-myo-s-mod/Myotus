package me.myogoo.myotus.init;

import me.myogoo.myotus.Myotus;
import me.myogoo.myotus.api.MyotusAPI;
import me.myogoo.myotus.item.DiamondUpgradeCardItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class MyoItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Myotus.MODID);

    public static final DeferredItem<DiamondUpgradeCardItem> DIAMOND_UPGRADE_CARD = registerDevItem(
            "diamond_upgrade_card", () -> new DiamondUpgradeCardItem(new Item.Properties().stacksTo(1)));


    private static <T extends Item> DeferredItem<T> registerDevItem(String name, Supplier<T> item) {
        DeferredItem<T> registeredItem = ITEMS.register(name, item);
        if (Myotus.DEV_MODE) {
            MyotusAPI.creativeTabRegistrar().creativeTabItem(registeredItem);
        }
        return registeredItem;
    }
}
