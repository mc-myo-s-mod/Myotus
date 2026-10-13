package me.myogoo.myotus.gametest;

import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod("myotus_gametest")
public final class MyotusGameTestMod {
    public MyotusGameTestMod(IEventBus modEventBus) {
        modEventBus.addListener((RegisterEvent event) -> {
            if (event.getRegistryKey() == Registries.TEST_INSTANCE_TYPE) {
                event.register(Registries.TEST_INSTANCE_TYPE, MyoExperienceGameTests.TEST_INSTANCE_TYPE_ID,
                        () -> MyoExperienceGameTests.CODEC);
            }
        });
        modEventBus.addListener((RegisterGameTestsEvent event) ->
                MyoExperienceGameTests.registerAll(event::registerTest));
    }
}
