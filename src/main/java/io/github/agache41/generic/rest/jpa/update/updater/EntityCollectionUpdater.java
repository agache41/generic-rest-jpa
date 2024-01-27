
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

public class EntityCollectionUpdater<TARGET, SOURCE, COLLECTION extends Collection<VALUE>, VALUE extends Updateable<VALUE> & PrimaryKey<PK>, PK> extends ValueUpdater<TARGET, SOURCE, COLLECTION> {

    protected final Supplier<VALUE> constructor;

    public EntityCollectionUpdater(final BiConsumer<TARGET, COLLECTION> setter,
                                   final Function<TARGET, COLLECTION> getter,
                                   final boolean notNull,
                                   final Function<SOURCE, COLLECTION> sourceGetter,
                                   final Supplier<VALUE> constructor) {
        super(setter, getter, notNull, sourceGetter);
        this.constructor = constructor;
    }

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
        // nulls

        // empty
        final Collection<VALUE> targetValue = this.getter.apply(target);
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
        final Map<PK, VALUE> sourceValueMap = new HashMap<>();
        final List<VALUE> sourceValueList = new ArrayList<>(sourceValue.size());
        for (final VALUE val : sourceValue) {
            final PK PK = val.getId();
            if (PK == null) {
                sourceValueList.add(val);
            } else {
                sourceValueMap.put(PK, val);
            }
        }

        final Map<PK, VALUE> targetValueMap = new HashMap<>();
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
