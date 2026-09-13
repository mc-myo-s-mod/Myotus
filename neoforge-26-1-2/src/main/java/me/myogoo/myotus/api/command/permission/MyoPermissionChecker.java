package me.myogoo.myotus.api.command.permission;

import net.minecraft.commands.CommandSourceStack;

@FunctionalInterface
public interface MyoPermissionChecker {
    boolean check(CommandSourceStack source);
}
