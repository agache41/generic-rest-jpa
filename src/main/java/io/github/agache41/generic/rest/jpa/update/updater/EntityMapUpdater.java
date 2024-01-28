
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

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class EntityMapUpdater<TARGET, SOURCE, MAP extends Map<KEY, VALUE>, VALUE extends Updateable<VALUE>, KEY> extends ValueUpdater<TARGET, SOURCE, MAP> {

    protected final Supplier<VALUE> constructor;

    public EntityMapUpdater(final BiConsumer<TARGET, MAP> setter,
                            final Function<TARGET, MAP> getter,
                            final boolean notNull,
                            final Function<SOURCE, MAP> sourceGetter,
                            final Supplier<VALUE> constructor) {
        super(setter, getter, notNull, sourceGetter);
        this.constructor = constructor;
    }

    public static <T, S, C extends Map<K, E>, E extends Updateable<E> & PrimaryKey<K>, K> boolean updateEntityMap(
            final BiConsumer<T, C> setter,
            final Function<T, C> getter,
            final boolean notNull,
            final Function<S, C> sourceGetter,
            final Supplier<E> constructor,
            final T target,
            final S source) {
        return new EntityMapUpdater<>(setter, getter, notNull, sourceGetter, constructor).update(target, source);
    }

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
        return ValueUpdater.updateMap(targetValue, sourceValue, this.constructor);
    }
}
