
/*
 *    Copyright 2022-2023  Alexandru Agache
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.agache41.generic.rest.jpa.update;

import java.lang.annotation.*;

/**
 * <pre>
 * Marker annotation for fields and classes, implementing the update from transfer object pattern.
 * This is a marker Annotation for the fields of the Entities to be updated from a GUI request.
 *
 * When a field is annotated, it will be updated from the provided source when the update method is called.
 * When used on the class, all fields will be updated, except the ones annotated with @ {@link Update.excluded} annotation.
 *
 * By default the values can not be set to null, so if a null value is received, it will be skipped,
 * and the previous value will be kept.
 * The {@link Update#dynamic()}  set to false means that the field will be updated also when a null value is provided.
 * This is only recommended to be used when the transfer object is always complete.
 * </pre>
 */
@Documented
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Update {
    int defaultLength = -1;

    /**
     * <pre>
     * A dynamic update process happens when only the fields that are present in the source object are being updated.
     * The ones that are not set, and are null in the transfer object will be ignored.
     * Only non null values will be updated.
     *
     * If set to false, this will cause null values to be written to the fields.
     * This can be use only in updates where the complete update object is being sent (non dynamic)
     *
     * The value has impact on the update process.
     * Default is true.
     * </pre>
     *
     * @return the boolean
     */
    boolean dynamic() default true;

    /**
     * <pre>
     * Informal value,
     * Tells if the field allows nulls.
     * The value has no impact on the update process.
     * Default is true.
     * </pre>
     *
     * @return the boolean
     */
    boolean nullable() default true;

    /**
     * <pre>
     * Informal value,
     * Tells if the field can be updated.
     * The value has no impact on the update process.
     * Default is true.
     * </pre>
     *
     * @return the boolean
     */
    boolean updatable() default true;


    /**
     * <pre>
     * Informal value,
     * Tells if the field can be inserted.
     * The value has no impact on the update process.
     * Default is true.
     * </pre>
     *
     * @return the boolean
     */
    boolean insertable() default true;

    /**
     * <pre>
     * Informal value, telling the maximum allowed length of the field for String fields or
     * the maximum number of elements for an array or collection Type.
     * The value has no impact on the update process.
     * Default is -1.
     * </pre>
     *
     * @return the length
     */
    int length() default defaultLength;

    /**
     * <pre>
     * Indicates that this field is to be skipped in update.
     * Used in conjunction with the @ {@link Update} Annotation placed at class level.
     * </pre>
     */
    @Documented
    @Target({ElementType.FIELD, ElementType.METHOD})
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @interface excluded {

    }
}
