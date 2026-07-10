package me.myogoo.myotus.mixin.ae2wtlib;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;

@Pseudo
@Mixin(targets = "de.mari_023.ae2wtlib.api.gui.ScrollingUpgradesPanel", remap = false)
public interface ScrollingUpgradesPanelAccessor {
    @Invoker("scrolling")
    boolean myotus$isScrolling();
}
