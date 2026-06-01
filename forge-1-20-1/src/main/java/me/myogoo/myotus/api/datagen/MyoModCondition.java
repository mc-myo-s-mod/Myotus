package me.myogoo.myotus.api.datagen;

import com.google.gson.JsonObject;
import me.myogoo.myotus.Myotus;
import me.myogoo.myotus.util.mod.ModIntegrationManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

/**
 * Condition that evaluates to {@code true} when an annotation-discovered
 * Myotus mod integration is active.
 *
 * <p>This condition is serialized with the {@code active_mod} field. The value
 * may be the integration mod ID, {@link me.myogoo.myotus.api.annotation.MyoMod#alias()},
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
    public static final ResourceLocation ID = Myotus.makeId("mod_condition");

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
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return ModIntegrationManager.isLoaded(modId);
    }

    /**
     * Returns the mod ID serialized by this condition.
     *
     * @return integration mod ID
     */
    public String getModId() {
        return modId;
    }

    public static final class Serializer implements IConditionSerializer<MyoModCondition> {
        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {
        }

        @Override
        public void write(JsonObject json, MyoModCondition value) {
            json.addProperty("active_mod", value.getModId());
        }

        @Override
        public MyoModCondition read(JsonObject json) {
            return new MyoModCondition(GsonHelper.getAsString(json, "active_mod"));
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}
