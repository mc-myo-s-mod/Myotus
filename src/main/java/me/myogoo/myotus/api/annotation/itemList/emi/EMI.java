package me.myogoo.myotus.api.annotation.itemList.emi;

/**
 * Marker for integration classes that should only be processed when EMI is active.
 *
 * <p>Register this marker with {@code MyotusAPI.modRegistrar()} and annotate the
 * class that contains {@code @MyotusSubscriber} methods for EMI registration.</p>
 */
public @interface EMI {
}
