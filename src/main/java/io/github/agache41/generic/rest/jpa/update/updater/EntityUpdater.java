
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

import io.github.agache41.generic.rest.jpa.update.Updatable;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The updater for entities types (implementing PrimaryKey and Updatable).
 * It updates the field value in the target based on the value of the field value in the source
 *
 * @param <TARGET> the type parameter of the target object
 * @param <SOURCE> the type parameter of the source object
 * @param <VALUE>  the type parameter of the updating value
 */
public class EntityUpdater<TARGET, SOURCE, VALUE extends Updatable<VALUE>> extends ValueUpdater<TARGET, SOURCE, VALUE> {
    /**
     * The Constructor.
     */
    protected final Supplier<VALUE> constructor;

    /**
     * Instantiates a new Entity updater.
     *
     * @param setter       the target setter
     * @param getter       the target getter
     * @param dynamic      if the update should be dynamic processed and nulls will be ignored
     * @param sourceGetter the source getter
     * @param constructor  the entity constructor
     */
    public EntityUpdater(final BiConsumer<TARGET, VALUE> setter,
                         final Function<TARGET, VALUE> getter,
                         final boolean dynamic,
                         final Function<SOURCE, VALUE> sourceGetter,
                         final Supplier<VALUE> constructor) {
        super(setter, getter, dynamic, sourceGetter);
        this.constructor = constructor;
    }

    /**
     * Convenient static method.
     * It updates the field value in the target based on the value of the field value in the source.
     *
     * @param <T>          the type parameter of the target object
     * @param <S>          the type parameter of the source object
     * @param <V>          the type parameter of the entity
     * @param setter       the target setter
     * @param getter       the target getter
     * @param dynamic      if the update should be dynamic processed and nulls will be ignored
     * @param sourceGetter the source getter
     * @param constructor  the Entity constructor
     * @param target       the target
     * @param source       the source
     * @return true if the target changed
     */
    public static <T, S, V extends Updatable<V>> boolean updateEntity(
            final BiConsumer<T, V> setter,
            final Function<T, V> getter,
            final boolean dynamic,
            final Function<S, V> sourceGetter,
            final Supplier<V> constructor,
            final T target,
            final S source) {
        return new EntityUpdater<>(setter, getter, dynamic, sourceGetter, constructor).update(target, source);
    }

    /**
     * The method updates the field in target based on the field the source
     *
     * @param target the target
     * @param source the source
     * @return true if the target changed
     */
    @Override
    public boolean update(final TARGET target,
                          final SOURCE source) {
        // the sourceValue to be updated
        final VALUE sourceValue = this.sourceGetter.apply(source);
        // nulls
        if (sourceValue == null) {
            // null ignore or both null
            if (this.dynamic || this.getter.apply(target) == null) {
                return false;
            }
            this.setter.accept(target, null);
            return true;
        }
        final VALUE targetValue = this.getter.apply(target);
        // target not initialized
        if (targetValue == null) {
            // previous sourceValue was null, we assign the new one
            final VALUE newValue = this.constructor.get();
            final boolean updated = newValue.update(sourceValue);
            this.setter.accept(target, newValue);
            return updated;
        }
        final boolean updated = targetValue.update(sourceValue);
        if (updated) {
            this.setter.accept(target, targetValue);
        }
        return updated;
    }
}
