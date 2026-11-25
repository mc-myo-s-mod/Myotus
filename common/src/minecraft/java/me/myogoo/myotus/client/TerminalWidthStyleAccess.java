package me.myogoo.myotus.client;

import appeng.client.gui.style.ScreenStyle;

/** Internal bridge to the style field owned by AEBaseScreen. */
public interface TerminalWidthStyleAccess {
    void myotus$setScreenStyle(ScreenStyle style);
}
