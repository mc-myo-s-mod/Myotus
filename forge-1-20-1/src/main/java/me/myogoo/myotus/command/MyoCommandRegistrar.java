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
import java.util.Set;

public final class MyoCommandRegistrar {
    private MyoCommandRegistrar() {
    }

    public static void registerAdapter(Class<?> type, MyoArgumentAdapter<?> adapter) {
        MyoCommandArguments.registerAdapter(type, adapter);
    }

    public static void registerAll(CommandDispatcher<CommandSourceStack> dispatcher,
                                   Collection<Class<?>> commandClasses,
                                   boolean developmentMode) {
        List<Class<?>> filteredClasses = filterCommandClasses(commandClasses, developmentMode);
        for (Class<?> clazz : filteredClasses) {
            MyoCommandMetadata.CommandInfo command = MyoCommandMetadata.getCommandInfo(clazz);
            if (command != null && command.parent() == void.class) {
                register(dispatcher, clazz, filteredClasses);
            }
        }
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                Class<?> clazz,
                                List<Class<?>> allCommandClasses) {
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
    }

    private static List<Class<?>> filterCommandClasses(Collection<Class<?>> commandClasses, boolean developmentMode) {
        Set<Class<?>> filtered = new LinkedHashSet<>();
        for (Class<?> clazz : commandClasses) {
            if (clazz == null || MyoCommandMetadata.getCommandInfo(clazz) == null) {
                continue;
            }
            if (MyoCommandMetadata.isDebugOnly(clazz) && !developmentMode) {
                MyoLogger.debug("Skipping debug command class outside development mode: {}", clazz.getName());
                continue;
            }
            filtered.add(clazz);
        }
        return new ArrayList<>(filtered);
    }
}
