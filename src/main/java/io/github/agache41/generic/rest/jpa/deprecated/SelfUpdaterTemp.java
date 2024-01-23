
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

package io.github.agache41.generic.rest.jpa.deprecated;

import io.github.agache41.generic.rest.jpa.dataAccess.PrimaryKey;
import io.github.agache41.generic.rest.jpa.update.ClassReflector;
import io.github.agache41.generic.rest.jpa.update.Updateable;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Deprecated
public interface SelfUpdaterTemp<ENTITY extends Updateable<ENTITY>> extends UpdaterTemp<ENTITY, ENTITY> {

    default <VALUE> boolean update(BiConsumer<ENTITY, VALUE> setter,
                                   Function<ENTITY, VALUE> getter,
                                   boolean notNull,
                                   ENTITY target,
                                   ENTITY source) {
        return update(setter, getter, notNull, getter, target, source);
    }


    default <VALUE extends Updateable<VALUE>> boolean updateEntity(BiConsumer<ENTITY, VALUE> setter,
                                                                   Function<ENTITY, VALUE> getter,
                                                                   boolean notNull,
                                                                   ENTITY target,
                                                                   ENTITY source) {
        return updateEntity(setter, getter, notNull, getter, target, source, Updateable::update, ClassReflector::create);
    }


    default <KEY, VALUE> boolean updateMap(Function<ENTITY, Map<KEY, VALUE>> getter,
                                           BiConsumer<ENTITY, Map<KEY, VALUE>> setter,
                                           boolean notNull,
                                           ENTITY target,
                                           ENTITY source) {
        return updateMap(getter, setter, notNull, getter, target, source);
    }


    default <KEY, VALUE extends Updateable<VALUE>> boolean updateEntityMap(Function<ENTITY, Map<KEY, VALUE>> getter,
                                                                           BiConsumer<ENTITY, Map<KEY, VALUE>> setter,
                                                                           boolean notNull,
                                                                           ENTITY target,
                                                                           ENTITY source) {
        return updateEntityMap(getter, setter, notNull, getter, target, source, Updateable::update, ClassReflector::create);
    }

    default <VALUE> boolean updateCollection(Function<ENTITY, Collection<VALUE>> getter,
                                             BiConsumer<ENTITY, Collection<VALUE>> setter,
                                             boolean notNull,
                                             ENTITY target,
                                             ENTITY source) {
        return updateCollection(getter, setter, notNull, getter, target, source);
    }


    default <VALUE extends Updateable<VALUE> & PrimaryKey<PK>, PK> boolean updateEntityCollection(Function<ENTITY, Collection<VALUE>> getter,
                                                                                                  BiConsumer<ENTITY, Collection<VALUE>> setter,
                                                                                                  boolean notNull,
                                                                                                  ENTITY target,
                                                                                                  ENTITY source) {
        return updateEntityCollection(getter, setter, notNull, getter, target, source, Updateable::update, ClassReflector::create);
    }
}
