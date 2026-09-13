package me.myogoo.myotus.api.datagen;

import me.myogoo.myotus.api.annotation.MyoMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.myogoo.myotus.util.MyoLogger;
import me.myogoo.myotus.util.mod.ModIntegrationManager;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;

/**
 * Condition that evaluates to {@code true} when an annotation-discovered
 * Myotus mod integration is active.
 *
 * <p>This condition is serialized with the {@code active_mod} field. The value
 * may be the integration mod ID, {@link MyoMod#alias()},
 * namespace, or display name.</p>
 *
 * <p>Example JSON:</p>
 * <pre>{@code
 * {
 *   "type": "myotus:mod_condition",
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
     * Creates a condition that targets an integration mod ID.
     *
     * @param modId Myotus integration mod ID
     */
    public MyoModCondition(String modId) {
        this.modId = modId;
    }

    @Override
    public boolean test(ICondition.IContext context) {
        if (!ModIntegrationManager.isRegistered(modId)) {
            MyoLogger.warn("Unknown Myotus integration condition '{}'. Register it with @MyoMod or fix active_mod. Treating as false.", modId);
            return false;
        }
        return ModIntegrationManager.isLoaded(modId);
    }

    @Override
    public @NotNull MapCodec<? extends ICondition> codec() {
        return CODEC;
    }

    /**
     * Returns the mod ID serialized by this condition.
     *
     * @return integration mod ID
     */
    public String getModId() {
        return modId;
    }
}
