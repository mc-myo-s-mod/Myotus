package me.myogoo.myotus.command;

import me.myogoo.myotus.api.annotation.commands.MyoCommand;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MyoCommandPathsTest {
    @Test
    void splitHandlesNullBlankSlashAndWhitespaceSeparatedPaths() {
        assertEquals(List.of(), MyoCommandPaths.split(null));
        assertEquals(List.of(), MyoCommandPaths.split("  "));
        assertEquals(List.of("alpha", "beta", "gamma"),
                MyoCommandPaths.split(" /alpha beta//gamma "));
    }

    @Test
    void resolveAliasUsesAbsolutePathWhenAliasStartsWithSlash() {
        assertEquals(List.of("global", "reload"),
                MyoCommandPaths.resolveAlias("/global reload", List.of("myotus", "config")));
    }

    @Test
    void resolveAliasAppendsRelativePathToParentPath() {
        assertEquals(List.of("myotus", "config", "set", "value"),
                MyoCommandPaths.resolveAlias("set value", List.of("myotus", "config")));
    }

    @Test
    void resolveAliasReturnsEmptyPathForBlankAlias() {
        assertEquals(List.of(), MyoCommandPaths.resolveAlias(null, List.of("myotus")));
        assertEquals(List.of(), MyoCommandPaths.resolveAlias(" ", List.of("myotus")));
    }

    @Test
    void resolveCommandPathWalksAnnotatedParentChain() {
        assertEquals(List.of("root", "child", "leaf"),
                MyoCommandPaths.resolveCommandPath(LeafCommand.class, RootCommand.class));
    }

    @Test
    void resolveCommandPathReturnsNullForBrokenOrMismatchedChains() {
        assertNull(MyoCommandPaths.resolveCommandPath(BrokenCommand.class, RootCommand.class));
        assertNull(MyoCommandPaths.resolveCommandPath(PlainCommand.class, RootCommand.class));
        assertNull(MyoCommandPaths.resolveCommandPath(ChildCommand.class, LeafCommand.class));
    }

    @MyoCommand("root")
    private static final class RootCommand {
    }

    @MyoCommand(value = "child", parent = RootCommand.class)
    private static final class ChildCommand {
    }

    @MyoCommand(value = "leaf", parent = ChildCommand.class)
    private static final class LeafCommand {
    }

    @MyoCommand(value = "broken", parent = PlainCommand.class)
    private static final class BrokenCommand {
    }

    private static final class PlainCommand {
    }
}
