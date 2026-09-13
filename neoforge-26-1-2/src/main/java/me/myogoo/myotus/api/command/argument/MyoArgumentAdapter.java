package me.myogoo.myotus.api.command.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;

public interface MyoArgumentAdapter<T> {
    ArgumentType<?> argumentType();

    T value(CommandContext<CommandSourceStack> context, String name) throws Exception;
}
