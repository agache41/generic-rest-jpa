
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

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * The updater for simple value types (String, Integer).
 * It updates the field value in the target based on the value of the field value in the source
 *
 * @param <TO>     the type parameter of the source object
 * @param <ENTITY> the type parameter of the target object
 * @param <VALUE>  the type parameter of the updating value
 */
public class ValueUpdater<TO, ENTITY, VALUE> implements Updater<TO, ENTITY> {

    /**
     * The entity getter.
     */
    protected final Function<ENTITY, VALUE> entityGetter;

    /**
     * The entity setter.
     */
    protected final BiConsumer<ENTITY, VALUE> entitySetter;

    /**
     * If the update should be dynamic processed and nulls will be ignored.
     */
    protected final boolean dynamic;
    /**
     * The transfer object getter.
     */
    protected final Function<TO, VALUE> toGetter;

    /**
     * The transfer object getter.
     */
    protected final BiConsumer<TO, VALUE> toSetter;

    /**
     * Instantiates a new Value updater.
     *
     * @param entityGetter the entity getter
     * @param entitySetter the entity setter
     * @param dynamic      if the update should be dynamic processed and nulls will be ignored
     * @param toGetter     the transfer object getter
     * @param toSetter     the transfer object setter
     */
    public ValueUpdater(final Function<ENTITY, VALUE> entityGetter,
                        final BiConsumer<ENTITY, VALUE> entitySetter,
                        final boolean dynamic,
                        final Function<TO, VALUE> toGetter,
                        final BiConsumer<TO, VALUE> toSetter) {
        this.entityGetter = entityGetter;
        this.entitySetter = entitySetter;
        this.dynamic = dynamic;
        this.toGetter = toGetter;
        this.toSetter = toSetter;
    }

    /**
     * Convenient static method.
     * It updates the field value in the entity based on the value of the field value in the transfer object.
     *
     * @param <E>            the type parameter of the entity object
     * @param <T>            the type parameter of the transfer object
     * @param <V>            the type parameter of the value
     * @param entityGetter   the entity getter
     * @param entitySetter   the entity setter
     * @param notNull        if the update should be dynamic processed and nulls will be ignored
     * @param toGetter       the entity getter
     * @param toSetter       the transfer object setter
     * @param transferObject the transfer object
     * @param entity         the entity
     * @return true if the target changed
     */
    public static <E, T, V> boolean updateValue(
            final Function<E, V> entityGetter,
            final BiConsumer<E, V> entitySetter,
            final boolean notNull,
            final Function<T, V> toGetter,
            final BiConsumer<T, V> toSetter,
            final T transferObject,
            final E entity
    ) {
        return new ValueUpdater<>(entityGetter, entitySetter, notNull, toGetter, toSetter).update(transferObject, entity);
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
    protected static <KEY, VALUE extends Updatable<VALUE>> boolean updateMap(
            final Map<KEY, VALUE> targetValue,
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
     * @param <KEY>       the type parameter
     * @param <VALUE>     the type parameter
     * @param sourceValue the source value
     * @param constructor the constructor
     * @return true if the target list has changes
     */
    protected static <KEY, VALUE extends Updatable<VALUE>> List<VALUE> updateList(
            final List<VALUE> sourceValue,
            final Supplier<VALUE> constructor) {
        return sourceValue.stream()
                          .map(source -> {
                              final VALUE newValue = constructor.get();
                              newValue.update(source);
                              return newValue;
                          })
                          .collect(Collectors.toList());
    }

    /**
     * The method updates the field in target based on the field the source
     *
     * @param entity         the target
     * @param transferObject the source
     * @return true if the target changed
     */
    @Override
    public boolean update(final TO transferObject,
                          final ENTITY entity
    ) {
        // the toValue to be updated
        final VALUE sourceValue = this.toGetter.apply(transferObject);
        // nulls
        if (sourceValue == null) {
            // null ignore or both null
            if (this.dynamic || this.entityGetter.apply(entity) == null) {
                return false;
            }
            this.entitySetter.accept(entity, null);
            return true;
        }
        // nulls

        if (!Objects.equals(this.entityGetter.apply(entity), sourceValue)) {
            // equals check
            this.entitySetter.accept(entity, sourceValue);
            return true;
        } // otherwise no update
        return false;
    }
}
