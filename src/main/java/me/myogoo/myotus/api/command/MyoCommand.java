package me.myogoo.myotus.api.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a Myotus-discovered command node.
 *
 * <p>If {@link #parent()} is left as {@code void.class}, the command becomes a
 * root command. Otherwise it is attached as a subcommand of the specified parent.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * @MyoCommand("myotus")
 * public final class RootCommand {
 * }
 *
 * @MyoCommand(value = "mods", parent = RootCommand.class)
 * public final class ModsCommand {
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyoCommand {
    /**
     * Returns the literal name of this command node.
     *
     * @return command literal, for example {@code "mods"}
     */
    String value();

    /**
     * Returns the parent command class.
     *
     * @return parent command class, or {@code void.class} for a root command
     */
    Class<?> parent() default void.class;
}
