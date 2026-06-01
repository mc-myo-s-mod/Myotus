package me.myogoo.myotus.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.myogoo.myotus.command.MyoCommandArguments.ParameterMapping;
import me.myogoo.myotus.command.MyoCommandMetadata.CommandInfo;
import me.myogoo.myotus.command.MyoCommandMetadata.ExecuteInfo;
import me.myogoo.myotus.command.MyoCommandMetadata.PermissionInfo;
import me.myogoo.myotus.api.command.argument.MyoArgumentAdapter;
import me.myogoo.myotus.util.MyoLogger;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

final class MyoCommandNodeBuilder {
    private MyoCommandNodeBuilder() {
    }

    static LiteralArgumentBuilder<CommandSourceStack> build(Class<?> clazz, List<Class<?>> allCommandClasses) {
        CommandInfo command = MyoCommandMetadata.getCommandInfo(clazz);
        if (command == null) {
            return null;
        }

        LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal(command.value());
        PermissionInfo permission = MyoCommandMetadata.getPermissionInfo(clazz);
        PermissionInfo executePermission = null;
        if (permission != null && MyoCommandPermissions.validate(permission, clazz.getName())) {
            MyoCommandPermissions.apply(node, permission);
            if (!permission.propagate()) {
                executePermission = permission;
            }
        }

        for (Method method : clazz.getDeclaredMethods()) {
            ExecuteInfo execute = MyoCommandMetadata.getExecuteInfo(method);
            if (execute == null) {
                continue;
            }
            if (!Modifier.isStatic(method.getModifiers())) {
                MyoLogger.error("Command execute method must be static: {}.{}", clazz.getName(), method.getName());
                continue;
            }

            buildExecutionBranch(clazz, method, execute, executePermission, node);
        }

        addChildCommands(clazz, allCommandClasses, node);
        return node;
    }

    private static void buildExecutionBranch(Class<?> owner,
                                             Method method,
                                             ExecuteInfo execute,
                                             PermissionInfo executePermission,
                                             LiteralArgumentBuilder<CommandSourceStack> node) {
        Parameter[] parameters = method.getParameters();
        List<ParameterMapping> mappings = new ArrayList<>();
        List<RequiredArgumentBuilder<CommandSourceStack, ?>> argumentBuilders = new ArrayList<>();

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class<?> parameterType = parameter.getType();
            if (parameterType == CommandContext.class || parameterType == CommandSourceStack.class) {
                mappings.add(ParameterMapping.context(i, parameterType));
                continue;
            }

            String argumentName = MyoCommandMetadata.getArgumentName(parameter);
            if (argumentName == null || argumentName.isBlank()) {
                MyoLogger.error("Parameter {} in {}.{} is missing @MyoArgument.",
                        parameter.getName(), owner.getName(), method.getName());
                return;
            }

            MyoArgumentAdapter<?> adapter = MyoCommandArguments.getAdapter(parameterType);
            if (adapter == null) {
                MyoLogger.error("Unsupported command argument type {} in {}.{}",
                        parameterType.getName(), owner.getName(), method.getName());
                return;
            }

            mappings.add(ParameterMapping.argument(i, argumentName, adapter));
            ArgumentType<?> argumentType = adapter.argumentType();
            argumentBuilders.add(Commands.argument(argumentName, argumentType));
        }

        method.setAccessible(true);
        Command<CommandSourceStack> executor = context -> invokeExecute(method, parameters, mappings,
                executePermission, context);
        attachExecution(node, execute.path(), argumentBuilders, executor);
    }

    private static int invokeExecute(Method method,
                                     Parameter[] parameters,
                                     List<ParameterMapping> mappings,
                                     PermissionInfo executePermission,
                                     CommandContext<CommandSourceStack> context) {
        try {
            if (executePermission != null && !MyoCommandPermissions.check(context.getSource(), executePermission)) {
                context.getSource().sendFailure(Component.literal("You do not have permission to execute this command."));
                return 0;
            }

            Object[] args = new Object[parameters.length];
            for (ParameterMapping mapping : mappings) {
                args[mapping.index()] = mapping.value(context);
            }

            Object result = method.invoke(null, args);
            if (result instanceof Number number) {
                return number.intValue();
            }
            if (result instanceof Boolean bool) {
                return bool ? Command.SINGLE_SUCCESS : 0;
            }
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            MyoLogger.error("Error executing Myotus command method {}", method.getName(), e);
            return 0;
        }
    }

    private static void attachExecution(LiteralArgumentBuilder<CommandSourceStack> node,
                                        String path,
                                        List<RequiredArgumentBuilder<CommandSourceStack, ?>> argumentBuilders,
                                        Command<CommandSourceStack> executor) {
        ArgumentBuilder<CommandSourceStack, ?> branch = null;
        if (!argumentBuilders.isEmpty()) {
            argumentBuilders.get(argumentBuilders.size() - 1).executes(executor);
            for (int i = argumentBuilders.size() - 1; i > 0; i--) {
                argumentBuilders.get(i - 1).then(argumentBuilders.get(i));
            }
            branch = argumentBuilders.get(0);
        }

        List<String> segments = MyoCommandPaths.split(path);
        if (segments.isEmpty()) {
            if (branch == null) {
                node.executes(executor);
            } else {
                node.then(branch);
            }
            return;
        }

        LiteralArgumentBuilder<CommandSourceStack> leaf = Commands.literal(segments.get(segments.size() - 1));
        if (branch == null) {
            leaf.executes(executor);
        } else {
            leaf.then(branch);
        }

        LiteralArgumentBuilder<CommandSourceStack> current = leaf;
        for (int i = segments.size() - 2; i >= 0; i--) {
            LiteralArgumentBuilder<CommandSourceStack> parent = Commands.literal(segments.get(i));
            parent.then(current);
            current = parent;
        }
        node.then(current);
    }

    private static void addChildCommands(Class<?> clazz,
                                         List<Class<?>> allCommandClasses,
                                         LiteralArgumentBuilder<CommandSourceStack> node) {
        for (Class<?> innerClass : clazz.getDeclaredClasses()) {
            CommandInfo innerCommand = MyoCommandMetadata.getCommandInfo(innerClass);
            if (innerCommand != null && innerCommand.parent() == clazz) {
                LiteralArgumentBuilder<CommandSourceStack> child = build(innerClass, allCommandClasses);
                if (child != null) {
                    node.then(child);
                }
            }
        }

        for (Class<?> candidate : allCommandClasses) {
            CommandInfo candidateCommand = MyoCommandMetadata.getCommandInfo(candidate);
            if (candidateCommand != null
                    && candidateCommand.parent() == clazz
                    && candidate.getDeclaringClass() != clazz) {
                LiteralArgumentBuilder<CommandSourceStack> child = build(candidate, allCommandClasses);
                if (child != null) {
                    node.then(child);
                }
            }
        }
    }
}
