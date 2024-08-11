
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
 * The updater for transfer object types (implementing Updatable).
 * It updates the field value in the entity based on the value of the field value in the transfer object
 *
 * @param <TO>     the type parameter of the transfer object
 * @param <ENTITY> the type parameter of the entity object
 * @param <VALUE>  the type parameter of the updating value
 */
public class EntityUpdater<TO, ENTITY, VALUE extends Updatable<VALUE>> extends ValueUpdater<TO, ENTITY, VALUE> {
    /**
     * The Constructor.
     */
    protected final Supplier<VALUE> constructor;

    /**
     * Instantiates a new Entity updater.
     *
     * @param toGetter     the source entityGetter
     * @param toSetter     the to setter
     * @param dynamic      if the update should be dynamic processed and nulls will be ignored
     * @param entityGetter the target entityGetter
     * @param entitySetter the target entitySetter
     * @param constructor  the value constructor
     */
    public EntityUpdater(final Function<TO, VALUE> toGetter,
                         final BiConsumer<TO, VALUE> toSetter,
                         final boolean dynamic,
                         final Function<ENTITY, VALUE> entityGetter,
                         final BiConsumer<ENTITY, VALUE> entitySetter,
                         final Supplier<VALUE> constructor) {
        super(toGetter, toSetter, dynamic, entityGetter, entitySetter);
        this.constructor = constructor;
    }

    /**
     * Convenient static method.
     * It updates the field value in the transferObject based on the value of the field value in the entity.
     *
     * @param <T>            the type parameter of the transferObject object
     * @param <E>            the type parameter of the entity object
     * @param <V>            the type parameter of the entity
     * @param toGetter       the transferObject toGetter
     * @param toSetter       the transferObject toSetter
     * @param dynamic        if the update should be dynamic processed and nulls will be ignored
     * @param entityGetter   the entity toGetter
     * @param entitySetter   the entity setter
     * @param constructor    the Entity constructor
     * @param transferObject the transferObject
     * @param entity         the entity
     * @return true if the transferObject changed
     */
    public static <T, E, V extends Updatable<V>> boolean updateEntity(final Function<T, V> toGetter,
                                                                      final BiConsumer<T, V> toSetter,
                                                                      final boolean dynamic,
                                                                      final Function<E, V> entityGetter,
                                                                      final BiConsumer<E, V> entitySetter,
                                                                      final Supplier<V> constructor,
                                                                      final T transferObject,
                                                                      final E entity) {
        return new EntityUpdater<>(toGetter, toSetter, dynamic, entityGetter, entitySetter, constructor).update(transferObject, entity);
    }

    /**
     * The method updates the field in target based on the field the source
     *
     * @param entity         the target
     * @param transferObject the source
     * @return true if the target changed
     */
    @Override
    public boolean update(final TO transferObject,
                          final ENTITY entity
    ) {
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
        final VALUE entityValue = this.entityGetter.apply(entity);
        // target not initialized
        if (entityValue == null) {
            // previous toValue was null, we assign the new one
            final VALUE newValue = this.constructor.get();
            newValue.update(toValue);
            this.entitySetter.accept(entity, newValue);
            return true;
        }
        final boolean updated = entityValue.update(toValue);
        if (updated) {
            this.entitySetter.accept(entity, entityValue);
        }
        return updated;
    }
}
