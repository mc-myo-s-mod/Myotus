package me.myogoo.myotus.init;

import me.myogoo.myotus.client.settings.TerminalKeyConflictContext;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;

public class MyoKeyConflictContext {
    public static final IKeyConflictContext TERMINAL = new TerminalKeyConflictContext();
}
