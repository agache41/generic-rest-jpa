package org.structured.api.quarkus.reflection;

import java.lang.annotation.*;

/**
 * <pre>
 * Marker annotation for fields and classes, implementing the update from transfer object pattern.
 * This is a marker Annotation for the fields of the Entities to be updated from a GUI request.
 *
 * When a field is annotated, it will be updated from the provided source when the update method is called.
 * When used on the class, all fields will be updated, except the ones annotated with @Write.exclude
 *
 * By default the values can not be set to null, so if a null value is received, it will be skipped,
 * and the previous value will be kept.
 * The notNull() set to false means that the field will be updated also when a null value is provided.
 * This is only recommended to be used when the transfer object is always complete.
 * </pre>
 */
@Documented
@Target({ElementType.TYPE, ElementType.FIELD})
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Write {
    /**
     * <pre>
     * Indicates that a set to null operation is allowed.
     * </pre>
     *
     * @return the boolean
     */
    boolean notNull() default true;

    /**
     * <pre>
     * Indicates that this field is to be skipped in update.
     * </pre>
     */
    @Documented
    @Target({ElementType.FIELD})
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @interface excluded {

    }
}
