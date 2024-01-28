
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


public class Producer<T> {

    public static final int defaultCollectionSize = 10;

    private static final Map<Class<?>, Producer<?>> supplierCache = new ConcurrentHashMap<>();

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

    protected final Random random = new Random();
    private final Class<T> clazz;
    private int collectionSize;

    public Producer(final Class<T> clazz,
                    final int collectionSize) {
        this.clazz = clazz;
        this.collectionSize = collectionSize;
    }

    public Producer(final Class<T> clazz) {
        this.clazz = clazz;
        this.collectionSize = defaultCollectionSize;
    }

    public static void add(final Producer<?> supplier) {
        supplierCache.put(supplier.getClazz(), supplier);
    }


    public static <R> Producer<R> ofClass(@NotNull final Class<R> clazz) {
        return (Producer<R>) supplierCache.computeIfAbsent(clazz, cls -> new Producer(cls));
    }

    public static <K, V> Map<K, V> getMap(final Class<K> keyType,
                                          final Class<V> valueType) {
        final Map<K, V> result = new HashMap<>();
        final Producer<V> valueSupplier = Producer.ofClass(valueType);
        final Producer<K> keySupplier = Producer.ofClass(keyType);
        for (int i = 0; i < valueSupplier.getCollectionSize(); i++) {
            result.put(keySupplier.produce(), valueSupplier.produce());
        }
        return result;
    }

    public List<T> getList() {
        return this.getList(this.collectionSize);
    }

    public List<T> getList(final int size) {
        final List<T> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
            result.add(this.produce());
        return result;
    }

    public List<T> applyList(final List<T> source) {
        return source.stream()
                     .map(this::change)
                     .collect(Collectors.toList());
    }

    public T produce() {
        final T result = ClassReflector.ofClass(this.clazz)
                                       .newInstance();
        return this.processFields(result);
    }

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
                                              .getList());
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
                    for (final Object key : map.keySet()) {
                        map.put(key, mapSupplier.change(map.get(key)));
                    }
                    map.putAll(getMap(mapKeyParameter, mapValueParameter));
                }
            } else {
                // do recurse on the type
                fieldReflector.set(result, ((Producer<Object>) Producer.ofClass(fieldType))
                        .change(target));
            }
        }
        return result;
    }

    public Class<T> getClazz() {
        return this.clazz;
    }

    public int getCollectionSize() {
        return this.collectionSize;
    }

    public void setCollectionSize(final int collectionSize) {
        this.collectionSize = collectionSize;
    }

}