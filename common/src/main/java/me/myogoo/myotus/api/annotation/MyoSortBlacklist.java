package me.myogoo.myotus.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excludes a menu or screen class from inventory sorting through supported integrations.
 *
 * <p>Apply this marker to an {@code AbstractContainerMenu} subclass to exclude
 * its server-side sorting and supported client-side sorting. A marker on a
 * {@code Screen} subclass only excludes client-side sorting for that screen;
 * it does not prevent server-side sorting. Mark the menu for server protection.</p>
 *
 * <p>Abstract base classes may also be marked. Their subclasses are excluded,
 * even when defined by another mod and not explicitly annotated. Interfaces
 * and composed annotations do not propagate this marker.</p>
 *
 * <p>Only sorting mods integrated with Myotus honor this marker; it does not
 * disable sorting performed by arbitrary mods.</p>
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyoSortBlacklist {
}
