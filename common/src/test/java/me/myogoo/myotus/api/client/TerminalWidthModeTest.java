package me.myogoo.myotus.api.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

class TerminalWidthModeTest {
    @Test
    void toggleCyclesFromCompactToWideAndBack() {
        var wide = TerminalWidthMode.COMPACT.next();
        assertSame(TerminalWidthMode.WIDE, wide);
        assertSame(TerminalWidthMode.COMPACT, wide.next());
    }
}
