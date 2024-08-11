
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

import io.github.agache41.generic.rest.jpa.update.TransferObject;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The updater for transfer object types (implementing Updatable).
 * It updates the field value in the entity based on the value of the field value in the transfer object
 *
 * @param <TO>      the type parameter of the transfer object
 * @param <ENTITY>  the type parameter of the entity object
 * @param <TOVALUE> the type parameter of the updating value
 * @param <ENVALUE> the type parameter
 */
public class EntityUpdater<TO, ENTITY, TOVALUE extends TransferObject<TOVALUE, ENVALUE>, ENVALUE> extends BaseUpdater<TO, ENTITY, TOVALUE, ENVALUE> {
    /**
     * The Constructor.
     */
    protected final Supplier<TOVALUE> toValueConstructor;

    /**
     * The Constructor.
     */
    protected final Supplier<ENVALUE> enValueConstructor;

    /**
     * Instantiates a new Entity updater.
     *
     * @param toGetter           the source entityGetter
     * @param toSetter           the to setter
     * @param toValueConstructor the value constructor
     * @param dynamic            if the update should be dynamic processed and nulls will be ignored
     * @param entityGetter       the target entityGetter
     * @param entitySetter       the target entitySetter
     * @param enValueConstructor the en value constructor
     */
    public EntityUpdater(final Function<TO, TOVALUE> toGetter,
                         final BiConsumer<TO, TOVALUE> toSetter,
                         final Supplier<TOVALUE> toValueConstructor,
                         final boolean dynamic,
                         final Function<ENTITY, ENVALUE> entityGetter,
                         final BiConsumer<ENTITY, ENVALUE> entitySetter,
                         final Supplier<ENVALUE> enValueConstructor
    ) {
        super(toGetter, toSetter, dynamic, entityGetter, entitySetter);
        this.toValueConstructor = toValueConstructor;
        this.enValueConstructor = enValueConstructor;
    }

    /**
     * Convenient static method.
     * It updates the field value in the transferObject based on the value of the field value in the entity.
     *
     * @param <T>                the type parameter of the transferObject object
     * @param <E>                the type parameter of the entity object
     * @param <TV>               the type parameter of the field in transfer object
     * @param <EV>               the type parameter of the field in entity
     * @param toGetter           the transferObject toGetter
     * @param toSetter           the transferObject toSetter
     * @param toValueConstructor the to value constructor
     * @param dynamic            if the update should be dynamic processed and nulls will be ignored
     * @param entityGetter       the entity toGetter
     * @param entitySetter       the entity setter
     * @param enValueConstructor the en value constructor
     * @param transferObject     the transferObject
     * @param entity             the entity
     * @return true if the transferObject changed
     */
    public static <T, E, TV extends TransferObject<TV, EV>, EV> boolean updateEntity(final Function<T, TV> toGetter,
                                                                                     final BiConsumer<T, TV> toSetter,
                                                                                     final Supplier<TV> toValueConstructor,
                                                                                     final boolean dynamic,
                                                                                     final Function<E, EV> entityGetter,
                                                                                     final BiConsumer<E, EV> entitySetter,
                                                                                     final Supplier<EV> enValueConstructor,
                                                                                     final T transferObject,
                                                                                     final E entity) {
        return new EntityUpdater<>(toGetter, toSetter, toValueConstructor, dynamic, entityGetter, entitySetter, enValueConstructor).update(transferObject, entity);
    }

    /**
     * The method updates the fields in entity based on the fields from to
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
        final TOVALUE toValue = this.toGetter.apply(transferObject);
        // nulls
        if (toValue == null) {
            // null ignore or both null
            if (this.dynamic || this.entityGetter.apply(entity) == null) {
                return false;
            }
            this.entitySetter.accept(entity, null);
            return true;
        }
        final ENVALUE entityValue = this.entityGetter.apply(entity);
        // target not initialized
        if (entityValue == null) {
            // previous toValue was null, we assign the new one
            final ENVALUE newValue = this.enValueConstructor.get();
            final boolean updated = toValue.update(newValue);
            this.entitySetter.accept(entity, newValue);
            return updated;
        }
        final boolean updated = toValue.update(entityValue);
        if (updated) {
            this.entitySetter.accept(entity, entityValue);
        }
        return updated;
    }
}
