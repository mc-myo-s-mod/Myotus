package me.myogoo.myotus.api.datagen;

import com.google.gson.JsonObject;
import me.myogoo.myotus.Myotus;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

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
    public static final ResourceLocation ID = Myotus.makeId("dev");
    public static final MyoDevModeCondition INSTANCE = new MyoDevModeCondition();

    private MyoDevModeCondition() {
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return Myotus.DEV_MODE;
    }

    public static final class Serializer implements IConditionSerializer<MyoDevModeCondition> {
        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {
        }

        @Override
        public void write(JsonObject json, MyoDevModeCondition value) {
        }

        @Override
        public MyoDevModeCondition read(JsonObject json) {
            return MyoDevModeCondition.INSTANCE;
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}
