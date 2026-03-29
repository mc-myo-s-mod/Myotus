package me.myogoo.myotus.api.annotation.itemList.rei;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker for integration classes that should only be processed when REI is active.
 *
 * <p>Register this marker with {@code MyotusAPI.modRegistrar()} and annotate the
 * class that contains {@code @MyotusSubscriber} methods for REI registration.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface REI {
}
