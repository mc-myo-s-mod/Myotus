package me.myogoo.myotus.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.myogoo.myotus.client.settings.TerminalKeyConflictContext;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    private static final TerminalKeyConflictContext TERMINAL_CONFLICT_CONTEXT = new TerminalKeyConflictContext();

    public static final KeyMapping OPEN_TERMINAL_SETTING = new KeyMapping(
            TranslateKey.OPEN_TERMINAL_SETTING_KEY.key(),
            TERMINAL_CONFLICT_CONTEXT,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_COMMA,
            TranslateKey.KEY_CATEGORY.key()
    );

    public static final KeyMapping TOGGLE_SUB_SIDE_PANEL = new KeyMapping(
            TranslateKey.TOGGLE_SUB_SIDE_PANEL_KEY.key(),
            TERMINAL_CONFLICT_CONTEXT,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_PERIOD,
            TranslateKey.KEY_CATEGORY.key()
    );
}
