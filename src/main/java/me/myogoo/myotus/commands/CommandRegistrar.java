package me.myogoo.myotus.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.myogoo.myotus.api.command.MyoArgument;
import me.myogoo.myotus.api.command.MyoCommand;
import me.myogoo.myotus.api.command.MyoExecute;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandRegistrar {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandRegistrar.class);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, Class<?> clazz,
            List<Class<?>> allCommandClasses) {
        if (!clazz.isAnnotationPresent(MyoCommand.class)) {
            return;
        }

        LiteralArgumentBuilder<CommandSourceStack> builder = buildCommandNode(clazz, allCommandClasses);
        if (builder != null) {
            dispatcher.register(builder);
            LOGGER.info("Successfully registered root command node: {}", clazz.getSimpleName());
        }
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildCommandNode(Class<?> clazz,
            List<Class<?>> allCommandClasses) {
        MyoCommand cmdAnnotation = clazz.getAnnotation(MyoCommand.class);
        if (cmdAnnotation == null)
            return null;

        LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal(cmdAnnotation.value());

        // Handle executes
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(MyoExecute.class)) {
                if (!Modifier.isStatic(method.getModifiers())) {
                    LOGGER.error("Method {} in {} must be static.", method.getName(), clazz.getName());
                    continue;
                }

                Parameter[] parameters = method.getParameters();
                List<ParameterMapping> mappings = new ArrayList<>();
                List<RequiredArgumentBuilder<CommandSourceStack, ?>> argBuilders = new ArrayList<>();

                for (Parameter param : parameters) {
                    if (param.getType() == CommandContext.class || param.getType() == CommandSourceStack.class) {
                        mappings.add(new ParameterMapping(null, param.getType()));
                        continue;
                    }

                    String argName = null;
                    if (param.isAnnotationPresent(MyoArgument.class)) {
                        argName = param.getAnnotation(MyoArgument.class).value();
                    }

                    if (argName == null) {
                        LOGGER.error("Parameter {} in method {} is missing @MyoArgument annotation.", param.getName(),
                                method.getName());
                        continue;
                    }

                    ArgumentType<?> argType = getArgumentTypeForClass(param.getType());
                    if (argType == null) {
                        LOGGER.error("Unsupported argument type {} in method {}", param.getType().getName(),
                                method.getName());
                        continue;
                    }

                    mappings.add(new ParameterMapping(argName, param.getType()));
                    RequiredArgumentBuilder<CommandSourceStack, ?> argBuilder = Commands.argument(argName, argType);
                    argBuilders.add(argBuilder);
                }

                Command<CommandSourceStack> commandExecutor = context -> {
                    try {
                        Object[] args = new Object[parameters.length];
                        for (int i = 0; i < mappings.size(); i++) {
                            ParameterMapping map = mappings.get(i);
                            if (map.name == null) {
                                if (map.type == CommandContext.class) {
                                    args[i] = context;
                                } else if (map.type == CommandSourceStack.class) {
                                    args[i] = context.getSource();
                                }
                            } else {
                                if (map.type.getName().contains("ServerPlayer")) {
                                    args[i] = EntityArgument.getPlayer(context, map.name);
                                } else if (map.type.getName().contains("Entity")) {
                                    args[i] = EntityArgument.getEntity(context, map.name);
                                } else {
                                    args[i] = context.getArgument(map.name, map.type);
                                }
                            }
                        }
                        return (int) method.invoke(null, args); // Execute the command method
                    } catch (Exception e) {
                        LOGGER.error("Error executing command", e);
                        return 0;
                    }
                };

                if (!argBuilders.isEmpty()) {
                    argBuilders.get(argBuilders.size() - 1).executes(commandExecutor);
                    for (int j = argBuilders.size() - 1; j > 0; j--) {
                        argBuilders.get(j - 1).then(argBuilders.get(j));
                    }
                    node.then(argBuilders.get(0));
                } else {
                    node = node.executes(commandExecutor);
                }
            }
        }

        // Handle subcommands: inner classes
        for (Class<?> innerClass : clazz.getDeclaredClasses()) {
            if (innerClass.isAnnotationPresent(MyoCommand.class)) {
                MyoCommand innerCmdAnnotation = innerClass.getAnnotation(MyoCommand.class);
                if (innerCmdAnnotation.parent() == clazz) {
                    LiteralArgumentBuilder<CommandSourceStack> subNode = buildCommandNode(innerClass,
                            allCommandClasses);
                    if (subNode != null) {
                        node = node.then(subNode);
                    }
                }
            }
        }

        // Handle subcommands: external classes
        for (Class<?> candidate : allCommandClasses) {
            if (candidate.isAnnotationPresent(MyoCommand.class)) {
                MyoCommand candidateAnnotation = candidate.getAnnotation(MyoCommand.class);
                if (candidateAnnotation.parent() == clazz && candidate.getDeclaringClass() != clazz) {
                    LiteralArgumentBuilder<CommandSourceStack> subNode = buildCommandNode(candidate, allCommandClasses);
                    if (subNode != null) {
                        node = node.then(subNode);
                    }
                }
            }
        }

        return node;
    }

    private static ArgumentType<?> getArgumentTypeForClass(Class<?> clazz) {
        if (clazz == int.class || clazz == Integer.class)
            return IntegerArgumentType.integer();
        if (clazz == boolean.class || clazz == Boolean.class)
            return BoolArgumentType.bool();
        if (clazz == String.class)
            return StringArgumentType.string();
        if (clazz == ServerPlayer.class)
            return EntityArgument.player();
        if (clazz == Entity.class)
            return EntityArgument.entity();
        return null;
    }

    private static class ParameterMapping {
        String name;
        Class<?> type;

        ParameterMapping(String name, Class<?> type) {
            this.name = name;
            this.type = type;
        }
    }
}
