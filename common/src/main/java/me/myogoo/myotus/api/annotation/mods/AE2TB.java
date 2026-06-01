package me.myogoo.myotus.api.annotation.mods;

import me.myogoo.myotus.api.annotation.MyoMod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker for classes that should only load when the AE2TB integration is active.
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@MyoMod("ae2tb")
public @interface AE2TB {
}
