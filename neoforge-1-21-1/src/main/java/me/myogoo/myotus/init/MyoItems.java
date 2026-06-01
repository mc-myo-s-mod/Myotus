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

import java.util.function.Supplier;

public class MyoItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Myotus.MODID);

    public static final DeferredItem<Item> COMPAT_PROCESSOR = registerItem("compat_processor",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PRINTED_COMPAT_PROCESSOR = registerItem("printed_compat_processor",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> COMPAT_PRESS = registerItem("compat_press",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<ChargedEnderPearlItem> CHARGED_ENDER_PEARL = registerItem("charged_ender_pearl",
            () -> new ChargedEnderPearlItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<BlockItem> ENDER_PEARL_BLOCK = registerBlockItem("ender_pearl_block",
            MyoBlocks.ENDER_PEARL_BLOCK);
    public static final DeferredItem<BlockItem> CHARGED_ENDER_PEARL_BLOCK = registerBlockItem(
            "charged_ender_pearl_block", MyoBlocks.CHARGED_ENDER_PEARL_BLOCK);

    public static final DeferredItem<MyotusUpgradeCardItem> MYOTUS_UPGRADE_CARD = registerDevItem(
            "myotus_upgrade_card", () -> new MyotusUpgradeCardItem(new Item.Properties().stacksTo(1)));


    private static <T extends Item> DeferredItem<T> registerItem(String name, Supplier<T> item) {
        DeferredItem<T> registeredItem = ITEMS.register(name, item);
        MyotusAPI.creativeTabs().registerCreativeTabItem(registeredItem);
        return registeredItem;
    }

    private static DeferredItem<BlockItem> registerBlockItem(String name, Supplier<? extends Block> block) {
        return registerItem(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static <T extends Item> DeferredItem<T> registerDevItem(String name, Supplier<T> item) {
        DeferredItem<T> registeredItem = ITEMS.register(name, item);
        if (Myotus.DEV_MODE) {
            MyotusAPI.creativeTabs().registerCreativeTabItem(registeredItem);
        }
        return registeredItem;
    }
}
