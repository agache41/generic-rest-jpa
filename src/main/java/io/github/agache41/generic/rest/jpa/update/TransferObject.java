
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

import io.github.agache41.generic.rest.jpa.update.reflector.ClassReflector;
import jakarta.persistence.Transient;

/**
 * The interface Transfer object.
 *
 * @param <TO>     the transfer object type parameter
 * @param <ENTITY> the entity type parameter
 */
public interface TransferObject<TO extends TransferObject<TO, ENTITY>, ENTITY> {


    /**
     * Updates the entity fields with values from this object
     * Is to be used in POST - create request
     * The returned entity is to be inserted or merged in the db.
     *
     * @param entity  the entity
     * @param context the context
     * @return entity entity
     */
    @Transient
    default ENTITY create(final ENTITY entity,
                          final Object context) {
        this.update(entity, context);
        return entity;
    }


    /**
     * Updates the entity fields with values from this object
     * Is to be used in PUT - update request
     * The returned entity is to be updated or merged in the db.
     *
     * @param entity  the entity
     * @param context the context
     * @return entity boolean
     */
    @Transient
    default boolean update(final ENTITY entity,
                           final Object context) {
        return ClassReflector.ofObject(this, entity)
                             .update(this, entity, context);
    }


    /**
     * Updates the fields in TO from the entity.
     * Is to be used in GET - reder request
     *
     * @param entity  the entity
     * @param context the context
     * @return to to
     */
    @Transient
    default TO render(final ENTITY entity,
                      final Object context) {
        ClassReflector.ofObject(this, entity)
                      .render(this, entity, context);
        return (TO) this;
    }

    /**
     * Tells if the two objects are equal from the Update perspective.
     *
     * @param source the source
     * @return the boolean
     */
    @Transient
    default boolean updateEquals(final Object source) {
        return ClassReflector.ofObject(this)
                             .areEqual(this, source);
    }
}
