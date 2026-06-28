package me.myogoo.myotus.client;

import me.myogoo.myotus.Myotus;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = Myotus.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class MyotusClient {
    private MyotusClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(MyotusConfigTab::initialize);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.OPEN_TERMINAL_SETTING);
        event.register(KeyBindings.TOGGLE_UPGRADE_TERMINAL_PANEL);
    }
}
