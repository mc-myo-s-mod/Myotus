package me.myogoo.myotus.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

/** Poll the configured key while a recipe viewer, rather than the terminal, owns keyboard input. */
public final class TerminalModuleTransferKey {
    private TerminalModuleTransferKey() {}

    public static boolean isDown() {
        // Ctrl keeps AE2's native request-missing-ingredients behavior, even with Alt held.
        if (Screen.hasControlDown()) return false;
        var binding = KeyBindings.TRANSFER_TO_MODULE;
        var modifier = binding.getKeyModifier();
        if (modifier != KeyModifier.NONE && !modifier.isActive(KeyConflictContext.GUI)) return false;
        var key = binding.getKey();
        long window = Minecraft.getInstance().getWindow().getWindow();
        if (key.getType() == InputConstants.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS;
        }
        if (key.getType() != InputConstants.Type.KEYSYM || key.getValue() == GLFW.GLFW_KEY_UNKNOWN) return false;
        if (key.getValue() == GLFW.GLFW_KEY_LEFT_ALT || key.getValue() == GLFW.GLFW_KEY_RIGHT_ALT) {
            return Screen.hasAltDown();
        }
        return InputConstants.isKeyDown(window, key.getValue());
    }
}
