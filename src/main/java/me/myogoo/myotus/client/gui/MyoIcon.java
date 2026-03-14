package me.myogoo.myotus.client.gui;

import appeng.client.gui.style.Blitter;
import me.myogoo.myotus.Myotus;
import net.minecraft.resources.ResourceLocation;

public enum MyoIcon {

    BUG_REPORT(0, 0),
    MYOTUS_CONFIG(16,0),
    AE2TB_CONFIG(32,0);
    private static final ResourceLocation TEXTURE = Myotus.makeId("textures/gui/myoicons.png");

    private final int x;
    private final int y;
    MyoIcon(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Blitter getBlitter() {
        return Blitter.texture(TEXTURE)
                .src(x, y, 16, 16);
    }
}
