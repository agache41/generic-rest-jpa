
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

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * The updater for collection of simple types (String, Integer).
 * It updates the field value in the target based on the value of the field value in the source.
 *
 * @param <TARGET> the type parameter
 * @param <SOURCE> the type parameter
 * @param <VALUE>  the type parameter
 */
public class CollectionUpdater<TARGET, SOURCE, VALUE> extends ValueUpdater<TARGET, SOURCE, Collection<VALUE>> {

    /**
     * Instantiates a new Collection updater.
     *
     * @param setter       the target setter
     * @param getter       the target getter
     * @param notNull      if values is not null
     * @param sourceGetter the source getter
     */
    public CollectionUpdater(final BiConsumer<TARGET, Collection<VALUE>> setter,
                             final Function<TARGET, Collection<VALUE>> getter,
                             final boolean notNull,
                             final Function<SOURCE, Collection<VALUE>> sourceGetter) {
        super(setter, getter, notNull, sourceGetter);
    }

    /**
     * Convenient static method.
     * It updates the field value in the target based on the value of the field value in the source.
     *
     * @param <T>          the type parameter of the target object
     * @param <S>          the type parameter of the source object
     * @param <V>          the type parameter of the collection values
     * @param setter       the target setter
     * @param getter       the target getter
     * @param notNull      if values is not null
     * @param sourceGetter the source getter
     * @param target       the target
     * @param source       the source
     * @return true if the target changed
     */
    public static <T, S, V> boolean updateCollection(
            final BiConsumer<T, Collection<V>> setter,
            final Function<T, Collection<V>> getter,
            final boolean notNull,
            final Function<S, Collection<V>> sourceGetter,
            final T target,
            final S source) {
        return new CollectionUpdater<>(setter, getter, notNull, sourceGetter).update(target, source);
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
        final Collection<VALUE> sourceValue = this.sourceGetter.apply(source);
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
        targetValue.clear();
        targetValue.addAll(sourceValue);
        // collection work
        return true;
    }
}
