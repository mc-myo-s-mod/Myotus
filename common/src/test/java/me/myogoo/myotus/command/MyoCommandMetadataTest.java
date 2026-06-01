package me.myogoo.myotus.command;

import me.myogoo.myotus.api.annotation.MyoDebug;
import me.myogoo.myotus.api.annotation.commands.MyoAlias;
import me.myogoo.myotus.api.annotation.commands.MyoArgument;
import me.myogoo.myotus.api.annotation.commands.MyoCommand;
import me.myogoo.myotus.api.annotation.commands.MyoExecute;
import me.myogoo.myotus.api.annotation.commands.MyoPermission;
import me.myogoo.myotus.api.command.permission.MyoPermissionLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MyoCommandMetadataTest {
    @Test
    void readsCommandClassMetadata() {
        var command = MyoCommandMetadata.getCommandInfo(CommandWithMetadata.class);
        var alias = MyoCommandMetadata.getAliasInfo(CommandWithMetadata.class);
        var permission = MyoCommandMetadata.getPermissionInfo(CommandWithMetadata.class);

        assertEquals("root", command.value());
        assertEquals(void.class, command.parent());
        assertArrayEquals(new String[]{"r", "rootAlias"}, alias.values());
        assertArrayEquals(new String[]{"myotus.root"}, permission.nodes());
        assertEquals(MyoPermissionLevel.ADMIN, permission.level());
        assertEquals(CustomChecker.class, permission.customChecker());
        assertEquals(Void.class, permission.defaultChecker());
        assertTrue(permission.propagate());
        assertTrue(MyoCommandMetadata.isDebugOnly(CommandWithMetadata.class));
    }

    @Test
    void readsExecuteMethodAndArgumentMetadata() throws NoSuchMethodException {
        var method = CommandWithMetadata.class.getDeclaredMethod("execute", int.class, String.class);
        var execute = MyoCommandMetadata.getExecuteInfo(method);
        var permission = MyoCommandMetadata.getPermissionInfo(method);
        var parameters = method.getParameters();

        assertEquals("child run", execute.path());
        assertEquals("amount", MyoCommandMetadata.getArgumentName(parameters[0]));
        assertNull(MyoCommandMetadata.getArgumentName(parameters[1]));
        assertArrayEquals(new String[0], permission.nodes());
        assertEquals(MyoPermissionLevel.OWNER, permission.level());
        assertFalse(permission.propagate());
    }

    @Test
    void returnsNullOrFalseWhenMetadataIsAbsent() throws NoSuchMethodException {
        var method = PlainCommand.class.getDeclaredMethod("execute", String.class);

        assertNull(MyoCommandMetadata.getCommandInfo(PlainCommand.class));
        assertNull(MyoCommandMetadata.getAliasInfo(PlainCommand.class));
        assertNull(MyoCommandMetadata.getPermissionInfo(PlainCommand.class));
        assertNull(MyoCommandMetadata.getExecuteInfo(method));
        assertNull(MyoCommandMetadata.getArgumentName(method.getParameters()[0]));
        assertFalse(MyoCommandMetadata.isDebugOnly(PlainCommand.class));
    }

    @MyoDebug
    @MyoAlias({"r", "rootAlias"})
    @MyoPermission(value = "myotus.root", permission = MyoPermissionLevel.ADMIN,
            custom = CustomChecker.class, propagate = true)
    @MyoCommand("root")
    private static final class CommandWithMetadata {
        @MyoExecute("child run")
        @MyoPermission(permission = MyoPermissionLevel.OWNER)
        private static void execute(@MyoArgument("amount") int amount, String ignored) {
        }
    }

    private static final class PlainCommand {
        private static void execute(String ignored) {
        }
    }

    private static final class CustomChecker {
    }
}
