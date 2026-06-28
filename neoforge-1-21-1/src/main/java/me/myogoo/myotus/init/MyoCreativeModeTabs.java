package me.myogoo.myotus.init;

import me.myogoo.myotus.client.TranslateKey;
import appeng.core.definitions.AEItems;
import me.myogoo.myotus.impl.CreativeTabManager;
import me.myogoo.myotus.Myotus;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MyoCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Myotus.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MYOTUS =
            CREATIVE_MODE_TABS.register("myotus", () -> CreativeModeTab.builder()
                    .title(Component.translatable(TranslateKey.ITEM_GROUP_MYOTUS.key()))
                    .icon(MyoItems.MYOTUS_UPGRADE_CARD.get()::getDefaultInstance)
                    .displayItems((parameters, output) -> {
                        CreativeTabManager.INSTANCE.populate(output);
                    })
                    .build());

    private MyoCreativeModeTabs() {
    }
}
