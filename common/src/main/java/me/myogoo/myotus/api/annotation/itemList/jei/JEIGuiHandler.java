package me.myogoo.myotus.api.annotation.itemList.jei;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker for JEI GUI handler integration classes discovered by Myotus.
 *
 * <p>Use this when a class contributes JEI-specific GUI area or screen handler
 * registrations through {@code @MyotusSubscriber} methods.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JEIGuiHandler {
}
