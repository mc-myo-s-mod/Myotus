package me.myogoo.myotus.api.annotation.itemList.jei;

/**
 * Marker for integration classes that should only be processed when JEI is active.
 *
 * <p>Register this marker with {@code MyotusAPI.modRegistrar()} and annotate the
 * class that contains {@code @MyotusSubscriber} methods for JEI registration.</p>
 */
public @interface JEI {
}
