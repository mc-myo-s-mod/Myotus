package me.myogoo.myotus.api.annotation;


import me.myogoo.myotus.api.integration.MyoCustomCondition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyoMod {
    /**
     * ModId
     */
    String value();

    /**
     * Stable identifier for this integration when multiple integrations share the same mod ID.
     */
    String alias() default "";

    String versionRange() default "*";
    IntegrationMode mode() default IntegrationMode.ONLY;
    Class<? extends MyoCustomCondition> customCondition() default MyoCustomCondition.class;

    enum IntegrationMode {
        OVERRIDE,
        ONLY,
        EXTENDED
    }
}

