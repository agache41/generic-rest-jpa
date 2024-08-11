
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
import io.github.agache41.generic.rest.jpa.update.Updatable;

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
public class EntityCollectionUpdater<TARGET, SOURCE, COLLECTION extends Collection<VALUE>, VALUE extends Updatable<VALUE> & PrimaryKey<PK>, PK> extends ValueUpdater<TARGET, SOURCE, COLLECTION> {

    /**
     * The Constructor.
     */
    protected final Supplier<VALUE> constructor;

    /**
     * Instantiates a new Entity collection updater.
     *
     * @param setter       the target setter
     * @param getter       the target getter
     * @param dynamic      if the update should be dynamic processed and nulls will be ignored
     * @param sourceGetter the source getter
     * @param constructor  the entity constructor
     */
    public EntityCollectionUpdater(final BiConsumer<TARGET, COLLECTION> setter,
                                   final Function<TARGET, COLLECTION> getter,
                                   final boolean dynamic,
                                   final Function<SOURCE, COLLECTION> sourceGetter,
                                   final Supplier<VALUE> constructor) {
        super(setter, getter, dynamic, sourceGetter);
        this.constructor = constructor;
    }

    /**
     * Convenient static method.
     * It updates the field value in the target based on the value of the field value in the source.
     *
     * @param <T>          the type parameter of the target object
     * @param <S>          the type parameter of the source object
     * @param <C>          the Collection type
     * @param <E>          the type parameter of the collection values (the entity)
     * @param <K>          the type parameter of the primary key of the entity
     * @param setter       the target setter
     * @param getter       the target getter
     * @param dynamic      if the update should be dynamic processed and nulls will be ignored
     * @param sourceGetter the source getter
     * @param constructor  the constructor for collection values
     * @param target       the target
     * @param source       the source
     * @return true if the target changed
     */
    public static <T, S, C extends Collection<E>, E extends Updatable<E> & PrimaryKey<K>, K> boolean updateEntityCollection(
            final BiConsumer<T, C> setter,
            final Function<T, C> getter,
            final boolean dynamic,
            final Function<S, C> sourceGetter,
            final Supplier<E> constructor,
            final T target,
            final S source) {
        return new EntityCollectionUpdater<>(setter, getter, dynamic, sourceGetter, constructor).update(target, source);
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
        final COLLECTION sourceValue = this.toGetter.apply(source);
        // nulls
        if (sourceValue == null) {
            if (this.dynamic || this.entityGetter.apply(target) == null) // null ignore
            {
                return false;
            } else {
                this.entitySetter.accept(target, null);
                // todo: rise warning on collection set. this can causes trouble in  Hibernate.
                System.out.println("Warning : Setting collection to null in Class " + target.getClass()
                                                                                            .getSimpleName());
                return true;
            }
        }
        final Collection<VALUE> targetValue = this.entityGetter.apply(target);
        // collection not initialized
        if (targetValue == null) {
            // todo: rise warning on collection set. this can causes trouble in  Hibernate.
            System.out.println("Warning : Found not initialized (null) collection in Class " + target.getClass()
                                                                                                     .getSimpleName());
            this.entitySetter.accept(target, sourceValue);
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

        final boolean updated = ValueUpdater.updateMap(targetValueMap, sourceValueMap, this.constructor);
        final List<VALUE> targetValueList = ValueUpdater.updateList(sourceValueList, this.constructor);

        if (!targetValueList.isEmpty() || updated) {
            targetValue.clear();
            // add the updated entities
            targetValue.addAll(targetValueMap.values());
            // add the new ones
            targetValue.addAll(targetValueList);
            // collection work
            // re set it
            this.entitySetter.accept(target, (COLLECTION) targetValue);
            return true;
        }
        return updated;
    }
}
