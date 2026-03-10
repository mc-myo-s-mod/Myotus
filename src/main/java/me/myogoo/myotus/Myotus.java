package me.myogoo.myotus;

import com.mojang.logging.LogUtils;

import me.myogoo.myotus.api.ConfigTab;
import me.myogoo.myotus.api.annotation.itemList.emi.EMI;
import me.myogoo.myotus.api.annotation.itemList.jei.JEI;
import me.myogoo.myotus.api.annotation.itemList.rei.REI;
import me.myogoo.myotus.api.annotation.wt.AE2WTLib;
import me.myogoo.myotus.integration.ae2.TerminalConfig;
import me.myogoo.myotus.util.mod.ModIntegrationManager;
import me.myogoo.myotus.util.mod.SupportedMods;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(Myotus.MODID)
public class Myotus {
    public static final String MODID = "myotus";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Myotus(IEventBus modEventBus, ModContainer modContainer) {
        SupportedMods.put(JEI.class, "jei");
        SupportedMods.put(EMI.class, "emi");
        SupportedMods.put(REI.class, "roughlyenoughitems");
        SupportedMods.put(AE2WTLib.class, "ae2wtlib");

        modEventBus.addListener(this::commonSetup);
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModIntegrationManager.initialize();

            // 임의의 테스트 탭 추가
            TerminalConfig.registerTab(new ConfigTab(
                    Component.literal("Test Tab"),
                    new ItemStack(Items.APPLE),
                    (widgets, screen) -> {
                        // 탭이 선택되었을 때 배치할 위젯 로직 작성
                    }));
        });
    }

    public static ResourceLocation makeId(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}