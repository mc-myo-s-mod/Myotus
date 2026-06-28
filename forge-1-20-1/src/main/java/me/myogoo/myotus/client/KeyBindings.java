package me.myogoo.myotus.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.myogoo.myotus.client.settings.TerminalKeyConflictContext;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    private static final TerminalKeyConflictContext TERMINAL_CONFLICT_CONTEXT = new TerminalKeyConflictContext();

    public static final KeyMapping OPEN_TERMINAL_SETTING = new KeyMapping(
            TranslateKey.KEY.OPEN_TERMINAL_SETTING.key(),
            TERMINAL_CONFLICT_CONTEXT,
            KeyModifier.CONTROL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_PERIOD,
            TranslateKey.KEY.CATEGORY_MYOTUS.key()
    );

    public static final KeyMapping TOGGLE_UPGRADE_TERMINAL_PANEL = new KeyMapping(
            TranslateKey.KEY.TOGGLE_UPGRADE_TERMINAL_PANEL.key(),
            TERMINAL_CONFLICT_CONTEXT,
            KeyModifier.CONTROL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_COMMA,
            TranslateKey.KEY.CATEGORY_MYOTUS.key()
    );
}
