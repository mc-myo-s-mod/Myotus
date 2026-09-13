package me.myogoo.myotus.api.datagen;

import com.mojang.serialization.MapCodec;
import me.myogoo.myotus.Myotus;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;

/**
 * Condition that evaluates to {@code true} outside production builds.
 *
 * <p>Example JSON:</p>
 * <pre>{@code
 * {
 *   "type": "myotus:dev"
 * }
 * }</pre>
 */
public final class MyoDevModeCondition implements ICondition {
    public static final MyoDevModeCondition INSTANCE = new MyoDevModeCondition();
    public static final MapCodec<MyoDevModeCondition> CODEC = MapCodec.unit(INSTANCE).stable();

    private MyoDevModeCondition() {
    }

    @Override
    public boolean test(ICondition.IContext context) {
        return Myotus.DEV_MODE;
    }

    @Override
    public @NotNull MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
