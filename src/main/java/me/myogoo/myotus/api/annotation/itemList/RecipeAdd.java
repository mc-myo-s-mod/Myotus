package me.myogoo.myotus.api.annotation.itemList;

/**
 * Marker for classes that contribute recipe registration entries to an item-list integration.
 *
 * <p>This marker is intended to be combined with an integration marker such as
 * {@code @JEI}, {@code @EMI}, or {@code @REI}, plus one or more
 * {@link me.myogoo.myotus.api.annotation.MyotusSubscriber} methods.</p>
 */
public @interface RecipeAdd {
}
