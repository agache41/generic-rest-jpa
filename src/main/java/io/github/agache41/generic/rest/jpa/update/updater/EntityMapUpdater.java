
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
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class EntityMapUpdater<TARGET, SOURCE, MAP extends Map<KEY, VALUE>, VALUE extends Updateable<VALUE>, KEY> extends ValueUpdater<TARGET, SOURCE, MAP> {

    protected final Supplier<VALUE> constructor;

    public EntityMapUpdater(BiConsumer<TARGET, MAP> setter, Function<TARGET, MAP> getter, boolean notNull, Function<SOURCE, MAP> sourceGetter, Supplier<VALUE> constructor) {
        super(setter, getter, notNull, sourceGetter);
        this.constructor = constructor;
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
        return updateMap(targetValue, sourceValue, this.constructor);
    }
}
