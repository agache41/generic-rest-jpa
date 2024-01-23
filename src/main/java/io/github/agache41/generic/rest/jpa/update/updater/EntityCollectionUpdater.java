
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

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class EntityCollectionUpdater<TARGET, SOURCE, COLLECTION extends Collection<VALUE>, VALUE extends UpdateableAndPrimaryKey<VALUE, PK>, PK> extends ValueUpdater<TARGET, SOURCE, COLLECTION> {

    protected final Supplier<VALUE> constructor;

    public EntityCollectionUpdater(BiConsumer<TARGET, COLLECTION> setter, Function<TARGET, COLLECTION> getter, boolean notNull, Function<SOURCE, COLLECTION> sourceGetter, Supplier<VALUE> constructor) {
        super(setter, getter, notNull, sourceGetter);
        this.constructor = constructor;
    }

    @Override
    public boolean update(TARGET target, SOURCE source) {
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
            PK PK = val.getId();
            if (PK == null)
                sourceValueList.add(val);
            else
                sourceValueMap.put(PK, val);
        }

        Map<PK, VALUE> targetValueMap = new HashMap<>();
        for (VALUE val : targetValue) {
            PK PK = val.getId();
            if (PK != null)
                targetValueMap.put(PK, val);
        }

        updateMap(targetValueMap, sourceValueMap, this.constructor);

        targetValue.clear();
        // add the updated entities
        targetValue.addAll(targetValueMap.values());
        // add the new ones
        targetValue.addAll(sourceValueList);
        // collection work
        return true;
    }
}
