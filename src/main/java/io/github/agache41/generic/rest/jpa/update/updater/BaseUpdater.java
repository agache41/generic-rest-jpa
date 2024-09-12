
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
import org.jboss.logging.Logger;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * The updater for simple value types (String, Integer).
 * It updates the field value in the entity based on the value of the field value in the transfer object
 *
 * @param <TO>      the type parameter of the transfer object
 * @param <ENTITY>  the type parameter of the entity
 * @param <TOVALUE> the type parameter
 * @param <ENVALUE> the type parameter
 */
public abstract class BaseUpdater<TO, ENTITY, TOVALUE, ENVALUE> implements Updater<TO, ENTITY> {

    /**
     * The constant log.
     */
    protected static final Logger log = Logger.getLogger(BaseUpdater.class);

    /**
     * The transfer object getter.
     */
    protected final Function<TO, TOVALUE> toGetter;

    /**
     * The transfer object getter.
     */
    protected final BiConsumer<TO, TOVALUE> toSetter;

    /**
     * If the update should be dynamic processed and nulls will be ignored.
     */
    protected final boolean dynamic;

    /**
     * The entity getter.
     */
    protected final Function<ENTITY, ENVALUE> entityGetter;

    /**
     * The entity setter.
     */
    protected final BiConsumer<ENTITY, ENVALUE> entitySetter;

    /**
     * Instantiates a new Value updater.
     *
     * @param toGetter     the transfer object getter
     * @param toSetter     the transfer object setter
     * @param dynamic      if the update should be dynamic processed and nulls will be ignored
     * @param entityGetter the entity getter
     * @param entitySetter the entity setter
     */
    public BaseUpdater(final Function<TO, TOVALUE> toGetter,
                       final BiConsumer<TO, TOVALUE> toSetter,
                       final boolean dynamic,
                       final Function<ENTITY, ENVALUE> entityGetter,
                       final BiConsumer<ENTITY, ENVALUE> entitySetter

    ) {
        this.toGetter = toGetter;
        this.toSetter = toSetter;
        this.dynamic = dynamic;
        this.entityGetter = entityGetter;
        this.entitySetter = entitySetter;
    }


    /**
     * Internal update method for maps.
     *
     * @param <KEY>              the type parameter
     * @param <TVALUE>           the type parameter
     * @param <ENVALUE>          the type parameter
     * @param toMap              the source value
     * @param enMap              the target value
     * @param enValueConstructor the enValueConstructor
     * @param context            the context
     * @return true if the target map has changed
     */
    protected static <KEY, TVALUE extends TransferObject<TVALUE, ENVALUE>, ENVALUE> boolean updateMap(final Map<KEY, TVALUE> toMap,
                                                                                                      final Map<KEY, ENVALUE> enMap,
                                                                                                      final Supplier<ENVALUE> enValueConstructor,
                                                                                                      final Object context) {

        final Set<KEY> enKeys = enMap.keySet();
        // make a copy to not change the input
        final Set<KEY> toKeys = new LinkedHashSet<>(toMap.keySet());
        //remove all that are now longer available
        boolean updated = enKeys.retainAll(toKeys);
        //update all that remained in the intersection
        updated = enKeys.stream()
                        .map(k -> toMap.get(k)
                                       .update(enMap.get(k), context))
                        .reduce(updated, (u, n) -> u || n);
        //remove those that are updated
        toKeys.removeAll(enKeys);
        //insert all new (that remained)
        if (!toKeys.isEmpty()) {
            updated = toKeys.stream()
                            .map(key -> {
                                final ENVALUE newValue = enValueConstructor.get();
                                final boolean upd = toMap.get(key)
                                                         .update(newValue, context);
                                enMap.put(key, newValue);
                                return upd;
                            })
                            .reduce(updated, (u, n) -> u || n);
        }
        return updated;
    }

    /**
     * Internal update method for lists.
     *
     * @param <TVALUE>           the type parameter
     * @param <EVALUE>           the type parameter
     * @param toList             the to list
     * @param enValueConstructor the enValueConstructor
     * @param context            the context
     * @return true if the target list has changes
     */
    protected static <TVALUE extends TransferObject<TVALUE, EVALUE>, EVALUE> List<EVALUE> updateList(final List<TVALUE> toList,
                                                                                                     final Supplier<EVALUE> enValueConstructor,
                                                                                                     final Object context) {
        return toList.stream()
                     .map(toValue -> {
                         final EVALUE enValue = enValueConstructor.get();
                         toValue.update(enValue, context);
                         return enValue;
                     })
                     .collect(Collectors.toList());
    }

    /**
     * Internal render method for collections.
     *
     * @param <TVALUE>           the type parameter
     * @param <EVALUE>           the type parameter
     * @param enValueCollection  the en value collection
     * @param toValueConstructor the to value constructor
     * @param context            the context
     * @return the collection
     */
    protected static <TVALUE extends TransferObject<TVALUE, EVALUE>, EVALUE> Collection<TVALUE> renderCollection(final Collection<EVALUE> enValueCollection,
                                                                                                                 final Supplier<TVALUE> toValueConstructor,
                                                                                                                 final Object context) {
        return enValueCollection.stream()
                                .map(envalue -> toValueConstructor.get()
                                                                  .render(envalue, context))
                                .collect(Collectors.toList());
    }

    /**
     * Warn null map.
     *
     * @param object the object
     */
    protected void warnNullMap(final Object object) {
        BaseUpdater.log.warnf("WARNING: Found uninitialized map in class %s.", object.getClass()
                                                                                     .getSimpleName());
    }

    /**
     * Warn null collection.
     *
     * @param object the object
     */
    protected void warnNullCollection(final Object object) {
        BaseUpdater.log.warnf("WARNING: Found uninitialized collection in class %s.", object.getClass()
                                                                                            .getSimpleName());
    }

    /**
     * Throw null map.
     *
     * @param object the object
     */
    protected void throwNullMap(final Object object) {
        BaseUpdater.log.errorf("ERROR: Found uninitialized map in class %s.", object.getClass()
                                                                                    .getSimpleName());
        throw new RuntimeException("ERROR: Found uninitialized map in class %s." + object.getClass()
                                                                                         .getSimpleName());
    }

    /**
     * Throw null collection.
     *
     * @param object the object
     */
    protected void throwNullCollection(final Object object) {
        BaseUpdater.log.errorf("ERROR: Found uninitialized collection in class %s.", object.getClass()
                                                                                           .getSimpleName());
        throw new RuntimeException("ERROR: Found uninitialized collection in class %s." + object.getClass()
                                                                                                .getSimpleName());
    }
}
