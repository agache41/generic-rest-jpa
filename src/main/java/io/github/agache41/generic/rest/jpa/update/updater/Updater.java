
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

package io.github.agache41.generic.rest.jpa.update.updater;

/**
 * The Updater defines a single method meant to update a target based on data coming from a source.
 * The update process happens at field level and the implementing classes treat the cases for simpek types, collections ...
 *
 * @param <TO>     the type parameter
 * @param <ENTITY> the type parameter
 */
public interface Updater<TO, ENTITY> {
    /**
     * The method updates the fields in entity based on the fields from the transfer object
     *
     * @param transferObject the transfer object
     * @param entity         the entity
     * @return if the update introduced changes in the target
     */
    boolean update(TO transferObject,
                   ENTITY entity);


    /**
     * The method updates the fields in transfer object based on the fields from the entity
     *
     * @param transferObject the transfer object
     * @param entity         the entity
     * @return the boolean
     */
//    boolean render(TO transferObject,
//                   ENTITY entity);
}
