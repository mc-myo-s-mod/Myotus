package me.myogoo.myotus.client;

import me.myogoo.myotus.Myotus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@Mod(value = Myotus.MODID, dist = Dist.CLIENT)
public class MyotusClient {
    public MyotusClient(IEventBus modEventBus) {
        modEventBus.addListener(MyotusConfigTab::onClientSetup);
        modEventBus.addListener(MyotusClient::registerKeyMappings);
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.OPEN_TERMINAL_SETTING);
        event.register(KeyBindings.TOGGLE_UPGRADE_TERMINAL_PANEL);
    }
}
