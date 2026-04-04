package me.myogoo.myotus.api.annotation.itemList;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker for classes that contribute recipe registration entries to an item-list integration.
 *
 * <p>This marker is intended to be combined with an integration marker such as
 * {@code @JEI}, {@code @EMI}, or {@code @REI}, plus one or more
 * {@link me.myogoo.myotus.api.annotation.MyotusSubscriber} methods.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RecipeAdd {
}
