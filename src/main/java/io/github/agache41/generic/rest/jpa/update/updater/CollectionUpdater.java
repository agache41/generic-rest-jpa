
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

public class CollectionUpdater<TARGET, SOURCE, VALUE> extends ValueUpdater<TARGET, SOURCE, Collection<VALUE>> {

    public CollectionUpdater(final BiConsumer<TARGET, Collection<VALUE>> setter,
                             final Function<TARGET, Collection<VALUE>> getter,
                             final boolean notNull,
                             final Function<SOURCE, Collection<VALUE>> sourceGetter) {
        super(setter, getter, notNull, sourceGetter);
    }

    public static <T, S, V> boolean updateCollection(
            final BiConsumer<T, Collection<V>> setter,
            final Function<T, Collection<V>> getter,
            final boolean notNull,
            final Function<S, Collection<V>> sourceGetter,
            final T target,
            final S source) {
        return new CollectionUpdater<>(setter, getter, notNull, sourceGetter).update(target, source);
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
        targetValue.clear();
        targetValue.addAll(sourceValue);
        // collection work
        return true;
    }
}
