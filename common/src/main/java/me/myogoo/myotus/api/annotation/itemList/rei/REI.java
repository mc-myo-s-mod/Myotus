package me.myogoo.myotus.api.annotation.itemList.rei;

import me.myogoo.myotus.api.annotation.MyoMod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker for integration classes that should only be processed when REI is active.
 *
 * <p>This marker is discovered through {@link MyoMod}; annotate the class that
 * contains {@code @MyotusSubscriber} methods for REI registration.</p>
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@MyoMod("roughlyenoughitems")
public @interface REI {
}
