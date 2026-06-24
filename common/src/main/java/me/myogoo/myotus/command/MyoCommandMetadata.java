package me.myogoo.myotus.command;

import me.myogoo.myotus.api.annotation.MyoDebug;
import me.myogoo.myotus.api.annotation.commands.MyoAlias;
import me.myogoo.myotus.api.annotation.commands.MyoArgument;
import me.myogoo.myotus.api.annotation.commands.MyoCommand;
import me.myogoo.myotus.api.annotation.commands.MyoExecute;
import me.myogoo.myotus.api.annotation.commands.MyoPermission;
import me.myogoo.myotus.api.command.permission.MyoPermissionLevel;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

final class MyoCommandMetadata {
    private MyoCommandMetadata() {
    }

    static CommandInfo getCommandInfo(Class<?> clazz) {
        MyoCommand command = clazz.getAnnotation(MyoCommand.class);
        if (command != null) {
            return new CommandInfo(command.value(), command.parent());
        }
        return null;
    }

    static ExecuteInfo getExecuteInfo(Method method) {
        MyoExecute execute = method.getAnnotation(MyoExecute.class);
        if (execute != null) {
            return new ExecuteInfo(execute.value());
        }
        return null;
    }

    static String getArgumentName(Parameter parameter) {
        MyoArgument argument = parameter.getAnnotation(MyoArgument.class);
        if (argument != null) {
            return argument.value();
        }
        return null;
    }

    static AliasInfo getAliasInfo(Class<?> clazz) {
        MyoAlias alias = clazz.getAnnotation(MyoAlias.class);
        if (alias != null) {
            return new AliasInfo(alias.value());
        }
        return null;
    }

    static PermissionInfo getPermissionInfo(AnnotatedElement element) {
        MyoPermission permission = element.getAnnotation(MyoPermission.class);
        if (permission != null) {
            return new PermissionInfo(permission.permission(), permission.custom(), Void.class,
                    permission.propagate());
        }
        return null;
    }

    static boolean isDebugOnly(Class<?> clazz) {
        return clazz.isAnnotationPresent(MyoDebug.class);
    }

    record CommandInfo(String value, Class<?> parent) {
    }

    record ExecuteInfo(String path) {
    }

    record AliasInfo(String[] values) {
    }

    record PermissionInfo(MyoPermissionLevel level,
                          Class<?> customChecker,
                          Class<?> defaultChecker,
                          boolean propagate) {
    }
}
