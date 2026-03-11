package me.myogoo.myotus.api.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define a root command or a subcommand.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyoCommand {
    /**
     * @return The name of the command (e.g., "test" for /test)
     */
    String value();

    /**
     * @return The parent command class. If not specified, it is treated as a root
     *         command.
     */
    Class<?> parent() default void.class;
}
