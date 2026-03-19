package me.myogoo.myotus.api.datagen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.myogoo.myotus.api.MyotusAPI;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;

public class MyoModCondition implements ICondition{
    public static final MapCodec<MyoModCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("active_mod").forGetter(MyoModCondition::getModId)).apply(instance, MyoModCondition::new));

    @Nullable
    private Class<? extends Annotation> modId = null;

    public MyoModCondition(String modId) {
        this.modId = MyotusAPI.get().modIntegrationManager().getAnnotationClass(modId);
    }

    @Override
    public boolean test(ICondition.IContext context) {
        if (this.modId == null)
            return false;
        return MyotusAPI.get().modIntegrationManager().isLoaded(this.modId);
    }

    @Override
    public @NotNull MapCodec<? extends ICondition> codec() {
        return CODEC;
    }

    public String getModId() {
        if (this.modId == null)
            return "null";
        return this.modId.getSimpleName();
    }

}
