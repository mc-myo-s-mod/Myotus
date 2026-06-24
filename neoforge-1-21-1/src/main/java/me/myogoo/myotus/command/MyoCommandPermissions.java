package me.myogoo.myotus.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.myogoo.myotus.command.MyoCommandMetadata.PermissionInfo;
import me.myogoo.myotus.api.command.permission.MyoPermissionLevel;
import me.myogoo.myotus.util.MyoLogger;
import net.minecraft.commands.CommandSourceStack;

import java.lang.reflect.Method;

final class MyoCommandPermissions {
    private MyoCommandPermissions() {
    }

    static void apply(LiteralArgumentBuilder<CommandSourceStack> builder, PermissionInfo permission) {
        builder.requires(source -> check(source, permission));
    }

    static boolean check(CommandSourceStack source, PermissionInfo permission) {
        if (permission == null) {
            return true;
        }
        if (permission.level() != null && permission.level() != MyoPermissionLevel.NONE) {
            return source.hasPermission(permission.level().getCommandPermissionLevel());
        }
        if (permission.customChecker() != permission.defaultChecker()) {
            return checkCustomPermission(source, permission.customChecker());
        }
        return true;
    }

    static boolean validate(PermissionInfo permission, String owner) {
        int count = 0;
        if (permission.level() != null && permission.level() != MyoPermissionLevel.NONE) {
            count++;
        }
        if (permission.customChecker() != permission.defaultChecker()) {
            count++;
        }

        if (count == 0) {
            MyoLogger.warn("@MyoPermission on {} has no requirement set. Ignoring.", owner);
            return false;
        }
        if (count > 1) {
            MyoLogger.error("@MyoPermission on {} has multiple requirement types set. Ignoring.", owner);
            return false;
        }
        return true;
    }

    private static boolean checkCustomPermission(CommandSourceStack source, Class<?> checkerClass) {
        try {
            Object checker = checkerClass.getDeclaredConstructor().newInstance();
            Method check = checkerClass.getMethod("check", CommandSourceStack.class);
            return Boolean.TRUE.equals(check.invoke(checker, source));
        } catch (Exception e) {
            MyoLogger.error("Failed to evaluate custom Myotus permission checker {}", checkerClass.getName(), e);
            return false;
        }
    }
}
