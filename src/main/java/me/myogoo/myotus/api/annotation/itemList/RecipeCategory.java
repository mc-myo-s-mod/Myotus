package me.myogoo.myotus.api.annotation.itemList;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker for classes that define recipe categories or catalysts for an item-list integration.
 *
 * <p>Typical usage maps to category-style registrations such as JEI recipe catalysts
 * or EMI recipe categories.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RecipeCategory {
}
