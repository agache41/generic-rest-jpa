
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

import io.github.agache41.generic.rest.jpa.update.TransferObject;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * The updater for simple value types (String, Integer).
 * It updates the field value in the entity based on the value of the field value in the transfer object
 *
 * @param <TO>      the type parameter of the transfer object
 * @param <ENTITY>  the type parameter of the entity
 * @param <TOVALUE> the type parameter
 * @param <ENVALUE> the type parameter
 */
public abstract class BaseUpdater<TO, ENTITY, TOVALUE, ENVALUE> implements Updater<TO, ENTITY> {
    /**
     * The transfer object getter.
     */
    protected final Function<TO, TOVALUE> toGetter;

    /**
     * The transfer object getter.
     */
    protected final BiConsumer<TO, TOVALUE> toSetter;

    /**
     * If the update should be dynamic processed and nulls will be ignored.
     */
    protected final boolean dynamic;

    /**
     * The entity getter.
     */
    protected final Function<ENTITY, ENVALUE> entityGetter;

    /**
     * The entity setter.
     */
    protected final BiConsumer<ENTITY, ENVALUE> entitySetter;

    /**
     * Instantiates a new Value updater.
     *
     * @param toGetter     the transfer object getter
     * @param toSetter     the transfer object setter
     * @param dynamic      if the update should be dynamic processed and nulls will be ignored
     * @param entityGetter the entity getter
     * @param entitySetter the entity setter
     */
    public BaseUpdater(final Function<TO, TOVALUE> toGetter,
                       final BiConsumer<TO, TOVALUE> toSetter,
                       final boolean dynamic,
                       final Function<ENTITY, ENVALUE> entityGetter,
                       final BiConsumer<ENTITY, ENVALUE> entitySetter

    ) {
        this.toGetter = toGetter;
        this.toSetter = toSetter;
        this.dynamic = dynamic;
        this.entityGetter = entityGetter;
        this.entitySetter = entitySetter;
    }


    /**
     * Internal update method for maps.
     *
     * @param <KEY>              the type parameter
     * @param <TVALUE>           the type parameter
     * @param <ENVALUE>          the type parameter
     * @param toMap              the source value
     * @param enMap              the target value
     * @param enValueConstructor the enValueConstructor
     * @return true if the target map has changed
     */
    protected static <KEY, TVALUE extends TransferObject<TVALUE, ENVALUE>, ENVALUE> boolean updateMap(final Map<KEY, TVALUE> toMap,
                                                                                                      final Map<KEY, ENVALUE> enMap,
                                                                                                      final Supplier<ENVALUE> enValueConstructor) {

        final Set<KEY> enKeys = enMap.keySet();
        // make a copy to not change the input
        final Set<KEY> toKeys = toMap.keySet()
                                     .stream()
                                     .collect(Collectors.toCollection(LinkedHashSet::new));
        //remove all that are now longer available
        boolean updated = enKeys.retainAll(toKeys);
        //update all that remained in the intersection
        updated = enKeys.stream()
                        .map(k -> toMap.get(k)
                                       .update(enMap.get(k)))
                        .reduce(updated, (u, n) -> u || n);
        //remove those that are updated
        toKeys.removeAll(enKeys);
        //insert all new (that remained)
        if (!toKeys.isEmpty()) {
            updated = toKeys.stream()
                            .map(key -> {
                                final ENVALUE newValue = enValueConstructor.get();
                                final boolean upd = toMap.get(key)
                                                         .update(newValue);
                                enMap.put(key, newValue);
                                return upd;
                            })
                            .reduce(updated, (u, n) -> u || n);
        }
        return updated;
    }

    /**
     * Internal update method for lists.
     *
     * @param <TVALUE>           the type parameter
     * @param <ENVALUE>          the type parameter
     * @param toList             the to list
     * @param enValueConstructor the enValueConstructor
     * @return true if the target list has changes
     */
    protected static <TVALUE extends TransferObject<TVALUE, ENVALUE>, ENVALUE> List<ENVALUE> updateList(final List<TVALUE> toList,
                                                                                                        final Supplier<ENVALUE> enValueConstructor) {
        return toList.stream()
                     .map(source -> {
                         final ENVALUE newValue = enValueConstructor.get();
                         source.update(newValue);
                         return newValue;
                     })
                     .collect(Collectors.toList());
    }

}
