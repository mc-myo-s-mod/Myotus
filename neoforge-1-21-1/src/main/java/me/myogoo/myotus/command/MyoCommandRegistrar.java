package me.myogoo.myotus.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.myogoo.myotus.api.command.argument.MyoArgumentAdapter;
import me.myogoo.myotus.util.MyoLogger;
import net.minecraft.commands.CommandSourceStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class MyoCommandRegistrar {
    private MyoCommandRegistrar() {
    }

    public static void registerAdapter(Class<?> type, MyoArgumentAdapter<?> adapter) {
        MyoCommandArguments.registerAdapter(
                Objects.requireNonNull(type, "type"),
                Objects.requireNonNull(adapter, "adapter"));
    }

    public static void registerAll(CommandDispatcher<CommandSourceStack> dispatcher,
                                   Collection<Class<?>> commandClasses,
                                   boolean developmentMode) {
        List<Class<?>> filteredClasses = filterCommandClasses(commandClasses, developmentMode);
        for (Class<?> clazz : filteredClasses) {
            try {
                MyoCommandMetadata.CommandInfo command = MyoCommandMetadata.getCommandInfo(clazz);
                if (command != null && command.parent() == void.class) {
                    register(dispatcher, clazz, filteredClasses);
                }
            } catch (RuntimeException | LinkageError e) {
                logSkippedCommand(clazz, e);
            }
        }
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                Class<?> clazz,
                                List<Class<?>> allCommandClasses) {
        try {
            MyoCommandMetadata.CommandInfo command = MyoCommandMetadata.getCommandInfo(clazz);
            if (command == null) {
                return;
            }

            LiteralArgumentBuilder<CommandSourceStack> builder = MyoCommandNodeBuilder.build(clazz, allCommandClasses);
            if (builder == null) {
                MyoLogger.warn("Could not build command node for {}", clazz.getName());
                return;
            }

            LiteralCommandNode<CommandSourceStack> registeredNode = dispatcher.register(builder);
            MyoCommandAliasRegistrar.registerAliases(dispatcher, clazz, registeredNode, allCommandClasses);
            MyoLogger.info("Registered Myotus command node: {}", registeredNode.getName());
        } catch (RuntimeException | LinkageError e) {
            logSkippedCommand(clazz, e);
        }
    }

    private static List<Class<?>> filterCommandClasses(Collection<Class<?>> commandClasses, boolean developmentMode) {
        Set<Class<?>> filtered = new LinkedHashSet<>();
        for (Class<?> clazz : commandClasses) {
            if (clazz == null) {
                continue;
            }
            try {
                if (MyoCommandMetadata.getCommandInfo(clazz) == null) {
                    continue;
                }
                if (MyoCommandMetadata.isDebugOnly(clazz) && !developmentMode) {
                    MyoLogger.debug("Skipping debug command class outside development mode: {}", clazz.getName());
                    continue;
                }
                filtered.add(clazz);
            } catch (RuntimeException | LinkageError e) {
                logSkippedCommand(clazz, e);
            }
        }
        return new ArrayList<>(filtered);
    }

    private static void logSkippedCommand(Class<?> clazz, Throwable cause) {
        MyoLogger.error("Skipping Myotus command class {} because its metadata could not be resolved.",
                clazz.getName(), cause);
    }
}
