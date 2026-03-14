package me.myogoo.myotus.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.myogoo.myotus.init.MyoKeyConflictContext;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static final KeyMapping OPEN_TERMINAL_SETTING = new KeyMapping(
            TranslateKey.OPEN_TERMINAL_SETTING_KEY.key(),
            MyoKeyConflictContext.TERMINAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_COMMA,
            TranslateKey.KEY_CATEGORY.key()
    );
}
