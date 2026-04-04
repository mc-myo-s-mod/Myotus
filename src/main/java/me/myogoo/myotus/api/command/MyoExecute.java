package me.myogoo.myotus.api.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the static method invoked when a generated command node executes.
 *
 * <p>The method must be {@code static}. Supported parameter types currently include
 * {@code CommandContext}, {@code CommandSourceStack}, {@code int}, {@code boolean},
 * {@code String}, {@code ServerPlayer}, and {@code Entity}. Non-context parameters
 * must also be annotated with {@link MyoArgument}.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * @MyoExecute
 * public static int execute(CommandSourceStack source, @MyoArgument("enabled") boolean enabled) {
 *     return 1;
 * }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyoExecute {
}
