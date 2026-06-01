package me.myogoo.myotus.init;

import com.mojang.serialization.MapCodec;
import me.myogoo.myotus.Myotus;
import me.myogoo.myotus.api.datagen.MyoDevModeCondition;
import me.myogoo.myotus.api.datagen.MyoModCondition;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class MyoCondition {
    public static final DeferredRegister<MapCodec<? extends ICondition>> REGISTER =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, Myotus.MODID);

    public static final Supplier<MapCodec<MyoModCondition>> ET_MOD_LOAD =
            REGISTER.register("mod_condition", () -> MyoModCondition.CODEC);

    public static final Supplier<MapCodec<MyoDevModeCondition>> DEV_MODE =
            REGISTER.register("dev", () -> MyoDevModeCondition.CODEC);

}
