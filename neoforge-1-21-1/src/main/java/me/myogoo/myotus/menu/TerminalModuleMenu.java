package me.myogoo.myotus.menu;

/** Internal per-menu storage; state has exactly the same lifetime as its owning menu. */
public interface TerminalModuleMenu {
    TerminalModuleSlots.State myotus$getModuleSlots();
}
