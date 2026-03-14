package me.myogoo.myotus.init;

import me.myogoo.myotus.Myotus;
import me.myogoo.myotus.client.KeyBindings;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = Myotus.MODID, value = Dist.CLIENT)
public class MyoKeyBinding {
    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.OPEN_TERMINAL_SETTING);
    }
}
