package me.myogoo.myotus.api.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the name of the argument in the command node tree.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyoArgument {
    /**
     * @return The name of the argument as it appears in the command structure
     */
    String value();
}
