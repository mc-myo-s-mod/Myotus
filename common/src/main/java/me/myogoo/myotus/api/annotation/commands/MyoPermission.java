package me.myogoo.myotus.api.annotation.commands;

import me.myogoo.myotus.api.command.permission.MyoPermissionLevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MyoPermission {
    String[] value() default {};

    MyoPermissionLevel permission() default MyoPermissionLevel.NONE;

    Class<?> custom() default Void.class;

    boolean propagate() default false;
}
