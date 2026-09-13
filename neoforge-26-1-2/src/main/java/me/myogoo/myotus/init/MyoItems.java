package me.myogoo.myotus.init;

import me.myogoo.myotus.Myotus;
import me.myogoo.myotus.api.MyotusAPI;
import me.myogoo.myotus.item.ChargedEnderPearlItem;
import me.myogoo.myotus.item.MyotusUpgradeCardItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;

public class MyoItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Myotus.MODID);

    public static final DeferredItem<Item> COMPAT_PROCESSOR = registerItem("compat_processor", Item::new);
    public static final DeferredItem<Item> PRINTED_COMPAT_PROCESSOR = registerItem("printed_compat_processor", Item::new);
    public static final DeferredItem<Item> COMPAT_PRESS = registerItem("compat_press", Item::new);
    public static final DeferredItem<ChargedEnderPearlItem> CHARGED_ENDER_PEARL = registerItem("charged_ender_pearl",
            properties -> new ChargedEnderPearlItem(properties.stacksTo(16)));
    public static final DeferredItem<BlockItem> ENDER_PEARL_BLOCK = registerBlockItem("ender_pearl_block",
            MyoBlocks.ENDER_PEARL_BLOCK);
    public static final DeferredItem<BlockItem> CHARGED_ENDER_PEARL_BLOCK = registerBlockItem(
            "charged_ender_pearl_block", MyoBlocks.CHARGED_ENDER_PEARL_BLOCK);

    public static final DeferredItem<MyotusUpgradeCardItem> MYOTUS_UPGRADE_CARD = registerDevItem(
            "myotus_upgrade_card", properties -> new MyotusUpgradeCardItem(properties.stacksTo(1)));


    private static <T extends Item> DeferredItem<T> registerItem(String name, Function<Item.Properties, T> item) {
        DeferredItem<T> registeredItem = ITEMS.registerItem(name, item);
        MyotusAPI.creativeTabs().registerCreativeTabItem(registeredItem);
        return registeredItem;
    }

    private static DeferredItem<BlockItem> registerBlockItem(String name, Supplier<? extends Block> block) {
        DeferredItem<BlockItem> registeredItem = ITEMS.registerSimpleBlockItem(name, block);
        MyotusAPI.creativeTabs().registerCreativeTabItem(registeredItem);
        return registeredItem;
    }

    private static <T extends Item> DeferredItem<T> registerDevItem(String name, Function<Item.Properties, T> item) {
        DeferredItem<T> registeredItem = ITEMS.registerItem(name, item);
        if (Myotus.DEV_MODE) {
            MyotusAPI.creativeTabs().registerCreativeTabItem(registeredItem);
        }
        return registeredItem;
    }
}
