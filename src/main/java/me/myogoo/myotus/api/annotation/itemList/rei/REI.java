package me.myogoo.myotus.api.annotation.itemList.rei;

/**
 * Marker for integration classes that should only be processed when REI is active.
 *
 * <p>Register this marker with {@code MyotusAPI.modRegistrar()} and annotate the
 * class that contains {@code @MyotusSubscriber} methods for REI registration.</p>
 */
public @interface REI {
}
