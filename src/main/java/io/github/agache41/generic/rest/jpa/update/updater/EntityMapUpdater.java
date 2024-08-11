
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

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The updater for map of entity types (implementing PrimaryKey and Updatable).
 * It updates the field value in the target based on the value of the field value in the source.
 *
 * @param <TO>      the type parameter
 * @param <ENTITY>  the type parameter
 * @param <TOMAP>   the type parameter
 * @param <ENMAP>   the type parameter
 * @param <TOVALUE> the type parameter
 * @param <ENVALUE> the type parameter
 * @param <KEY>     the type parameter
 */
public class EntityMapUpdater<TO, ENTITY, TOMAP extends Map<KEY, TOVALUE>, ENMAP extends Map<KEY, ENVALUE>, TOVALUE extends TransferObject<TOVALUE, ENVALUE>, ENVALUE, KEY> extends BaseUpdater<TO, ENTITY, TOMAP, ENMAP> {

    /**
     * The Constructor.
     */
    protected final Supplier<TOVALUE> toValueConstructor;

    /**
     * The Constructor.
     */
    protected final Supplier<ENVALUE> enValueConstructor;

    /**
     * Instantiates a new Entity map updater.
     *
     * @param toGeter            the to geter
     * @param toSetter           the to setter
     * @param toValueConstructor the to value constructor
     * @param dynamic            if the update should be dynamic processed and nulls will be ignored
     * @param entityGetter       the entity getter
     * @param entitySetter       the entity setter
     * @param enValueConstructor the en value constructor
     */
    public EntityMapUpdater(final Function<TO, TOMAP> toGeter,
                            final BiConsumer<TO, TOMAP> toSetter,
                            final Supplier<TOVALUE> toValueConstructor,
                            final boolean dynamic,
                            final Function<ENTITY, ENMAP> entityGetter,
                            final BiConsumer<ENTITY, ENMAP> entitySetter,
                            final Supplier<ENVALUE> enValueConstructor) {
        super(toGeter, toSetter, dynamic, entityGetter, entitySetter);
        this.toValueConstructor = toValueConstructor;
        this.enValueConstructor = enValueConstructor;
    }

    /**
     * Convenient static method.
     * It updates the field value in the target based on the value of the field value in the source.
     *
     * @param <T>                the type parameter of the target object
     * @param <E>                the type parameter of the map value (the entity)
     * @param <CTV>              the type parameter
     * @param <CEV>              the type parameter
     * @param <TV>               the type parameter
     * @param <EV>               the type parameter
     * @param <K>                the type parameter of the map key
     * @param toGetter           the to getter
     * @param toSetter           the to setter
     * @param toValueConstructor the to value constructor
     * @param dynamic            if the update should be dynamic processed and nulls will be ignored
     * @param entityGetter       the entity getter
     * @param entitySetter       the entity setter
     * @param enValueConstructor the en value constructor
     * @param target             the target
     * @param source             the source
     * @return true if the target changed
     */
    public static <T, E, CTV extends Map<K, TV>, CEV extends Map<K, EV>, TV extends TransferObject<TV, EV>, EV, K>
    boolean updateEntityMap(
            final Function<T, CTV> toGetter,
            final BiConsumer<T, CTV> toSetter,
            final Supplier<TV> toValueConstructor,
            final boolean dynamic,
            final Function<E, CEV> entityGetter,
            final BiConsumer<E, CEV> entitySetter,
            final Supplier<EV> enValueConstructor,
            final T target,
            final E source) {
        return new EntityMapUpdater<>(toGetter, toSetter, toValueConstructor, dynamic, entityGetter, entitySetter, enValueConstructor).update(target, source);
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
                          final ENTITY entity) {
        // the toMap to be updated
        final TOMAP toMap = this.toGetter.apply(transferObject);
        // nulls
        if (toMap == null) {
            if (this.dynamic || this.entityGetter.apply(entity) == null) // null ignore
            {
                return false;
            } else {
                this.entitySetter.accept(entity, null);
                // todo: rise warning on map set. this can causes trouble in  Hibernate.
                System.out.println("Warning : Setting map to null in Class " + entity.getClass()
                                                                                     .getSimpleName());
                return true;
            }
        }

        final ENMAP enMap = this.entityGetter.apply(entity);
        // map not initialized
        if (enMap == null) {
            throw new RuntimeException("Warning : Found not initialized (null) map in Class " + entity.getClass()
                                                                                                      .getSimpleName());
        }

        // empty
        if (toMap.isEmpty()) {
            if (enMap.isEmpty()) {
                return false;
            }
            enMap.clear();
            return true;
        }

        // empty
        final boolean updated = ValueUpdater.updateMap(toMap, enMap, this.enValueConstructor);
        if (updated) {
            this.entitySetter.accept(entity, enMap);
        }
        return updated;
    }
}
