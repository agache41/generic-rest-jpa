
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
import io.github.agache41.generic.rest.jpa.update.TransferObject;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The updater for collection of entity types (implementing PrimaryKey and Updatable).
 * It updates the field value in the target based on the value of the field value in the source.
 *
 * @param <TO>           the type parameter
 * @param <ENTITY>       the type parameter
 * @param <TOCOLLECTION> the type parameter
 * @param <ENCOLLECTION> the type parameter
 * @param <TOVALUE>      the type parameter
 * @param <ENVALUE>      the type parameter
 * @param <PK>           the type parameter
 */
public class EntityCollectionUpdater<TO, ENTITY, TOCOLLECTION extends Collection<TOVALUE>, ENCOLLECTION extends Collection<ENVALUE>, TOVALUE extends TransferObject<TOVALUE, ENVALUE> & PrimaryKey<PK>, ENVALUE extends PrimaryKey<PK>, PK> extends BaseUpdater<TO, ENTITY, TOCOLLECTION, ENCOLLECTION> {

    /**
     * The Constructor.
     */
    protected final Supplier<TOVALUE> toValueConstructor;

    /**
     * The Constructor.
     */
    protected final Supplier<ENVALUE> enValueConstructor;

    /**
     * Instantiates a new Entity collection updater.
     *
     * @param toGeter            the target toGeter
     * @param toSetter           the target toSetter
     * @param toValueConstructor the to value constructor
     * @param dynamic            if the update should be dynamic processed and nulls will be ignored
     * @param entityGetter       the source toGeter
     * @param entitySetter       the entity setter
     * @param enValueConstructor the entity enValueConstructor
     */
    public EntityCollectionUpdater(final Function<TO, TOCOLLECTION> toGeter,
                                   final BiConsumer<TO, TOCOLLECTION> toSetter,
                                   final Supplier<TOVALUE> toValueConstructor,
                                   final boolean dynamic,
                                   final Function<ENTITY, ENCOLLECTION> entityGetter,
                                   final BiConsumer<ENTITY, ENCOLLECTION> entitySetter,
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
     * @param <E>                the type parameter of the source object
     * @param <CTV>              the Collection type
     * @param <CEV>              the type parameter
     * @param <TV>               the type parameter of the collection values (the entity)
     * @param <EV>               the type parameter
     * @param <K>                the type parameter of the primary key of the entity
     * @param toGetter           the target toGetter
     * @param toSetter           the target toSetter
     * @param toValueConstructor the to value constructor
     * @param dynamic            if the update should be dynamic processed and nulls will be ignored
     * @param entityGetter       the source toGetter
     * @param entitySetter       the entity setter
     * @param enValueConstructor the en value constructor
     * @param target             the target
     * @param source             the source
     * @param context            the context
     * @return true if the target changed
     */
    public static <T, E, CTV extends Collection<TV>, CEV extends Collection<EV>, TV extends TransferObject<TV, EV> & PrimaryKey<K>, EV extends PrimaryKey<K>, K> boolean updateEntityCollection(final Function<T, CTV> toGetter,
                                                                                                                                                                                                final BiConsumer<T, CTV> toSetter,
                                                                                                                                                                                                final Supplier<TV> toValueConstructor,
                                                                                                                                                                                                final boolean dynamic,
                                                                                                                                                                                                final Function<E, CEV> entityGetter,
                                                                                                                                                                                                final BiConsumer<E, CEV> entitySetter,
                                                                                                                                                                                                final Supplier<EV> enValueConstructor,
                                                                                                                                                                                                final T target,
                                                                                                                                                                                                final E source,
                                                                                                                                                                                                final Object context) {
        return new EntityCollectionUpdater<>(toGetter, toSetter, toValueConstructor, dynamic, entityGetter, entitySetter, enValueConstructor).update(target, source, context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean update(final TO transferObject,
                          final ENTITY entity,
                          final Object context) {
        // the toCollection to be updated
        final TOCOLLECTION toCollection = this.toGetter.apply(transferObject);
        // nulls
        if (toCollection == null) {
            if (this.dynamic || this.entityGetter.apply(entity) == null) // null ignore
            {
                return false;
            } else {
                this.warnNullCollection(transferObject);
                this.entitySetter.accept(entity, null);
                return true;
            }
        }
        final ENCOLLECTION enCollection = this.entityGetter.apply(entity);
        // collection not initialized
        if (enCollection == null) {
            this.throwNullCollection(entity);
            return false;
        }
        // empty
        if (toCollection.isEmpty()) {
            if (enCollection.isEmpty()) {
                return false;
            }
            enCollection.clear();
            return true;
        }
        // empty

        // collection work

        // create maps with entities that have pk.
        final Map<PK, TOVALUE> toMap = new LinkedHashMap<>();
        final List<TOVALUE> toList = new ArrayList<>(toCollection.size());
        for (final TOVALUE val : toCollection) {
            final PK PK = val.getId();
            if (PK == null) {
                toList.add(val);
            } else {
                toMap.put(PK, val);
            }
        }

        final Map<PK, ENVALUE> enMap = new LinkedHashMap<>();
        for (final ENVALUE val : enCollection) {
            final PK PK = val.getId();
            if (PK != null) {
                enMap.put(PK, val);
            }
        }

        final boolean updated = ValueUpdater.updateMap(toMap, enMap, this.enValueConstructor, context);
        final List<ENVALUE> targetValueList = ValueUpdater.updateList(toList, this.enValueConstructor, context);

        if (!targetValueList.isEmpty() || updated) {
            enCollection.clear();
            // add the updated entities
            enCollection.addAll(enMap.values());
            // add the new ones
            enCollection.addAll(targetValueList);
            // collection work
            // re set it
            this.entitySetter.accept(entity, enCollection);
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(final TO transferObject,
                       final ENTITY entity,
                       final Object context) {
        final ENCOLLECTION enCollectionValue = this.entityGetter.apply(entity);
        // no data
        if (enCollectionValue == null) {
            // no data no fun
            this.warnNullCollection(entity);
            return;
        }
        // the sourceValue to be updated
        final TOCOLLECTION toCollectionValue = this.toGetter.apply(transferObject);
        // nulls
        if (toCollectionValue == null) {
            this.throwNullCollection(transferObject);
            return;
        }
        if (enCollectionValue.isEmpty()) {
            return;
        }
        enCollectionValue.stream()
                         .map(envalue -> this.toValueConstructor.get()
                                                                .render(envalue, context))
                         .forEach(toCollectionValue::add);
    }
}
