package me.myogoo.myotus.api.annotation.itemList;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker for classes that provide recipe-transfer style handlers to an item-list integration.
 *
 * <p>Typical usage maps to handler registrations such as JEI recipe transfer
 * handlers or EMI recipe handlers.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RecipeTransfer {
}
