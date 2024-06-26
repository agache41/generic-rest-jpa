
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

package io.github.agache41.generic.rest.jpa.producer;

import io.github.agache41.generic.rest.jpa.update.Updatable;
import io.github.agache41.generic.rest.jpa.update.reflector.ClassReflector;
import io.github.agache41.generic.rest.jpa.update.reflector.FieldReflector;
import jakarta.validation.constraints.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;


/**
 * Producer for Object Instances used for testing.
 * The class produces simple types and Object Beans by setting values in all
 * fields marked with the Update Annotation.
 * The Class
 *
 * @param <T> the type parameter
 */
public class Producer<T> {

    private static final Map<Class<?>, Producer<?>> producerCache = new ConcurrentHashMap<>();
    /**
     * The default size for collections and map generation.
     */
    public static int defaultSize = 16;

    static {
        Producer.add(new StringRandomProducer());
        //Producer.add(new EnglishWordsProducer());
        Producer.add(new IntegerRandomProducer());
        Producer.add(int.class, new IntegerRandomProducer());
        Producer.add(new LongRandomProducer());
        Producer.add(long.class, new LongRandomProducer());
        Producer.add(new DoubleRandomProducer());
        Producer.add(double.class, new DoubleRandomProducer());
        Producer.add(new BooleanRandomProducer());
        Producer.add(boolean.class, new BooleanRandomProducer());
        Producer.add(new ShortRandomProducer());
        Producer.add(short.class, new ShortRandomProducer());
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
     * The supplier for new list.
     * Defaults to LinkedList.
     */
    protected Supplier<List<T>> listSupplier;

    /**
     * The supplier for new maps.
     * Defaults to LinkedHashMap.
     */
    protected Supplier<Map<?, T>> mapSupplier;

    /**
     * Instantiates a new Producer.
     *
     * @param clazz the clazz
     */
    protected Producer(final Class<T> clazz) {
        this.clazz = clazz;
        this.size = defaultSize;
        this.listSupplier = LinkedList::new;
        this.mapSupplier = LinkedHashMap::new;
    }

    /**
     * Sets default size.
     *
     * @param size the size
     */
    public static void setDefaultSize(final int size) {
        Producer.defaultSize = size;
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
     * Adds a new Producer to the producer cache
     *
     * @param <T>      the type parameter
     * @param clazz    the clazz
     * @param supplier the supplier
     */
    public static <T> void add(final Class<T> clazz,
                               final Producer<? extends T> supplier) {
        producerCache.put(clazz, supplier);
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
     * With list producer.
     *
     * @param listSupplier the list supplier
     * @return the producer
     */
    public Producer<T> withList(final Supplier<List<T>> listSupplier) {
        this.listSupplier = listSupplier;
        return this;
    }

    /**
     * With map producer.
     *
     * @param mapSupplier the map supplier
     * @return the producer
     */
    public Producer<T> withMap(final Supplier<Map<?, T>> mapSupplier) {
        this.mapSupplier = mapSupplier;
        return this;
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
        final Map<K, T> result = (Map<K, T>) this.mapSupplier.get();
        final Producer<K> keySupplier = Producer.ofClass(keyType);
        for (int i = 0; i < mapSize; i++) {
            result.put(keySupplier.produce(), this.produce());
        }
        return result;
    }

    private int optionalSize(final int[] size) {
        if (size == null || size.length == 0 || size[0] < 0) {
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
        final List<T> result = this.listSupplier.get();
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
        return this.produceUpdatableFields(result);
    }

    /**
     * Produce a minimal instance of a new Object, inserting all the needed fields (nullable = false)
     *
     * @return the object
     */
    public T produceMinimal() {
        final T result = ClassReflector.ofClass(this.clazz)
                                       .newInstance();
        Stream.of(ClassReflector.ofClass(this.clazz)
                                .getUpdateReflectorsArray())
              .filter(not(FieldReflector::isNullable))
              .forEach(fieldReflector -> this.produceField(result, fieldReflector));
        return result;
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
        if (Updatable.class.isAssignableFrom(this.clazz)) {
            final T result = ClassReflector.ofClass(this.clazz)
                                           .newInstance();
            final Updatable updatableResult = (Updatable) result;
            updatableResult.update((Updatable) source);
            return this.produceUpdatableFields(result);
        }
        return this.produceUpdatableFields(source);
    }

    private T produceUpdatableFields(final T result) {

        for (final FieldReflector fieldReflector : ClassReflector.ofClass(this.clazz)
                                                                 .getUpdateReflectorsArray()) {
            this.produceField(result, fieldReflector);
        }
        return result;
    }

    /**
     * Produce field object.
     *
     * @param result         the result
     * @param fieldReflector the field reflector
     * @return the object
     */
    public Object produceField(final T result,
                               final FieldReflector fieldReflector) {

        final Object target = fieldReflector.get(result);
        if (!fieldReflector.isInsertable()) {
            return null;  // field not set and must not be inserted
        }
        if (target != null && !fieldReflector.isUpdatable()) {
            return target; // field already set and must not be updated
        }
        final int maxLength = fieldReflector.getLength();
        final Class<?> fieldType = fieldReflector.getType();
        Object fieldValue = null;
        if (fieldReflector.isValue()) {
            if (!String.class.equals(fieldType) || maxLength < 0) {
                fieldValue = Producer.ofClass(fieldType)
                                     .produce();
            } else {
                final String stringValue = Producer.ofClass(String.class)
                                                   .produce();
                if (stringValue.length() <= maxLength) {
                    fieldValue = stringValue;
                } else {
                    fieldValue = stringValue.substring(0, maxLength);
                }
            }
        } else if (fieldReflector.isCollection()) {
            final Class<Object> collectionType = fieldReflector.getFirstParameter();
            Collection<Object> collection = (Collection<Object>) target;
            final Producer<Object> objectProducer = Producer.ofClass(collectionType);
            if (collection == null) {
                if (List.class.isAssignableFrom(fieldType)) {
                    collection = (Collection<Object>) this.listSupplier.get();
                }
            }
            if (collection != null && collectionType != null) {
                final List<Object> applied = collection.stream()
                                                       .map(objectProducer::change)
                                                       .collect(Collectors.toList());
                collection.clear();
                collection.addAll(applied);
                collection.addAll(objectProducer.produceList(maxLength));
                fieldValue = collection;
            }
        } else if (fieldReflector.isMap()) {
            final Class<Object> mapKeyParameter = fieldReflector.getFirstParameter();
            final Class<Object> mapValueParameter = fieldReflector.getSecondParameter();
            final Producer<Object> mapSupplier = Producer.ofClass(mapValueParameter);
            Map map = (Map<Object, Object>) target;
            if (map == null) {
                if (Map.class.isAssignableFrom(fieldType)) {
                    map = this.mapSupplier.get();
                }
            }
            if (map != null && mapKeyParameter != null && mapValueParameter != null) {
                mapSupplier.changeMap(map);
                map.putAll(mapSupplier.produceMap(mapKeyParameter, maxLength));
                fieldValue = map;
            }
        } else {
            // do recurse on the type
            fieldValue = ((Producer<Object>) Producer.ofClass(fieldType)).change(target);
        }
        if (fieldValue != null) {
            fieldReflector.set(result, fieldValue);
        }
        return fieldValue;
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
     * @return the producer
     */
    public Producer<T> withSize(final int size) {
        this.size = size;
        return this;
    }
}