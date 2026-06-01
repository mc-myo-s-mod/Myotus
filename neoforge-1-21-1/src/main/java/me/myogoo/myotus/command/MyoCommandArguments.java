package me.myogoo.myotus.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.myogoo.myotus.api.command.argument.MyoArgumentAdapter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;

final class MyoCommandArguments {
    private static final Map<Class<?>, MyoArgumentAdapter<?>> ADAPTERS = new HashMap<>();

    static {
        registerAdapter(int.class, simpleAdapter(IntegerArgumentType.integer(), IntegerArgumentType::getInteger));
        registerAdapter(Integer.class, simpleAdapter(IntegerArgumentType.integer(), IntegerArgumentType::getInteger));
        registerAdapter(double.class, simpleAdapter(DoubleArgumentType.doubleArg(), DoubleArgumentType::getDouble));
        registerAdapter(Double.class, simpleAdapter(DoubleArgumentType.doubleArg(), DoubleArgumentType::getDouble));
        registerAdapter(float.class, simpleAdapter(FloatArgumentType.floatArg(), FloatArgumentType::getFloat));
        registerAdapter(Float.class, simpleAdapter(FloatArgumentType.floatArg(), FloatArgumentType::getFloat));
        registerAdapter(boolean.class, simpleAdapter(BoolArgumentType.bool(), BoolArgumentType::getBool));
        registerAdapter(Boolean.class, simpleAdapter(BoolArgumentType.bool(), BoolArgumentType::getBool));
        registerAdapter(String.class, simpleAdapter(StringArgumentType.string(), StringArgumentType::getString));
        registerAdapter(Entity.class, new MyoArgumentAdapter<>() {
            @Override
            public ArgumentType<?> argumentType() {
                return EntityArgument.entity();
            }

            @Override
            public Entity value(CommandContext<CommandSourceStack> context, String name) throws Exception {
                return EntityArgument.getEntity(context, name);
            }
        });
        registerAdapter(ServerPlayer.class, new MyoArgumentAdapter<>() {
            @Override
            public ArgumentType<?> argumentType() {
                return EntityArgument.player();
            }

            @Override
            public ServerPlayer value(CommandContext<CommandSourceStack> context, String name) throws Exception {
                return EntityArgument.getPlayer(context, name);
            }
        });
    }

    private MyoCommandArguments() {
    }

    static void registerAdapter(Class<?> type, MyoArgumentAdapter<?> adapter) {
        ADAPTERS.put(type, adapter);
    }

    static MyoArgumentAdapter<?> getAdapter(Class<?> type) {
        return ADAPTERS.get(type);
    }

    private static <T> MyoArgumentAdapter<T> simpleAdapter(ArgumentType<?> argumentType,
                                                           ArgumentGetter<T> getter) {
        return new MyoArgumentAdapter<>() {
            @Override
            public ArgumentType<?> argumentType() {
                return argumentType;
            }

            @Override
            public T value(CommandContext<CommandSourceStack> context, String name) throws Exception {
                return getter.get(context, name);
            }
        };
    }

    @FunctionalInterface
    private interface ArgumentGetter<T> {
        T get(CommandContext<CommandSourceStack> context, String name) throws Exception;
    }

    record ParameterMapping(int index,
                            String name,
                            Class<?> contextType,
                            MyoArgumentAdapter<?> adapter) {
        static ParameterMapping context(int index, Class<?> type) {
            return new ParameterMapping(index, null, type, null);
        }

        static ParameterMapping argument(int index, String name, MyoArgumentAdapter<?> adapter) {
            return new ParameterMapping(index, name, null, adapter);
        }

        Object value(CommandContext<CommandSourceStack> context) throws Exception {
            if (contextType == CommandContext.class) {
                return context;
            }
            if (contextType == CommandSourceStack.class) {
                return context.getSource();
            }
            return adapter.value(context, name);
        }
    }
}
