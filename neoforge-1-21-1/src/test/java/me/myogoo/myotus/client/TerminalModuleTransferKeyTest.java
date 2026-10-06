package me.myogoo.myotus.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TerminalModuleTransferKeyTest {
    @Test
    void altAndShiftAltActivateButControlAlwaysKeepsNativeTransfer() {
        var binding = KeyBindings.TRANSFER_TO_MODULE;
        var previousKey = binding.getKey();
        var previousModifier = binding.getKeyModifier();
        var alt = InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_LEFT_ALT);
        var custom = InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_R);
        var client = mock(Minecraft.class);
        var window = mock(Window.class);
        when(client.getWindow()).thenReturn(window);
        when(window.getWindow()).thenReturn(1L);
        try (var minecraft = mockStatic(Minecraft.class); var screen = mockStatic(Screen.class);
                var input = mockStatic(InputConstants.class)) {
            minecraft.when(Minecraft::getInstance).thenReturn(client);
            binding.setKeyModifierAndCode(KeyModifier.NONE, alt);
            assertFalse(TerminalModuleTransferKey.isDown());
            screen.when(Screen::hasAltDown).thenReturn(true);
            assertTrue(TerminalModuleTransferKey.isDown());
            screen.when(Screen::hasShiftDown).thenReturn(true);
            assertTrue(TerminalModuleTransferKey.isDown());
            screen.when(Screen::hasControlDown).thenReturn(true);
            assertFalse(TerminalModuleTransferKey.isDown());

            screen.when(Screen::hasControlDown).thenReturn(false);
            screen.when(Screen::hasAltDown).thenReturn(false);
            binding.setKeyModifierAndCode(KeyModifier.NONE, custom);
            input.when(() -> InputConstants.isKeyDown(1L, GLFW.GLFW_KEY_R)).thenReturn(true);
            assertTrue(TerminalModuleTransferKey.isDown());
            binding.setKeyModifierAndCode(KeyModifier.ALT, custom);
            assertFalse(TerminalModuleTransferKey.isDown());
            screen.when(Screen::hasAltDown).thenReturn(true);
            assertTrue(TerminalModuleTransferKey.isDown());
            binding.setKeyModifierAndCode(KeyModifier.NONE, InputConstants.UNKNOWN);
            assertFalse(TerminalModuleTransferKey.isDown());
        } finally {
            binding.setKeyModifierAndCode(previousModifier, previousKey);
        }
    }
}
