
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

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * The updater for map with simple value types (String, Integer).
 * It updates the field value in the target based on the value of the field value in the source.
 *
 * @param <TO>     the type parameter of the source object
 * @param <ENTITY> the type parameter of the target object
 * @param <VALUE>  the type parameter of the map value
 * @param <KEY>    the type parameter of the map key
 */
public class MapUpdater<TO, ENTITY, VALUE, KEY> extends ValueUpdater<TO, ENTITY, Map<KEY, VALUE>> {


    /**
     * Instantiates a new Map updater.
     *
     * @param toGetter the to getter
     * @param toSetter the to setter
     * @param dynamic  if the update should be dynamic processed and nulls will be ignored
     * @param enGetter the en getter
     * @param enSetter the en setter
     */
    public MapUpdater(final Function<TO, Map<KEY, VALUE>> toGetter,
                      final BiConsumer<TO, Map<KEY, VALUE>> toSetter,
                      final boolean dynamic,
                      final Function<ENTITY, Map<KEY, VALUE>> enGetter,
                      final BiConsumer<ENTITY, Map<KEY, VALUE>> enSetter) {
        super(toGetter, toSetter, dynamic, enGetter, enSetter);
    }


    /**
     * Convenient static method.
     * It updates the field value in the target based on the value of the field value in the source.
     *
     * @param <T>      the type parameter of the target object
     * @param <S>      the type parameter of the source object
     * @param <V>      the type parameter of the map value
     * @param <K>      the type parameter of the map key
     * @param toGetter the target toGetter
     * @param toSetter the target toSetter
     * @param dynamic  if the update should be dynamic processed and nulls will be ignored
     * @param enGetter the source toGetter
     * @param enSetter the en setter
     * @param target   the target
     * @param source   the source
     * @return true if the target changed
     */
    public static <T, S, V, K> boolean updateMap(final Function<T, Map<K, V>> toGetter,
                                                 final BiConsumer<T, Map<K, V>> toSetter,
                                                 final boolean dynamic,
                                                 final Function<S, Map<K, V>> enGetter,
                                                 final BiConsumer<S, Map<K, V>> enSetter,
                                                 final T target,
                                                 final S source) {
        return new MapUpdater<>(toGetter, toSetter, dynamic, enGetter, enSetter).update(target, source);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean update(final TO transferObject,
                          final ENTITY entity) {
        // the toMap to be updated
        final Map<KEY, VALUE> toMap = this.toGetter.apply(transferObject);
        // nulls
        if (toMap == null) {
            if (this.dynamic || this.entityGetter.apply(entity) == null) {
                return false;
            } else {
                this.warnNullMap(transferObject);
                this.entitySetter.accept(entity, null);
                return true;
            }
        }
        final Map<KEY, VALUE> enMap = this.entityGetter.apply(entity);
        // map not initialized
        if (enMap == null) {
            this.warnNullMap(entity);
            this.entitySetter.accept(entity, toMap);
            return true;
        }
        // empty
        if (toMap.isEmpty()) {
            if (enMap.isEmpty()) {
                return false;
            }
            enMap.clear();
            return true;
        }
        // map work
        final Set<KEY> enKeys = enMap.keySet();
        // make a copy to not change the input
        final Set<KEY> toKeys = new HashSet<>(toMap.keySet());
        //remove all that are now longer available
        boolean updated = enKeys.retainAll(toKeys);
        //update all that remained in the intersection
        updated |= enKeys.stream() // process only the ones that are not equal
                         .filter(key -> !Objects.equals(enMap.get(key), toMap.get(key)))
                         // take those from the input in the old map
                         .map(key -> enMap.put(key, toMap.get(key)))
                         .count() > 0;
        //remove those that are updated
        toKeys.removeAll(enKeys);
        //insert all new (that remained)
        if (!toKeys.isEmpty()) {
            toKeys.forEach(key -> enMap.put(key, toMap.get(key)));
            updated = true;
        }
        //set it again
        if (updated) {
            this.entitySetter.accept(entity, enMap);
        }
        return updated;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(final TO transferObject,
                       final ENTITY entity) {
        final Map<KEY, VALUE> enValue = this.entityGetter.apply(entity);
        // no data
        if (enValue == null) {
            // no data no fun
            this.warnNullMap(entity);
            return;
        }
        // the sourceValue to be updated
        final Map<KEY, VALUE> toValue = this.toGetter.apply(transferObject);
        // nulls
        if (toValue == null) {
            this.warnNullMap(transferObject);
            this.toSetter.accept(transferObject, enValue);
            return;
        }
        if (enValue.isEmpty()) {
            return;
        }
        toValue.putAll(enValue);
    }
}
