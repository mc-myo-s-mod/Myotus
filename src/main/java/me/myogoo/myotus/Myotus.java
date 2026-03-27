package me.myogoo.myotus;

import com.mojang.logging.LogUtils;

import me.myogoo.myotus.api.annotation.itemList.emi.EMI;
import me.myogoo.myotus.api.annotation.itemList.jei.JEI;
import me.myogoo.myotus.api.annotation.itemList.rei.REI;
import me.myogoo.myotus.api.annotation.myomods.AE2FCT;
import me.myogoo.myotus.api.annotation.myomods.AE2TB;
import me.myogoo.myotus.api.annotation.wt.AE2WTLib;
import me.myogoo.myotus.api.MyotusAPI;
import me.myogoo.myotus.impl.MyotusAPIImpl;
import me.myogoo.myotus.init.MyoCondition;
import me.myogoo.myotus.init.MyoCreativeModeTabs;
import me.myogoo.myotus.init.MyoItems;
import me.myogoo.myotus.init.MyoConfig;
import me.myogoo.myotus.init.MyotusConfigTab;
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

    private final IEventBus modEventBus;

    public Myotus(IEventBus modEventBus, ModContainer modContainer) {
        this.modEventBus = modEventBus;
        MyotusAPI._setInstance(MyotusAPIImpl.INSTANCE);

        MyotusAPI.modRegistrar()
                .registerLoadableMod(JEI.class, "jei")
                .registerLoadableMod(EMI.class, "emi")
                .registerLoadableMod(REI.class, "roughlyenoughitems")
                .registerLoadableMod(AE2WTLib.class, "ae2wtlib")
                .registerLoadableMod(AE2FCT.class, "ae2fct")
                .registerLoadableMod(AE2TB.class, "ae2tb");
        modEventBus.addListener(this::commonSetup);
        MyoCondition.REGISTER.register(modEventBus);
        MyoCreativeModeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        MyoItems.ITEMS.register(modEventBus);
        MyoConfig.initialize(modContainer);
    }


    public void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(MyotusConfigTab::initialize);
    }

    public static ResourceLocation makeId(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
