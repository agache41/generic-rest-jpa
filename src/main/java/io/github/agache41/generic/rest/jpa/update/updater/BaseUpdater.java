
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

import io.github.agache41.generic.rest.jpa.update.Updatable;

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
     * @param <KEY>       the type parameter
     * @param <VALUE>     the type parameter
     * @param targetValue the target value
     * @param sourceValue the source value
     * @param constructor the constructor
     * @return true if the target map has changed
     */
    protected static <KEY, VALUE extends Updatable<VALUE>> boolean updateMap(final Map<KEY, VALUE> targetValue,
                                                                             final Map<KEY, VALUE> sourceValue,
                                                                             final Supplier<VALUE> constructor) {

        final Set<KEY> targetKeys = targetValue.keySet();
        // make a copy to not change the input
        final Set<KEY> valueKeys = sourceValue.keySet()
                                              .stream()
                                              .collect(Collectors.toCollection(LinkedHashSet::new));
        //remove all that are now longer available
        boolean updated = targetKeys.retainAll(valueKeys);
        //update all that remained in the intersection
        updated = targetKeys.stream()
                            .map(k -> targetValue.get(k)
                                                 .update(sourceValue.get(k)))
                            .reduce(updated, (u, n) -> u || n);
        //remove those that are updated
        valueKeys.removeAll(targetKeys);
        //insert all new (that remained)
        if (!valueKeys.isEmpty()) {
            updated = valueKeys.stream()
                               .map(key -> {
                                   final VALUE newValue = constructor.get();
                                   final boolean upd = newValue.update(sourceValue.get(key));
                                   targetValue.put(key, newValue);
                                   return upd;
                               })
                               .reduce(updated, (u, n) -> u || n);
        }
        return updated;
    }

    /**
     * Internal update method for lists.
     *
     * @param <VALUE>     the type parameter
     * @param sourceValue the source value
     * @param constructor the constructor
     * @return true if the target list has changes
     */
    protected static <VALUE extends Updatable<VALUE>> List<VALUE> updateList(final List<VALUE> sourceValue,
                                                                             final Supplier<VALUE> constructor) {
        return sourceValue.stream()
                          .map(source -> {
                              final VALUE newValue = constructor.get();
                              newValue.update(source);
                              return newValue;
                          })
                          .collect(Collectors.toList());
    }

}
