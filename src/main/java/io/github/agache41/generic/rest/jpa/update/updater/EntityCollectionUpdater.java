
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

import io.github.agache41.generic.rest.jpa.dataAccess.PrimaryKey;
import io.github.agache41.generic.rest.jpa.update.Updateable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The updater for collection of entity types (implementing PrimaryKey and Updatable).
 * It updates the field value in the target based on the value of the field value in the source.
 *
 * @param <TARGET>     the type parameter
 * @param <SOURCE>     the type parameter
 * @param <COLLECTION> the type parameter
 * @param <VALUE>      the type parameter
 * @param <PK>         the type parameter
 */
public class EntityCollectionUpdater<TARGET, SOURCE, COLLECTION extends Collection<VALUE>, VALUE extends Updateable<VALUE> & PrimaryKey<PK>, PK> extends ValueUpdater<TARGET, SOURCE, COLLECTION> {

    /**
     * The Constructor.
     */
    protected final Supplier<VALUE> constructor;

    /**
     * Instantiates a new Entity collection updater.
     *
     * @param setter       the target setter
     * @param getter       the target getter
     * @param notNull      if entity collection can be null
     * @param sourceGetter the source getter
     * @param constructor  the entity constructor
     */
    public EntityCollectionUpdater(final BiConsumer<TARGET, COLLECTION> setter,
                                   final Function<TARGET, COLLECTION> getter,
                                   final boolean notNull,
                                   final Function<SOURCE, COLLECTION> sourceGetter,
                                   final Supplier<VALUE> constructor) {
        super(setter, getter, notNull, sourceGetter);
        this.constructor = constructor;
    }

    /**
     * Convenient static method.
     * It updates the field value in the target based on the value of the field value in the source.
     *
     * @param <T>          the type parameter of the target object
     * @param <S>          the type parameter of the source object
     * @param <E>          the type parameter of the collection values (the entity)
     * @param <K>          the type parameter of the primary key of the entity
     * @param setter       the target setter
     * @param getter       the target getter
     * @param notNull      if values is not null
     * @param sourceGetter the source getter
     * @param target       the target
     * @param source       the source
     * @return true if the target changed
     */
    public static <T, S, C extends Collection<E>, E extends Updateable<E> & PrimaryKey<K>, K> boolean updateEntityCollection(
            final BiConsumer<T, C> setter,
            final Function<T, C> getter,
            final boolean notNull,
            final Function<S, C> sourceGetter,
            final Supplier<E> constructor,
            final T target,
            final S source) {
        return new EntityCollectionUpdater<>(setter, getter, notNull, sourceGetter, constructor).update(target, source);
    }

    /**
     * The method updates the field in target based on the field the source
     *
     * @param target the target
     * @param source the source
     * @return true if the target changed
     */
    @Override
    public boolean update(final TARGET target,
                          final SOURCE source) {
        // the sourceValue to be updated
        final COLLECTION sourceValue = this.sourceGetter.apply(source);
        // nulls
        if (sourceValue == null) {
            if (this.notNull || this.getter.apply(target) == null) // null ignore
            {
                return false;
            } else {
                this.setter.accept(target, null);
                return true;
            }
        }
        final Collection<VALUE> targetValue = this.getter.apply(target);
        // collection not initialized
        if (targetValue == null) {
            this.setter.accept(target, sourceValue);
            return true;
        }
        // empty
        if (sourceValue.isEmpty()) {
            if (targetValue.isEmpty()) {
                return false;
            }
            targetValue.clear();
            return true;
        }
        // empty

        // collection work

        // create maps with entities that have pk.
        final Map<PK, VALUE> sourceValueMap = new LinkedHashMap<>();
        final List<VALUE> sourceValueList = new ArrayList<>(sourceValue.size());
        for (final VALUE val : sourceValue) {
            final PK PK = val.getId();
            if (PK == null) {
                sourceValueList.add(val);
            } else {
                sourceValueMap.put(PK, val);
            }
        }

        final Map<PK, VALUE> targetValueMap = new LinkedHashMap<>();
        for (final VALUE val : targetValue) {
            final PK PK = val.getId();
            if (PK != null) {
                targetValueMap.put(PK, val);
            }
        }

        ValueUpdater.updateMap(targetValueMap, sourceValueMap, this.constructor);

        targetValue.clear();
        // add the updated entities
        targetValue.addAll(targetValueMap.values());
        // add the new ones
        targetValue.addAll(sourceValueList);
        // collection work
        return true;
    }
}
