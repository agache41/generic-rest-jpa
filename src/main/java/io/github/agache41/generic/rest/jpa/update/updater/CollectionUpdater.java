
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

    public CollectionUpdater(BiConsumer<TARGET, Collection<VALUE>> setter, Function<TARGET, Collection<VALUE>> getter, boolean notNull, Function<SOURCE, Collection<VALUE>> sourceGetter) {
        super(setter, getter, notNull, sourceGetter);
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
        targetValue.clear();
        targetValue.addAll(sourceValue);
        // collection work
        return true;
    }
}
