package me.myogoo.myotus.api.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a command method parameter as a Brigadier argument.
 *
 * <p>The annotated value becomes the argument name in the generated command tree.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * @MyoExecute
 * public static int execute(@MyoArgument("amount") int amount) {
 *     return 1;
 * }
 * }</pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyoArgument {
    /**
     * Returns the argument name as it should appear in the command syntax.
     *
     * @return Brigadier argument name
     */
    String value();
}
