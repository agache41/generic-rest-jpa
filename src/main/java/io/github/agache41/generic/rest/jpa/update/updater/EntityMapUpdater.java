
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

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The updater for map of entity types (implementing PrimaryKey and Updatable).
 * It updates the field value in the target based on the value of the field value in the source.
 *
 * @param <TARGET> the type parameter
 * @param <SOURCE> the type parameter
 * @param <MAP>    the type parameter
 * @param <VALUE>  the type parameter
 * @param <KEY>    the type parameter
 */
public class EntityMapUpdater<TARGET, SOURCE, MAP extends Map<KEY, VALUE>, VALUE extends Updatable<VALUE>, KEY> extends ValueUpdater<TARGET, SOURCE, MAP> {

    /**
     * The Constructor.
     */
    protected final Supplier<VALUE> constructor;

    /**
     * Instantiates a new Entity map updater.
     *
     * @param setter       the target setter
     * @param getter       the target getter
     * @param notNull      if entity map can be null
     * @param sourceGetter the source getter
     * @param constructor  the entity constructor
     */
    public EntityMapUpdater(final BiConsumer<TARGET, MAP> setter,
                            final Function<TARGET, MAP> getter,
                            final boolean notNull,
                            final Function<SOURCE, MAP> sourceGetter,
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
     * @param <C>          the type parameter
     * @param <E>          the type parameter of the map value (the entity)
     * @param <K>          the type parameter of the map key
     * @param setter       the target setter
     * @param getter       the target getter
     * @param notNull      if values can be not null
     * @param sourceGetter the source getter
     * @param constructor  the constructor for the map values
     * @param target       the target
     * @param source       the source
     * @return true if the target changed
     */
    public static <T, S, C extends Map<K, E>, E extends Updatable<E> & PrimaryKey<K>, K> boolean updateEntityMap(
            final BiConsumer<T, C> setter,
            final Function<T, C> getter,
            final boolean notNull,
            final Function<S, C> sourceGetter,
            final Supplier<E> constructor,
            final T target,
            final S source) {
        return new EntityMapUpdater<>(setter, getter, notNull, sourceGetter, constructor).update(target, source);
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
        final MAP sourceValue = this.sourceGetter.apply(source);
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

        final Map<KEY, VALUE> targetValue = this.getter.apply(target);
        // map not initialized
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
        final boolean updated = ValueUpdater.updateMap(targetValue, sourceValue, this.constructor);
        this.setter.accept(target, (MAP) targetValue);
        return updated;
    }
}
