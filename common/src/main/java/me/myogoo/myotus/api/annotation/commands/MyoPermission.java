package me.myogoo.myotus.api.annotation.commands;

import me.myogoo.myotus.api.command.permission.MyoPermissionLevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MyoPermission {
    /** Built-in command permission level. Do not combine this with {@link #custom()}. */
    MyoPermissionLevel permission() default MyoPermissionLevel.NONE;

    /**
     * Custom checker class. The class must implement the loader-specific
     * {@code me.myogoo.myotus.api.command.permission.MyoPermissionChecker} interface and expose a public
     * no-argument constructor. Do not combine this with {@link #permission()}.
     */
    Class<?> custom() default Void.class;

    /**
     * On command classes, applies the requirement to all descendant command nodes. When false, the
     * requirement applies only to execute methods declared by that class.
     */
    boolean propagate() default false;
}
