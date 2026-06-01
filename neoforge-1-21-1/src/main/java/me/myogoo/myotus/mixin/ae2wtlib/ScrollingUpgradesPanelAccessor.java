package me.myogoo.myotus.mixin.ae2wtlib;

import de.mari_023.ae2wtlib.api.gui.ScrollingUpgradesPanel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;

@Pseudo
@Mixin(ScrollingUpgradesPanel.class)
public interface ScrollingUpgradesPanelAccessor {
    @Invoker("setMaxRows")
    void myotus$setMaxRows(int rows);

    @Invoker("scrolling")
    boolean hasScrollBar();
}
