package me.myogoo.myotus;

import com.mojang.logging.LogUtils;

import me.myogoo.myotus.api.MyotusAPI;
import me.myogoo.myotus.data.MyotusDataGenerators;
import me.myogoo.myotus.gametest.MyoExperienceGameTests;
import me.myogoo.myotus.impl.MyotusAPIImpl;
import me.myogoo.myotus.init.MyoBlocks;
import me.myogoo.myotus.init.MyoCondition;
import me.myogoo.myotus.init.MyoConfig;
import me.myogoo.myotus.init.MyoCreativeModeTabs;
import me.myogoo.myotus.init.MyoItems;
import me.myogoo.myotus.platform.AnnotationScanData;
import me.myogoo.myotus.platform.mod.NeoForgeModList;
import me.myogoo.myotus.util.mod.ModIntegrationManager;
import me.myogoo.myotus.util.mod.MyoModVersionMismatchException;
import me.myogoo.myotus.util.reflect.SafeClass;
import me.myogoo.myotus.util.reflect.annotation.AnnotationScanner;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingException;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;

@Mod(Myotus.MODID)
public class Myotus {
    public static final String MODID = "myotus";
    private static final String VERSION_MISMATCH_LOADING_ERROR =
            "The {0} must satisfy version range {1}. Current version: {2}";
    public static final boolean DEFAULT_DEV_MODE = !FMLEnvironment.isProduction();
    public static boolean DEV_MODE = DEFAULT_DEV_MODE;
    public static final Logger LOGGER = LogUtils.getLogger();

    public Myotus(IEventBus modEventBus, ModContainer modContainer) {
        MyotusAPI._setInstance(MyotusAPIImpl.INSTANCE);
        SafeClass.setDedicatedServer(FMLEnvironment.getDist().isDedicatedServer());
        AnnotationScanner.setAnnotationProvider(AnnotationScanData::getAnnotations);
        try {
            ModIntegrationManager.setModList(NeoForgeModList.INSTANCE);
        } catch (MyoModVersionMismatchException e) {
            throw new ModLoadingException(
                    ModLoadingIssue.error(
                            VERSION_MISMATCH_LOADING_ERROR,
                            e.getDisplayModName(),
                            e.getVersionRange(),
                            e.getModVersion())
                            .withCause(e)
                            .withAffectedMod(modContainer.getModInfo()));
        }
        MyoCondition.REGISTER.register(modEventBus);
        MyoCreativeModeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        MyoBlocks.BLOCKS.register(modEventBus);
        MyoItems.ITEMS.register(modEventBus);
        modEventBus.addListener(MyotusDataGenerators::onGatherData);
        modEventBus.addListener((RegisterEvent event) -> {
            if (event.getRegistryKey() == Registries.TEST_INSTANCE_TYPE) {
                event.register(Registries.TEST_INSTANCE_TYPE, MyoExperienceGameTests.TEST_INSTANCE_TYPE_ID,
                        () -> MyoExperienceGameTests.CODEC);
            }
        });
        modEventBus.addListener((RegisterGameTestsEvent event) ->
                MyoExperienceGameTests.registerAll(event::registerTest));
        MyoConfig.initialize(modContainer);
    }

    public static Identifier makeId(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
