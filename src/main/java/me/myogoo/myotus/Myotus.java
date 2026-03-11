package me.myogoo.myotus;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.WidgetContainer;
import appeng.core.definitions.AEParts;
import com.mojang.logging.LogUtils;

import me.myogoo.myotus.api.annotation.itemList.emi.EMI;
import me.myogoo.myotus.api.annotation.itemList.jei.JEI;
import me.myogoo.myotus.api.annotation.itemList.rei.REI;
import me.myogoo.myotus.api.annotation.wt.AE2WTLib;
import me.myogoo.myotus.api.config.ConfigTab;
import me.myogoo.myotus.api.config.ConfigTabProvider;
import me.myogoo.myotus.integration.ae2.TerminalConfig;
import me.myogoo.myotus.util.mod.ModIntegrationManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(Myotus.MODID)
public class Myotus {
    public static final String MODID = "myotus";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Myotus(IEventBus modEventBus, ModContainer modContainer) {
        ModIntegrationManager.put(JEI.class, "jei");
        ModIntegrationManager.put(EMI.class, "emi");
        ModIntegrationManager.put(REI.class, "roughlyenoughitems");
        ModIntegrationManager.put(AE2WTLib.class, "ae2wtlib");

        modEventBus.addListener(this::commonSetup);
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModIntegrationManager.initialize();
            TerminalConfig.registerTab(
                    new ConfigTab(
                            Component.translatable("config.myotus.general"),
                            AEParts.TERMINAL.stack(),
                            "myotus.json",
                            (widgets, screen) -> {
                                widgets.addCheckbox("ttt",Component.translatable("selectServer.select"), () -> {
                                    System.out.println("응디");
                                } );
                            }
                    )
            );
        });
    }

    public static ResourceLocation makeId(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}