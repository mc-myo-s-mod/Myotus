package me.myogoo.myotus.api.annotation.itemList.jei;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker for integration classes that should only be processed when JEI is active.
 *
 * <p>Register this marker with {@code MyotusAPI.modRegistrar()} and annotate the
 * class that contains {@code @MyotusSubscriber} methods for JEI registration.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JEI {
}
