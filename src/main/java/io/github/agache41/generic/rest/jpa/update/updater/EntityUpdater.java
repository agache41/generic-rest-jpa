
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

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class EntityUpdater<TARGET, SOURCE, VALUE extends Updateable<VALUE>> extends ValueUpdater<TARGET, SOURCE, VALUE> {
    protected final Supplier<VALUE> constructor;

    public EntityUpdater(final BiConsumer<TARGET, VALUE> setter,
                         final Function<TARGET, VALUE> getter,
                         final boolean notNull,
                         final Function<SOURCE, VALUE> sourceGetter,
                         final Supplier<VALUE> constructor) {
        super(setter, getter, notNull, sourceGetter);
        this.constructor = constructor;
    }

    public static <T, S, V extends Updateable<V>> boolean updateEntity(
            final BiConsumer<T, V> setter,
            final Function<T, V> getter,
            final boolean notNull,
            final Function<S, V> sourceGetter,
            final Supplier<V> constructor,
            final T target,
            final S source) {
        return new EntityUpdater<>(setter, getter, notNull, sourceGetter, constructor).update(target, source);
    }

    @Override
    public boolean update(final TARGET target,
                          final SOURCE source) {
        // the sourceValue to be updated
        final VALUE sourceValue = this.sourceGetter.apply(source);
        // nulls
        if (sourceValue == null) {
            // null ignore or both null
            if (this.notNull || this.getter.apply(target) == null) {
                return false;
            }
            this.setter.accept(target, null);
            return true;
        }
        // nulls
        final VALUE targetValue = this.getter.apply(target);
        if (targetValue == null) {
            // previous sourceValue was null, we assign the new one
            final VALUE newValue = this.constructor.get();
            final boolean updated = newValue.update(sourceValue);
            this.setter.accept(target, newValue);
            return updated;
        }
        return targetValue.update(sourceValue);
    }
}
