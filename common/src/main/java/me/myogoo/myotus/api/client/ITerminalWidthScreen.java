package me.myogoo.myotus.api.client;

/**
 * Client-side width preference exposed by AEBaseScreen and inherited by its subclasses.
 * ME storage screens with a dedicated wide style apply this preference to their layout.
 * Call from the Minecraft client thread.
 */
public interface ITerminalWidthScreen {
    TerminalWidthMode getTerminalWidthMode();

    /** Saves the client preference and refreshes this screen's layout. */
    void setTerminalWidthMode(TerminalWidthMode mode);
}
