package me.myogoo.myotus.api.client;

/** Horizontal terminal layout, independent of AE2's row-height setting. */
public enum TerminalWidthMode {
    COMPACT,
    WIDE;

    public TerminalWidthMode next() {
        return this == COMPACT ? WIDE : COMPACT;
    }
}
