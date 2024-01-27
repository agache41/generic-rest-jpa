
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

/**
 * @author Alexandru.Agache.Extern@atruvia.de
 * <p>
 * Generic Interface for updatable Database Entities
 * It consists of one method to be implemented in the entity to coordinate the update process
 * and default working methods that update single default types fields and, respectively
 * map based one to many relations.
 */
public interface Updateable<ENTITY extends Updateable<ENTITY>> {
    default boolean update(final ENTITY source) {
        return ClassReflector.ofObject(this)
                             .update(this, source);
    }
}
