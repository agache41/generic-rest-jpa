package org.structured.api.quarkus.reflection;

import java.lang.annotation.*;

/**
 * This Field will be updated from the View
 * This is a marker Annotation for the fields of the Entities to be updated from a GUI request.
 * By default the values can not be nulled, so if a null value is received, it will be skipped,
 * and the previous value will be kept.
 * The notNull() set to false means that the field will be in this case to null updated.
 */
@Documented
@Target({ElementType.TYPE, ElementType.FIELD})
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Write {
    boolean notNull() default true;

    @Documented
    @Target({ElementType.TYPE, ElementType.FIELD})
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @interface excluded {

    }
}
