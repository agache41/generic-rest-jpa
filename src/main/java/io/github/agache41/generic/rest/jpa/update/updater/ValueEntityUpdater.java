
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

public class ValueEntityUpdater<TARGET, SOURCE, VALUE extends Updateable<VALUE>> extends ValueUpdater<TARGET, SOURCE, VALUE> {
    protected final Supplier<VALUE> constructor;

    public ValueEntityUpdater(BiConsumer<TARGET, VALUE> setter, Function<TARGET, VALUE> getter, boolean notNull, Function<SOURCE, VALUE> sourceGetter, Supplier<VALUE> constructor) {
        super(setter, getter, notNull, sourceGetter);
        this.constructor = constructor;
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
        VALUE targetValue = getter.apply(target);
        if (targetValue == null) {
            // previous sourceValue was null, we assign the new one
            VALUE newValue = constructor.get();
            boolean updated = newValue.update(sourceValue);
            setter.accept(target, newValue);
            return updated;
        }
        return targetValue.update(sourceValue);
    }
}
