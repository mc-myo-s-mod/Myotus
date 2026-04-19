package me.myogoo.myotus.init;

import me.myogoo.myotus.Myotus;
import me.myogoo.myotus.api.MyotusAPI;
import me.myogoo.myotus.item.DiamondUpgradeCardItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class MyoItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Myotus.MODID);

    public static final RegistryObject<DiamondUpgradeCardItem> DIAMOND_UPGRADE_CARD = registerDevItem(
            "diamond_upgrade_card", () -> new DiamondUpgradeCardItem(new Item.Properties().stacksTo(1)));

    private static <T extends Item> RegistryObject<T> registerDevItem(String name, Supplier<T> item) {
        RegistryObject<T> registeredItem = ITEMS.register(name, item);
        if (Myotus.DEV_MODE) {
            MyotusAPI.creativeTabRegistrar().creativeTabItem(registeredItem);
        }
        return registeredItem;
    }
}
