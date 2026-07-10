package me.myogoo.myotus.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.myogoo.myotus.command.MyoCommandMetadata.PermissionInfo;
import me.myogoo.myotus.api.command.permission.MyoPermissionChecker;
import me.myogoo.myotus.api.command.permission.MyoPermissionLevel;
import me.myogoo.myotus.util.MyoLogger;
import net.minecraft.commands.CommandSourceStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class MyoCommandPermissions {
    private static final Map<Class<? extends MyoPermissionChecker>, MyoPermissionChecker> CHECKERS =
            new ConcurrentHashMap<>();

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
            MyoLogger.error("@MyoPermission on {} has no requirement set. Command registration was denied.", owner);
            return false;
        }
        if (count > 1) {
            MyoLogger.error("@MyoPermission on {} has multiple requirement types set. Command registration was denied.",
                    owner);
            return false;
        }
        if (permission.customChecker() != permission.defaultChecker()
                && !MyoPermissionChecker.class.isAssignableFrom(permission.customChecker())) {
            MyoLogger.error("Custom permission checker {} on {} does not implement {}. Command registration was denied.",
                    permission.customChecker().getName(), owner, MyoPermissionChecker.class.getName());
            return false;
        }
        if (permission.customChecker() != permission.defaultChecker()) {
            try {
                Class<? extends MyoPermissionChecker> checkerClass = permission.customChecker()
                        .asSubclass(MyoPermissionChecker.class);
                CHECKERS.computeIfAbsent(checkerClass, MyoCommandPermissions::createChecker);
            } catch (RuntimeException e) {
                MyoLogger.error("Custom permission checker {} on {} could not be initialized. "
                                + "Command registration was denied.",
                        permission.customChecker().getName(), owner, e);
                return false;
            }
        }
        return true;
    }

    private static boolean checkCustomPermission(CommandSourceStack source, Class<?> checkerClass) {
        try {
            Class<? extends MyoPermissionChecker> typedClass = checkerClass.asSubclass(MyoPermissionChecker.class);
            MyoPermissionChecker checker = CHECKERS.computeIfAbsent(typedClass,
                    ignored -> createChecker(typedClass));
            return checker.check(source);
        } catch (RuntimeException e) {
            MyoLogger.error("Failed to evaluate custom Myotus permission checker {}", checkerClass.getName(), e);
            return false;
        }
    }

    private static MyoPermissionChecker createChecker(Class<? extends MyoPermissionChecker> checkerClass) {
        try {
            return checkerClass.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Custom permission checker must expose a public no-argument constructor: "
                            + checkerClass.getName(),
                    e);
        }
    }
}
