
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
