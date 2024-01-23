
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

package io.github.agache41.generic.rest.jpa.deprecated;

import io.github.agache41.generic.rest.jpa.dataAccess.PrimaryKey;
import io.github.agache41.generic.rest.jpa.update.Updateable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Deprecated
public interface UpdaterTemp<TARGET, SOURCE> {

    default <VALUE> boolean update(
            BiConsumer<TARGET, VALUE> setter,
            Function<TARGET, VALUE> getter,
            boolean notNull,
            Function<SOURCE, VALUE> sourceGetter,
            TARGET target,
            SOURCE source) {

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


    default <VALUE extends Updateable<VALUE>> boolean updateEntity(
            BiConsumer<TARGET, VALUE> setter,
            Function<TARGET, VALUE> getter,
            boolean notNull,
            Function<SOURCE, VALUE> sourceGetter,
            TARGET target,
            SOURCE source,
            BooleanBiConsumerTemp<VALUE, VALUE> updateMethod,
            Function<VALUE, VALUE> createMethod) {

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
        VALUE targetValue = getter.apply(target);
        if (targetValue == null) {
            // previous sourceValue was null, we assign the new one
            setter.accept(target, createMethod.apply(sourceValue));
            return true;
        }
        return targetValue.update(sourceValue);
    }

    default <KEY, VALUE> boolean updateMap(
            Function<TARGET, Map<KEY, VALUE>> getter,
            BiConsumer<TARGET, Map<KEY, VALUE>> setter,
            boolean notNull,
            Function<SOURCE, Map<KEY, VALUE>> sourceGetter,
            TARGET target,
            SOURCE source) {

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


    default <KEY, VALUE extends Updateable<VALUE>> boolean updateEntityMap(
            Function<TARGET, Map<KEY, VALUE>> getter,
            BiConsumer<TARGET, Map<KEY, VALUE>> setter,
            boolean notNull,
            Function<SOURCE, Map<KEY, VALUE>> sourceGetter,
            TARGET target,
            SOURCE source,
            BooleanBiConsumerTemp<VALUE, VALUE> updateMethod,
            Function<VALUE, VALUE> createMethod) {

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
        return updateMap(targetValue, sourceValue, createMethod);
    }

    default <KEY, VALUE extends Updateable<VALUE>> boolean updateMap(
            Map<KEY, VALUE> targetValue,
            Map<KEY, VALUE> sourceValue,
            Function<VALUE, VALUE> createMethod) {


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
            valueKeys.stream()
                     .forEach(key -> targetValue.put(key, createMethod.apply(sourceValue.get(key))));
            updated = true;
        }
        return updated;
    }


    default <VALUE> boolean updateCollection(Function<TARGET, Collection<VALUE>> getter,
                                             BiConsumer<TARGET, Collection<VALUE>> setter,
                                             boolean notNull,
                                             Function<SOURCE, Collection<VALUE>> sourceGetter,
                                             TARGET target,
                                             SOURCE source) {
        // the sourceValue to be updated
        Collection<VALUE> sourceValue = sourceGetter.apply(source);
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
        Collection<VALUE> targetValue = getter.apply(target);
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


    default <VALUE extends Updateable<VALUE> & PrimaryKey<PK>, PK> boolean updateEntityCollection(Function<TARGET, Collection<VALUE>> getter,
                                                                                                  BiConsumer<TARGET, Collection<VALUE>> setter,
                                                                                                  boolean notNull,
                                                                                                  Function<SOURCE, Collection<VALUE>> sourceGetter,
                                                                                                  TARGET target,
                                                                                                  SOURCE source,
                                                                                                  BooleanBiConsumerTemp<VALUE, VALUE> updateMethod,
                                                                                                  Function<VALUE, VALUE> createMethod) {
        // the sourceValue to be updated
        Collection<VALUE> sourceValue = sourceGetter.apply(source);
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
        Collection<VALUE> targetValue = getter.apply(target);
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
        Map<PK, VALUE> sourceValueMap = new HashMap<>();
        List<VALUE> sourceValueList = new ArrayList<>(sourceValue.size());
        for (VALUE val : sourceValue) {
            PK pk = val.getId();
            if (pk == null)
                sourceValueList.add(val);
            else
                sourceValueMap.put(pk, val);
        }

        Map<PK, VALUE> targetValueMap = new HashMap<>();
        for (VALUE val : targetValue) {
            PK pk2 = val.getId();
            if (pk2 != null)
                targetValueMap.put(pk2, val);
        }

        updateMap(targetValueMap, sourceValueMap, createMethod);

        targetValue.clear();
        // add the updated entities
        targetValue.addAll(targetValueMap.values());
        // add the new ones
        targetValue.addAll(sourceValueList);
        // collection work

        return true;
    }
}