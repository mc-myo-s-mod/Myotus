package me.myogoo.myotus.client.settings;

import appeng.client.gui.me.common.MEStorageScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.settings.IKeyConflictContext;

public class TerminalKeyConflictContext implements IKeyConflictContext {
    @Override
    public boolean isActive() {
        var screen = Minecraft.getInstance().screen;
        if (screen == null) return false;
        return screen instanceof MEStorageScreen<?>;
    }

    @Override
    public boolean conflicts(IKeyConflictContext other) {
        return this == other;
    }
}
