package me.myogoo.myotus;

import com.mojang.logging.LogUtils;

import me.myogoo.myotus.api.annotation.itemList.emi.EMI;
import me.myogoo.myotus.api.annotation.itemList.jei.JEI;
import me.myogoo.myotus.api.annotation.itemList.rei.REI;
import me.myogoo.myotus.api.annotation.myomods.AE2FCT;
import me.myogoo.myotus.api.annotation.myomods.AE2TB;
import me.myogoo.myotus.api.annotation.wt.AE2WTLib;
import me.myogoo.myotus.init.MyoConfig;
import me.myogoo.myotus.init.MyotusConfigTab;
import me.myogoo.myotus.util.mod.ModIntegrationManager;
import net.minecraft.resources.ResourceLocation;
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
        MyoConfig.initialize(modContainer);
        ModIntegrationManager.put(JEI.class, "jei");
        ModIntegrationManager.put(EMI.class, "emi");
        ModIntegrationManager.put(REI.class, "roughlyenoughitems");
        ModIntegrationManager.put(AE2WTLib.class, "ae2wtlib");
        ModIntegrationManager.put(AE2FCT.class, "ae2fct");
        ModIntegrationManager.put(AE2TB.class,  "ae2tb");
        modEventBus.addListener(this::commonSetup);
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModIntegrationManager.initialize();
            MyotusConfigTab.initialize();
        });
    }

    public static ResourceLocation makeId(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}