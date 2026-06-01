package me.myogoo.myotus.init;

import appeng.core.definitions.AEItems;
import me.myogoo.myotus.Myotus;
import me.myogoo.myotus.api.wt.AddTerminalEvent;
import me.myogoo.myotus.impl.CreativeTabManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class MyoCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Myotus.MODID);
    private static final ResourceKey<CreativeModeTab> AE2WTLIB_TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath("ae2wtlib", "main"));

    public static final RegistryObject<CreativeModeTab> MYOTUS =
            CREATIVE_MODE_TABS.register("myotus", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.myotus"))
                    .icon(AEItems.ADVANCED_CARD::stack)
                    .displayItems((parameters, output) -> CreativeTabManager.INSTANCE.populate(output))
                    .build());

    public static void addAE2WTLibTerminals(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(AE2WTLIB_TAB)) {
            AddTerminalEvent.addCreativeTabTerminals(event);
        }
    }

    private MyoCreativeModeTabs() {
    }
}
