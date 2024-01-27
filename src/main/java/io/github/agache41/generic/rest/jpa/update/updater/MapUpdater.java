
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

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MapUpdater<TARGET, SOURCE, VALUE, KEY> extends ValueUpdater<TARGET, SOURCE, Map<KEY, VALUE>> {


    public MapUpdater(final BiConsumer<TARGET, Map<KEY, VALUE>> setter,
                      final Function<TARGET, Map<KEY, VALUE>> getter,
                      final boolean notNull,
                      final Function<SOURCE, Map<KEY, VALUE>> sourceGetter) {
        super(setter, getter, notNull, sourceGetter);
    }

    
    public static <T, S, V, K> boolean updateMap(
            final BiConsumer<T, Map<K, V>> setter,
            final Function<T, Map<K, V>> getter,
            final boolean notNull,
            final Function<S, Map<K, V>> sourceGetter,
            final T target,
            final S source) {
        return new MapUpdater<>(setter, getter, notNull, sourceGetter).update(target, source);
    }

    @Override
    public boolean update(final TARGET target,
                          final SOURCE source) {
        // the sourceValue to be updated
        final Map<KEY, VALUE> sourceValue = this.sourceGetter.apply(source);
        // nulls
        if (sourceValue == null) {
            if (this.notNull || this.getter.apply(target) == null) {
                return false;
            } else {
                this.setter.accept(target, null);
                return true;
            }
        }
        // nulls

        // empty
        final Map<KEY, VALUE> targetValue = this.getter.apply(target);
        if (sourceValue.isEmpty()) {
            if (targetValue.isEmpty()) {
                return false;
            }
            targetValue.clear();
            return true;
        }
        // empty

        // map work
        final Set<KEY> targetKeys = targetValue.keySet();
        // make a copy to not change the input
        final Set<KEY> valueKeys = sourceValue.keySet()
                                              .stream()
                                              .collect(Collectors.toSet());
        //remove all that are now longer available
        boolean updated = targetKeys.retainAll(valueKeys);
        //update all that remained in the intersection
        updated |= targetKeys.stream() // process only the ones that are not equal
                             .filter(key -> !Objects.equals(targetValue.get(key), sourceValue.get(key)))
                             // take those from the input in the old map
                             .map(key -> targetValue.put(key, sourceValue.get(key)))
                             .count() > 0;
        //remove those that are updated
        valueKeys.removeAll(targetKeys);
        //insert all new (that remained)
        if (!valueKeys.isEmpty()) {
            valueKeys.stream()
                     .forEach(key -> targetValue.put(key, sourceValue.get(key)));
            updated = true;
        }
        return updated;
    }
}
