
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

import io.github.agache41.generic.rest.jpa.update.Updateable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ValueUpdater<TARGET, SOURCE, VALUE> implements Updater<TARGET, SOURCE> {

    protected final BiConsumer<TARGET, VALUE> setter;
    protected final Function<TARGET, VALUE> getter;
    protected final boolean notNull;
    protected final Function<SOURCE, VALUE> sourceGetter;

    public ValueUpdater(BiConsumer<TARGET, VALUE> setter, Function<TARGET, VALUE> getter, boolean notNull, Function<SOURCE, VALUE> sourceGetter) {
        this.setter = setter;
        this.getter = getter;
        this.notNull = notNull;
        this.sourceGetter = sourceGetter;
    }

    @Override
    public boolean update(TARGET target, SOURCE source) {
        // the sourceValue to be updated
        VALUE sourceValue = sourceGetter.apply(source);
        // nulls
        if (sourceValue == null) {
            // null ignore or both null
            if (notNull || getter.apply(target) == null)
                return false;
            setter.accept(target, null);
            return true;
        }
        // nulls

        if (!Objects.equals(getter.apply(target), sourceValue)) {
            // equals check
            setter.accept(target, sourceValue);
            return true;
        } // otherwise no update
        return false;
    }

    protected <KEY, VALUE extends Updateable<VALUE>> boolean updateMap(
            Map<KEY, VALUE> targetValue,
            Map<KEY, VALUE> sourceValue,
            Supplier<VALUE> constructor) {


        Set<KEY> targetKeys = targetValue.keySet();
        // make a copy to not change the input
        Set<KEY> valueKeys = sourceValue.keySet()
                                        .stream()
                                        .collect(Collectors.toSet());
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
                                   VALUE newValue = constructor.get();
                                   boolean upd = newValue.update(sourceValue.get(key));
                                   targetValue.put(key, newValue);
                                   return upd;
                               })
                               .reduce(updated, (u, n) -> u || n);
        }
        return updated;
    }
}
