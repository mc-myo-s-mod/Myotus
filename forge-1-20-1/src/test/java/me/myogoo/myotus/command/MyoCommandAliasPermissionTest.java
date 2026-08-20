package me.myogoo.myotus.command;

import com.mojang.brigadier.CommandDispatcher;
import me.myogoo.myotus.api.annotation.commands.MyoAlias;
import me.myogoo.myotus.api.annotation.commands.MyoCommand;
import me.myogoo.myotus.api.annotation.commands.MyoExecute;
import me.myogoo.myotus.api.annotation.commands.MyoPermission;
import me.myogoo.myotus.api.command.permission.MyoPermissionLevel;
import net.minecraft.commands.CommandSourceStack;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MyoCommandAliasPermissionTest {
    @Test
    void absoluteChildAliasRetainsPropagatedAncestorPermission() {
        var dispatcher = new CommandDispatcher<CommandSourceStack>();
        MyoCommandRegistrar.registerAll(dispatcher, List.of(SecuredRoot.class), true);

        var shortcut = dispatcher.getRoot().getChild("shortcut");
        assertNotNull(shortcut);

        CommandSourceStack denied = mock(CommandSourceStack.class);
        when(denied.hasPermission(MyoPermissionLevel.ADMIN.getCommandPermissionLevel())).thenReturn(false);
        assertFalse(shortcut.canUse(denied));

        CommandSourceStack allowed = mock(CommandSourceStack.class);
        when(allowed.hasPermission(MyoPermissionLevel.ADMIN.getCommandPermissionLevel())).thenReturn(true);
        assertTrue(shortcut.canUse(allowed));
    }

    @MyoCommand("secured")
    @MyoPermission(permission = MyoPermissionLevel.ADMIN, propagate = true)
    static final class SecuredRoot {
        @MyoCommand(value = "child", parent = SecuredRoot.class)
        @MyoAlias("/shortcut")
        static final class Child {
            @MyoExecute
            static int execute() {
                return 1;
            }
        }
    }
}
