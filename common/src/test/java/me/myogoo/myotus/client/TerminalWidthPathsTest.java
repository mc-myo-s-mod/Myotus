package me.myogoo.myotus.client;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TerminalWidthPathsTest {
    @Test
    void derivesTheSiblingOfTheExactNativeDocument() {
        assertEquals(Optional.of("/screens/terminals/crafting_terminal_wide.json"),
                TerminalWidthPaths.widePath("/screens/terminals/crafting_terminal.json"));
        assertEquals(Optional.of("/screens/addon_wide/custom_wide.json"),
                TerminalWidthPaths.widePath("/screens/addon_wide/custom.json"));
        assertEquals(Optional.of("/terminal_wide.json"), TerminalWidthPaths.widePath("/terminal.json"));
    }

    @Test
    void rejectsUnknownNonJsonAndAlreadyWidePaths() {
        assertTrue(TerminalWidthPaths.widePath(null).isEmpty());
        assertTrue(TerminalWidthPaths.widePath("screens/terminal.json").isEmpty());
        assertTrue(TerminalWidthPaths.widePath("/screens/terminal.png").isEmpty());
        assertTrue(TerminalWidthPaths.widePath("/screens/.json").isEmpty());
        assertTrue(TerminalWidthPaths.widePath("/screens/terminal_wide.json").isEmpty());
    }
}
