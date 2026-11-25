package me.myogoo.myotus.client;

import me.myogoo.myotus.Myotus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@Mod(value = Myotus.MODID, dist = Dist.CLIENT)
public class MyotusClient {
    public MyotusClient(IEventBus modEventBus) {
        modEventBus.addListener(MyotusConfigTab::onClientSetup);
        modEventBus.addListener(MyotusClient::registerKeyMappings);
        modEventBus.addListener(MyotusClient::registerSortingIntegrations);
    }

    private static void registerSortingIntegrations(FMLClientSetupEvent event) {
        if (ModList.get().isLoaded("inventoryessentials")) {
            event.enqueueWork(InventoryEssentialsIntegration::register);
        }
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(KeyBindings.CATEGORY);
        event.register(KeyBindings.OPEN_TERMINAL_SETTING);
        event.register(KeyBindings.TOGGLE_UPGRADE_TERMINAL_PANEL);
    }
}
