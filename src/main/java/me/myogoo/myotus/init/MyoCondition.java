package me.myogoo.myotus.init;

import me.myogoo.myotus.api.datagen.MyoModCondition;
import net.minecraftforge.common.crafting.CraftingHelper;

public final class MyoCondition {
    private static boolean registered;

    private MyoCondition() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        CraftingHelper.register(MyoModCondition.Serializer.INSTANCE);
    }
}
