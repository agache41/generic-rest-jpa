
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


    public MapUpdater(BiConsumer<TARGET, Map<KEY, VALUE>> setter, Function<TARGET, Map<KEY, VALUE>> getter, boolean notNull, Function<SOURCE, Map<KEY, VALUE>> sourceGetter) {
        super(setter, getter, notNull, sourceGetter);
    }

    @Override
    public boolean update(TARGET target, SOURCE source) {
        // the sourceValue to be updated
        Map<KEY, VALUE> sourceValue = sourceGetter.apply(source);
        // nulls
        if (sourceValue == null) {
            if (notNull || getter.apply(target) == null) // null ignore
                return false;
            else {
                setter.accept(target, null);
                return true;
            }
        }
        // nulls

        // empty
        Map<KEY, VALUE> targetValue = getter.apply(target);
        if (sourceValue.isEmpty()) {
            if (targetValue.isEmpty()) {
                return false;
            }
            targetValue.clear();
            return true;
        }
        // empty

        // map work
        Set<KEY> targetKeys = targetValue.keySet();
        // make a copy to not change the input
        Set<KEY> valueKeys = sourceValue.keySet()
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
