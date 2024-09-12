
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

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * The updater for simple value types (String, Integer).
 * It updates the field value in the entity based on the value of the field value in the transfer object
 *
 * @param <TO>     the type parameter of the transfer object
 * @param <ENTITY> the type parameter of the entity
 * @param <VALUE>  the type parameter of the updating value
 */
public class ValueUpdater<TO, ENTITY, VALUE> extends BaseUpdater<TO, ENTITY, VALUE, VALUE> {

    /**
     * Instantiates a new Value updater.
     *
     * @param toGetter     the transfer object getter
     * @param toSetter     the transfer object setter
     * @param dynamic      if the update should be dynamic processed and nulls will be ignored
     * @param entityGetter the entity getter
     * @param entitySetter the entity setter
     */
    public ValueUpdater(final Function<TO, VALUE> toGetter,
                        final BiConsumer<TO, VALUE> toSetter,
                        final boolean dynamic,
                        final Function<ENTITY, VALUE> entityGetter,
                        final BiConsumer<ENTITY, VALUE> entitySetter

    ) {
        super(toGetter, toSetter, dynamic, entityGetter, entitySetter);
    }

    /**
     * Convenient static method.
     * It updates the field value in the entity based on the value of the field value in the transfer object.
     *
     * @param <T>            the type parameter of the transfer object
     * @param <E>            the type parameter of the entity object
     * @param <V>            the type parameter of the value
     * @param toGetter       the entity getter
     * @param toSetter       the transfer object setter
     * @param dynamic        if the update should be dynamic processed and nulls will be ignored
     * @param entityGetter   the entity getter
     * @param entitySetter   the entity setter
     * @param transferObject the transfer object
     * @param entity         the entity
     * @param context        the context
     * @return true if the target changed
     */
    public static <T, E, V> boolean updateValue(final Function<T, V> toGetter,
                                                final BiConsumer<T, V> toSetter,
                                                final boolean dynamic,
                                                final Function<E, V> entityGetter,
                                                final BiConsumer<E, V> entitySetter,
                                                final T transferObject,
                                                final E entity,
                                                final Object context) {
        return new ValueUpdater<>(toGetter, toSetter, dynamic, entityGetter, entitySetter).update(transferObject, entity, context);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean update(final TO transferObject,
                          final ENTITY entity,
                          final Object context) {
        // the toValue to be updated
        final VALUE toValue = this.toGetter.apply(transferObject);
        // nulls
        if (toValue == null) {
            // null ignore or both null
            if (this.dynamic || this.entityGetter.apply(entity) == null) {
                return false;
            }
            this.entitySetter.accept(entity, null);
            return true;
        }
        // nulls

        if (!Objects.equals(toValue, this.entityGetter.apply(entity))) {
            // equals check
            this.entitySetter.accept(entity, toValue);
            return true;
        } // otherwise no update
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(final TO transferObject,
                       final ENTITY entity,
                       final Object context) {
        this.toSetter.accept(transferObject, this.entityGetter.apply(entity));
    }
}
