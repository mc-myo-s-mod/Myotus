package me.myogoo.myotus.client;

import me.myogoo.myotus.Myotus;
import me.myogoo.myotus.api.MyotusAPI;
import me.myogoo.myotus.client.dev.TerminalModuleTestWidget;
import me.myogoo.myotus.init.MyoItems;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@Mod(value = Myotus.MODID, dist = Dist.CLIENT)
public class MyotusClient {
    public MyotusClient(IEventBus modEventBus) {
        modEventBus.addListener(MyotusConfigTab::onClientSetup);
        modEventBus.addListener(MyotusClient::registerKeyMappings);
        modEventBus.addListener(MyotusClient::registerTestModule);
    }

    private static void registerTestModule(FMLClientSetupEvent event) {
        if (Myotus.DEV_MODE) {
            event.enqueueWork(() -> MyotusAPI.Client.registerTerminalModule(MyoItems.MYOTUS_UPGRADE_CARD.get(),
                    (screen, stack) -> new TerminalModuleTestWidget()));
        }
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.TRANSFER_TO_MODULE);
        event.register(KeyBindings.OPEN_TERMINAL_SETTING);
        event.register(KeyBindings.TOGGLE_UPGRADE_TERMINAL_PANEL);
    }
}
