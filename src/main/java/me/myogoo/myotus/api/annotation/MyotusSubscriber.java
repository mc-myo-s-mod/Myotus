package me.myogoo.myotus.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a static subscriber method discovered on an integration class.
 *
 * <p>Myotus invokes these methods while processing item-list integrations such
 * as JEI, EMI, or REI. The annotated method must be {@code static} and must
 * declare exactly one parameter of the registration type requested by the
 * integration loader.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * @JEI
 * public final class ExampleJeiRegistration {
 *     @MyotusSubscriber
 *     public static void register(IRecipeRegistration registration) {
 *         // Register JEI content here.
 *     }
 * }
 * }</pre>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MyotusSubscriber {
}
