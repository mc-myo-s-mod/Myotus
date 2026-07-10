package me.myogoo.myotus.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import me.myogoo.myotus.command.MyoCommandMetadata.AliasInfo;
import me.myogoo.myotus.command.MyoCommandMetadata.CommandInfo;
import me.myogoo.myotus.util.MyoLogger;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

final class MyoCommandAliasRegistrar {
    private MyoCommandAliasRegistrar() {
    }

    static void registerAliases(CommandDispatcher<CommandSourceStack> dispatcher,
                                Class<?> rootClass,
                                CommandNode<CommandSourceStack> rootNode,
                                List<Class<?>> allCommandClasses) {
        AliasInfo rootAliases = MyoCommandMetadata.getAliasInfo(rootClass);
        if (rootAliases != null) {
            for (String alias : rootAliases.values()) {
                registerAlias(dispatcher, alias, rootNode, List.of(), source -> true);
            }
        }

        List<Class<?>> children = new ArrayList<>();
        collectAllChildren(rootClass, allCommandClasses, children);
        for (Class<?> child : children) {
            AliasInfo aliases = MyoCommandMetadata.getAliasInfo(child);
            if (aliases == null) {
                continue;
            }

            List<String> path = MyoCommandPaths.resolveCommandPath(child, rootClass);
            if (path == null || path.isEmpty()) {
                MyoLogger.warn("Could not resolve command path for alias on {}", child.getName());
                continue;
            }

            CommandNode<CommandSourceStack> targetNode = findNode(dispatcher, path);
            if (targetNode == null) {
                MyoLogger.warn("Could not find command node for alias path {}", path);
                continue;
            }

            List<String> parentPath = path.subList(0, path.size() - 1);
            Predicate<CommandSourceStack> ancestorRequirement = collectAncestorRequirement(dispatcher, path);
            for (String alias : aliases.values()) {
                registerAlias(dispatcher, alias, targetNode, parentPath,
                        alias.startsWith("/") ? ancestorRequirement : source -> true);
            }
        }
    }

    private static void registerAlias(CommandDispatcher<CommandSourceStack> dispatcher,
                                      String alias,
                                      CommandNode<CommandSourceStack> targetNode,
                                      List<String> parentPath,
                                      Predicate<CommandSourceStack> ancestorRequirement) {
        List<String> segments = MyoCommandPaths.resolveAlias(alias, parentPath);
        if (segments.isEmpty()) {
            return;
        }

        LiteralArgumentBuilder<CommandSourceStack> current = copyTargetNode(
                segments.get(segments.size() - 1), targetNode, ancestorRequirement);
        if (segments.size() == 1) {
            dispatcher.register(current);
            return;
        }

        for (int i = segments.size() - 2; i >= 1; i--) {
            LiteralArgumentBuilder<CommandSourceStack> parent = Commands.literal(segments.get(i));
            parent.then(current);
            current = parent;
        }

        CommandNode<CommandSourceStack> existingRoot = dispatcher.getRoot().getChild(segments.get(0));
        if (existingRoot != null) {
            existingRoot.addChild(current.build());
            return;
        }

        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(segments.get(0));
        root.then(current);
        dispatcher.register(root);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> copyTargetNode(String name,
            CommandNode<CommandSourceStack> targetNode,
            Predicate<CommandSourceStack> ancestorRequirement) {
        LiteralArgumentBuilder<CommandSourceStack> copy = Commands.literal(name);
        if (targetNode.getCommand() != null) {
            copy.executes(targetNode.getCommand());
        }
        copy.requires(ancestorRequirement.and(targetNode.getRequirement()));
        for (CommandNode<CommandSourceStack> child : targetNode.getChildren()) {
            copy.then(child);
        }
        return copy;
    }

    private static Predicate<CommandSourceStack> collectAncestorRequirement(
            CommandDispatcher<CommandSourceStack> dispatcher, List<String> path) {
        Predicate<CommandSourceStack> combined = source -> true;
        CommandNode<CommandSourceStack> node = dispatcher.getRoot();
        for (int i = 0; i < path.size() - 1; i++) {
            node = node.getChild(path.get(i));
            if (node == null) {
                return source -> false;
            }
            combined = combined.and(node.getRequirement());
        }
        return combined;
    }

    private static void collectAllChildren(Class<?> parentClass, List<Class<?>> allCommandClasses, List<Class<?>> result) {
        for (Class<?> innerClass : parentClass.getDeclaredClasses()) {
            CommandInfo command = MyoCommandMetadata.getCommandInfo(innerClass);
            if (command != null && command.parent() == parentClass) {
                result.add(innerClass);
                collectAllChildren(innerClass, allCommandClasses, result);
            }
        }

        for (Class<?> candidate : allCommandClasses) {
            CommandInfo command = MyoCommandMetadata.getCommandInfo(candidate);
            if (command != null && command.parent() == parentClass && candidate.getDeclaringClass() != parentClass) {
                result.add(candidate);
                collectAllChildren(candidate, allCommandClasses, result);
            }
        }
    }

    private static CommandNode<CommandSourceStack> findNode(CommandDispatcher<CommandSourceStack> dispatcher,
                                                            List<String> path) {
        CommandNode<CommandSourceStack> node = dispatcher.getRoot();
        for (String segment : path) {
            node = node.getChild(segment);
            if (node == null) {
                return null;
            }
        }
        return node;
    }
}
