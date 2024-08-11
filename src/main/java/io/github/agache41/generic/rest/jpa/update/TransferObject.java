
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

import jakarta.persistence.Transient;

/**
 * The interface Transfer object.
 *
 * @param <TO>     the transfer object type parameter
 * @param <ENTITY> the entity type parameter
 */
public interface TransferObject<TO, ENTITY> {


    /**
     * Updates the entity fields with values from this object
     * Is to be used in POST - create request
     * The returned entity is to be inserted or merged in the db.
     *
     * @param entity the entity
     * @return entity
     */
    @Transient
    ENTITY create(ENTITY entity);


    /**
     * Updates the entity fields with values from this object
     * Is to be used in PUT - update request
     * The returned entity is to be updated or merged in the db.
     *
     * @param entity the entity
     * @return entity
     */
    @Transient
    boolean update(ENTITY entity);


    /**
     * Updates the fields in TO from the entity.
     * Is to be used in GET - reder request
     *
     * @param entity the entity
     * @return to
     */
    @Transient
    TO render(ENTITY entity);

}
