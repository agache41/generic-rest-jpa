
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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * The updater for simple value types (String, Integer).
 * It updates the field value in the target based on the value of the field value in the source
 *
 * @param <TARGET> the type parameter of the target object
 * @param <SOURCE> the type parameter of the source object
 * @param <VALUE>  the type parameter of the updating value
 */
public class ValueUpdater<TARGET, SOURCE, VALUE> implements Updater<TARGET, SOURCE> {

    /**
     * The Setter.
     */
    protected final BiConsumer<TARGET, VALUE> setter;
    /**
     * The Getter.
     */
    protected final Function<TARGET, VALUE> getter;
    /**
     * If the update should be dynamic processed and nulls will be ignored.
     */
    protected final boolean dynamic;
    /**
     * The Source getter.
     */
    protected final Function<SOURCE, VALUE> sourceGetter;


    /**
     * Instantiates a new Value updater.
     *
     * @param setter       the target setter
     * @param getter       the target getter
     * @param dynamic      if the update should be dynamic processed and nulls will be ignored
     * @param sourceGetter the source getter
     */
    public ValueUpdater(final BiConsumer<TARGET, VALUE> setter,
                        final Function<TARGET, VALUE> getter,
                        final boolean dynamic,
                        final Function<SOURCE, VALUE> sourceGetter) {
        this.setter = setter;
        this.getter = getter;
        this.dynamic = dynamic;
        this.sourceGetter = sourceGetter;
    }

    /**
     * Convenient static method.
     * It updates the field value in the target based on the value of the field value in the source.
     *
     * @param <T>          the type parameter of the target object
     * @param <S>          the type parameter of the source object
     * @param <V>          the type parameter of the value
     * @param setter       the target setter
     * @param getter       the target getter
     * @param notNull      if the update should be dynamic processed and nulls will be ignored
     * @param sourceGetter the source getter
     * @param target       the target
     * @param source       the source
     * @return true if the target changed
     */
    public static <T, S, V> boolean updateValue(
            final BiConsumer<T, V> setter,
            final Function<T, V> getter,
            final boolean notNull,
            final Function<S, V> sourceGetter,
            final T target,
            final S source) {
        return new ValueUpdater<>(setter, getter, notNull, sourceGetter).update(target, source);
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
        final VALUE sourceValue = this.sourceGetter.apply(source);
        // nulls
        if (sourceValue == null) {
            // null ignore or both null
            if (this.dynamic || this.getter.apply(target) == null) {
                return false;
            }
            this.setter.accept(target, null);
            return true;
        }
        // nulls

        if (!Objects.equals(this.getter.apply(target), sourceValue)) {
            // equals check
            this.setter.accept(target, sourceValue);
            return true;
        } // otherwise no update
        return false;
    }
}
