package me.myogoo.myotus.api.datagen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.myogoo.myotus.api.MyotusAPI;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;

/**
 * Condition that evaluates to {@code true} when a Myotus-registered mod
 * integration is active.
 *
 * <p>This condition is serialized with the {@code active_mod} field.</p>
 *
 * <p>Example JSON:</p>
 * <pre>{@code
 * {
 *   "type": "myotus:myo_mod",
 *   "active_mod": "ae2wtlib"
 * }
 * }</pre>
 */
public class MyoModCondition implements ICondition {
    public static final MapCodec<MyoModCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("active_mod").forGetter(MyoModCondition::getModId))
            .apply(instance, MyoModCondition::new));

    private final String modId;

    /**
     * Creates a condition that targets a registered mod ID.
     *
     * @param modId registered Myotus integration ID
     */
    public MyoModCondition(String modId) {
        this.modId = modId;
    }

    @Override
    public boolean test(ICondition.IContext context) {
        return MyotusAPI.modIntegrationManager().isLoaded(modId);
    }

    @Override
    public @NotNull MapCodec<? extends ICondition> codec() {
        return CODEC;
    }

    /**
     * Returns the mod ID serialized by this condition.
     *
     * @return registered integration ID
     */
    public String getModId() {
        return modId;
    }
}
