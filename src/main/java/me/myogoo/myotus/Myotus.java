package me.myogoo.myotus;

import com.mojang.logging.LogUtils;

import me.myogoo.myotus.api.annotation.itemList.emi.EMI;
import me.myogoo.myotus.api.annotation.itemList.jei.JEI;
import me.myogoo.myotus.api.annotation.itemList.rei.REI;
import me.myogoo.myotus.api.annotation.myomods.AE2FCT;
import me.myogoo.myotus.api.annotation.myomods.AE2TB;
import me.myogoo.myotus.api.annotation.wt.AE2WTLib;
import me.myogoo.myotus.impl.MyotusAPIImpl;
import me.myogoo.myotus.api.MyotusAPI;
import me.myogoo.myotus.init.MyoCondition;
import me.myogoo.myotus.init.MyoConfig;
import me.myogoo.myotus.init.MyoCreativeModeTabs;
import me.myogoo.myotus.init.MyoItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Myotus.MODID)
public class Myotus {
    public static final String MODID = "myotus";
    public static final Logger LOGGER = LogUtils.getLogger();

    private final IEventBus modEventBus;

    public Myotus() {
        this.modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MyotusAPI._setInstance(MyotusAPIImpl.INSTANCE);

        MyotusAPI.modRegistrar()
                .registerLoadableMod(JEI.class, "jei")
                .registerLoadableMod(EMI.class, "emi")
                .registerLoadableMod(REI.class, "roughlyenoughitems")
                .registerLoadableMod(AE2WTLib.class, "ae2wtlib")
                .registerLoadableMod(AE2FCT.class, "ae2fct")
                .registerLoadableMod(AE2TB.class, "ae2tb");
        MyoCreativeModeTabs.CREATIVE_MODE_TABS.register(this.modEventBus);
        MyoItems.ITEMS.register(this.modEventBus);
        MyoCondition.register();
        MyoConfig.initialize();
    }

    public static ResourceLocation makeId(String path) {
        return new ResourceLocation(MODID, path);
    }
}
