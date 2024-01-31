
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

package io.github.agache41.generic.rest.jpa.filler;

import io.github.agache41.generic.rest.jpa.update.Updateable;
import io.github.agache41.generic.rest.jpa.update.reflector.ClassReflector;
import io.github.agache41.generic.rest.jpa.update.reflector.FieldReflector;
import jakarta.validation.constraints.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * Producer for Object Instances used for testing.
 * The class produces simple types and Object Beans by setting values in all
 * fields marked with the Update Annotation.
 * The Class
 *
 * @param <T> the type parameter
 */
public class Producer<T> {

    /**
     * The default size for collections and map generation.
     */
    public static final int defaultSize = 64;

    private static final Map<Class<?>, Producer<?>> producerCache = new ConcurrentHashMap<>();

    static {
        //UpdateSupplier.add(new StringRandomSupplier());
        Producer.add(new EnglishWordsProducer());
        Producer.add(new IntegerRandomProducer());
        Producer.add(new LongRandomProducer());
        Producer.add(new DoubleRandomProducer());
        Producer.add(new DoubleRandomProducer());
        Producer.add(new BooleanRandomProducer());
        Producer.add(new ShortRandomProducer());
        Producer.add(new BigDecimalRandomProducer());
        Producer.add(new BigIntegerRandomProducer());
    }

    /**
     * The Random instance used for random values.
     */
    protected final Random random = new Random();
    /**
     * The Class type of produced objects.
     */
    protected final Class<T> clazz;
    /**
     * The Size of generated collection and maps.
     * Defaults to the static defaultSize
     */
    protected int size;

    /**
     * Instantiates a new Producer.
     *
     * @param clazz the clazz
     */
    protected Producer(final Class<T> clazz) {
        this.clazz = clazz;
        this.size = defaultSize;
    }

    /**
     * Adds a new Producer to the producer cache
     *
     * @param supplier the supplier
     */
    public static void add(final Producer<?> supplier) {
        producerCache.put(supplier.getClazz(), supplier);
    }


    /**
     * Of class producer.
     *
     * @param <R>   the type parameter
     * @param clazz the clazz
     * @return the producer
     */
    public static <R> Producer<R> ofClass(@NotNull final Class<R> clazz) {
        return (Producer<R>) producerCache.computeIfAbsent(clazz, cls -> new Producer(cls));
    }

    /**
     * Produce map map.
     *
     * @param <K>     the type parameter
     * @param keyType the key type
     * @param size    the size
     * @return the map
     */
    public <K> Map<K, T> produceMap(final Class<K> keyType,
                                    final int... size) {
        final int mapSize = this.optionalSize(size);
        final Map<K, T> result = new HashMap<>(mapSize);
        final Producer<K> keySupplier = Producer.ofClass(keyType);
        for (int i = 0; i < mapSize; i++) {
            result.put(keySupplier.produce(), this.produce());
        }
        return result;
    }

    private int optionalSize(final int[] size) {
        if (size == null || size.length == 0) {
            return this.size;
        } else {
            return size[0];
        }
    }

    /**
     * Change map map.
     *
     * @param <K> the type parameter
     * @param map the map
     * @return the map
     */
    public <K> Map<K, T> changeMap(final Map<K, T> map) {
        for (final K key : map.keySet()) {
            map.put(key, this.change(map.get(key)));
        }
        return map;
    }

    /**
     * Produce list list.
     *
     * @param size the size
     * @return the list
     */
    public List<T> produceList(final int... size) {
        final int listSize = this.optionalSize(size);
        final List<T> result = new ArrayList<>(listSize);
        for (int i = 0; i < listSize; i++)
            result.add(this.produce());
        return result;
    }

    /**
     * Change list list.
     *
     * @param source the source
     * @return the list
     */
    public List<T> changeList(final List<T> source) {
        return source.stream()
                     .map(this::change)
                     .collect(Collectors.toList());
    }

    /**
     * Produce t.
     *
     * @return the t
     */
    public T produce() {
        final T result = ClassReflector.ofClass(this.clazz)
                                       .newInstance();
        return this.processFields(result);
    }

    /**
     * Change t.
     *
     * @param source the source
     * @return the t
     */
    public T change(final T source) {
        if (source == null) {
            return this.produce();
        }
        if (Updateable.class.isAssignableFrom(this.clazz)) {
            final T result = ClassReflector.ofClass(this.clazz)
                                           .newInstance();
            final Updateable updateableResult = (Updateable) result;
            updateableResult.update((Updateable) this.produce());
            return result;
        }
        return this.processFields(source);
    }

    private T processFields(final T result) {

        for (final FieldReflector fieldReflector : ClassReflector.ofClass(this.clazz)
                                                                 .getUpdateReflectorsArray()) {
            final Class<?> fieldType = fieldReflector.getType();
            final Object target = fieldReflector.get(result);
            if (fieldReflector.isValue()) {
                fieldReflector.set(result, Producer.ofClass(fieldType)
                                                   .produce());
            } else if (fieldReflector.isCollection()) {
                final Class<Object> collectionType = fieldReflector.getFirstParameter();
                Collection<Object> collection = (Collection<Object>) target;
                final Producer<Object> collectionSupplier = Producer.ofClass(collectionType);
                if (collection == null) {
                    if (List.class.isAssignableFrom(fieldType)) {
                        collection = new ArrayList<>();
                        fieldReflector.set(result, collection);
                    }
                }
                if (collection != null && collectionType != null) {
                    final List<Object> applied = collection.stream()
                                                           .map(collectionSupplier::change)
                                                           .collect(Collectors.toList());
                    collection.clear();
                    collection.addAll(applied);
                    collection.addAll(collectionSupplier
                                              .produceList());
                }
            } else if (fieldReflector.isMap()) {
                final Class<Object> mapKeyParameter = fieldReflector.getFirstParameter();
                final Class<Object> mapValueParameter = fieldReflector.getSecondParameter();
                final Producer<Object> mapSupplier = Producer.ofClass(mapValueParameter);
                Map map = (Map<Object, Object>) target;
                if (map == null) {
                    if (Map.class.isAssignableFrom(fieldType)) {
                        map = new LinkedHashMap();
                        fieldReflector.set(result, map);
                    }
                }
                if (map != null && mapKeyParameter != null && mapValueParameter != null) {
                    mapSupplier.changeMap(map);
                    map.putAll(mapSupplier.produceMap(mapKeyParameter));
                }
            } else {
                // do recurse on the type
                fieldReflector.set(result, ((Producer<Object>) Producer.ofClass(fieldType))
                        .change(target));
            }
        }
        return result;
    }

    /**
     * Gets clazz.
     *
     * @return the clazz
     */
    public Class<T> getClazz() {
        return this.clazz;
    }

    /**
     * Gets size.
     *
     * @return the size
     */
    public int getSize() {
        return this.size;
    }

    /**
     * Sets size.
     *
     * @param size the size
     */
    public Producer<T> ofSize(final int size) {
        this.size = size;
        return this;
    }

}