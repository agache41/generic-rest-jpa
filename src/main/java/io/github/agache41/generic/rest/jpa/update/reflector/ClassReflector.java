
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

package io.github.agache41.generic.rest.jpa.update.reflector;

import io.github.agache41.generic.rest.jpa.exceptions.UnexpectedException;
import io.github.agache41.generic.rest.jpa.update.Update;
import io.github.agache41.generic.rest.jpa.update.Updateable;
import io.github.agache41.generic.rest.jpa.utils.ReflectionUtils;
import jakarta.validation.constraints.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <pre>
 * The type {@link ClassReflector}
 * The class processes a given class and builds the necessary structure for implementing the update pattern.
 * It reflects the properties and methods of the given class and builds a dynamic class cache.
 * Typical usage :
 *      T source;
 *      T destination;
 *      destination = {@link ClassReflector#ofClass(Class)}.update(destination, source);
 * </pre>
 *
 * @param <T> the type parameter
 */
public final class ClassReflector<T> {

    private static final Map<Class<?>, ClassReflector<?>> concurrentClassReflectorCache = new ConcurrentHashMap<>();
    private final Class<T> clazz;

    /**
     * <pre>
     * The no arguments constructor associated for the type.
     * </pre>
     */
    private final Constructor<T> noArgsConstructor;
    private final Map<String, FieldReflector<T, Object>> reflectors;
    private final Map<String, FieldReflector<T, Object>> updateReflectors;

    private final FieldReflector[] updateReflectorsArray;
    private final FieldReflector[] valueReflectorsArray;
    private final String description;

    private final boolean isFinal;

    private ClassReflector(final Class<T> sourceClass) {

        this.clazz = sourceClass;

        this.isFinal = Modifier.isFinal(sourceClass.getModifiers());

        this.noArgsConstructor = ReflectionUtils.getNoArgsConstructor(sourceClass);

        this.reflectors = ReflectionUtils.getDeclaredFields(sourceClass)
                                         .stream()
                                         .map(field -> new FieldReflector<>(sourceClass, field))
                                         .filter(FieldReflector::isValid)
                                         .collect(Collectors.toMap(FieldReflector::getName, Function.identity()));

        this.updateReflectors = this.reflectors.values()
                                               .stream()
                                               .filter(FieldReflector::isUpdatable)
                                               .collect(Collectors.toMap(FieldReflector::getName, Function.identity()));
        this.updateReflectorsArray = this.updateReflectors.values()
                                                          .toArray(new FieldReflector[this.updateReflectors.size()]);

        final List<FieldReflector<T, Object>> valueReflectors = this.updateReflectors.values()
                                                                                     .stream()
                                                                                     .filter(FieldReflector::isValue)
                                                                                     .collect(Collectors.toList());
        this.valueReflectorsArray = valueReflectors.toArray(new FieldReflector[valueReflectors.size()]);

        this.description = this.description();
        //todo : add logging
        System.out.println(this);
    }

    /**
     * <pre>
     * Given a class, it returns the associated classdescriptor.
     * There will be just one class descriptor per class (Singleton)
     * </pre>
     *
     * @param <R>   the type parameter
     * @param clazz the clazz
     * @return class reflector
     */
    public static <R> ClassReflector<R> ofClass(@NotNull final Class<R> clazz) {
        return (ClassReflector<R>) concurrentClassReflectorCache.computeIfAbsent(clazz, cls -> new ClassReflector(cls));
    }

    /**
     * <pre>
     * Given an object of a class, it returns the associated classdescriptor.
     * There will be just one class descriptor per class (Singleton)
     * </pre>
     *
     * @param <R>    the type parameter
     * @param object the object
     * @return the class reflector
     */
    public static <R> ClassReflector<R> ofObject(@NotNull final R object) {
        return (ClassReflector<R>) concurrentClassReflectorCache.computeIfAbsent(object.getClass(), cls -> new ClassReflector(cls));
    }

    public static <ENTITY extends Updateable<ENTITY>> ENTITY create(final ENTITY value) {
        if (value == null) {
            return null;
        }
        final ENTITY result = ClassReflector.ofObject(value)
                                            .newInstance();
        result.update(value);
        return result;
    }

    private String description() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("public class \t");
        stringBuilder.append(this.clazz.getSimpleName());
        stringBuilder.append(" { \r\n");
        this.reflectors.values()
                       .stream()
                       .map(Objects::toString)
                       .forEach(stringBuilder::append);
        stringBuilder.append(" }\r\n");
        return stringBuilder.toString();
    }

    /**
     * <pre>
     * Given a source and a destination,
     * it will update all corresponding fields annotated with the @ {@link Update} annotation.
     * </pre>
     *
     * @param destination the destination
     * @param source      the source
     * @return the destination
     */
    public boolean update(final T destination,
                          final T source) {
        boolean updated = false;
        for (final FieldReflector reflector : this.updateReflectorsArray) {
            updated |= reflector.update(destination, source);
        }
        return updated;
    }

    /**
     * <pre>
     * Reflector for the fieldName.
     * </pre>
     *
     * @param fieldName the field name
     * @return the reflector
     */
    public FieldReflector<T, Object> getReflector(final String fieldName) {
        final FieldReflector<T, Object> fieldReflector = this.reflectors.get(fieldName);
        if (fieldReflector == null) {
            throw new UnexpectedException(" No such field " + fieldName + " in " + this.clazz.getSimpleName());
        }
        return fieldReflector;
    }

    /**
     * <pre>
     * Reflector for the fieldName.
     * </pre>
     *
     * @param <V>       the type parameter
     * @param fieldName the field name
     * @param fieldType the field type
     * @return the reflector
     */
    public <V> FieldReflector<T, V> getReflector(final String fieldName,
                                                 final Class<V> fieldType) {
        final FieldReflector<T, V> fieldReflector = (FieldReflector<T, V>) this.reflectors.get(fieldName);
        if (fieldReflector == null) {
            throw new UnexpectedException(" No such field " + fieldName + " in " + this.clazz.getSimpleName());
        }
        if (!fieldType.equals(fieldReflector.getType())) {
            throw new UnexpectedException(" Field" + fieldName + " in " + this.clazz.getSimpleName() + " has type " + fieldReflector.getType()
                                                                                                                                    .getSimpleName() + " and not " + fieldType.getSimpleName());
        }
        return fieldReflector;
    }

    /**
     * <pre>
     * Getter for the fieldName in source.
     * </pre>
     *
     * @param source    the source
     * @param fieldName the field name
     * @return the object
     */
    public Object get(final T source,
                      final String fieldName) {
        return this.getReflector(fieldName)
                   .get(source);
    }

    /**
     * <pre>
     * Setter for the fieldName in source.
     * </pre>
     *
     * @param source    the source
     * @param fieldName the field name
     * @param value     the value
     */
    public void set(final T source,
                    final String fieldName,
                    final Object value) {
        this.getReflector(fieldName)
            .set(source, value);
    }

    /**
     * Map the fields with values of the source object as values in a hash map.
     *
     * @param source the source
     * @return the hash map
     */
    public HashMap<String, Object> mapValues(final T source) {
        final HashMap<String, Object> result = new LinkedHashMap<>();
        for (final FieldReflector<T, ?> fieldReflector : this.valueReflectorsArray) {
            final Object value = fieldReflector.get(source);
            if (value == null) {
                continue;
            }
            result.put(fieldReflector.getName(), value);
        }
        return result;
    }

    /**
     * Map the fields with values of the source objects as values in a hash map.
     *
     * @param sources the sources
     * @return the hash map
     */
    public HashMap<String, List<Object>> mapValues(final List<T> sources) {
        final HashMap<String, List<Object>> result = new LinkedHashMap<>();
        for (final FieldReflector<T, ?> fieldReflector : this.valueReflectorsArray) {
            final List<Object> values = sources.stream()
                                               .map(fieldReflector::get)
                                               .filter(Objects::nonNull)
                                               .collect(Collectors.toList());
            if (values.isEmpty()) {
                continue;
            }
            result.put(fieldReflector.getName(), values);
        }
        return result;
    }

    public T newInstance() {
        try {
            return this.noArgsConstructor.newInstance();
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public Constructor<T> getNoArgsConstructor() {
        return this.noArgsConstructor;
    }

    public Map<String, FieldReflector<T, Object>> getReflectors() {
        return this.reflectors;
    }

    public Map<String, FieldReflector<T, Object>> getUpdateReflectors() {
        return this.updateReflectors;
    }

    public FieldReflector[] getUpdateReflectorsArray() {
        return this.updateReflectorsArray;
    }

    public FieldReflector[] getValueReflectorsArray() {
        return this.valueReflectorsArray;
    }

    public boolean isFinal() {
        return this.isFinal;
    }

    @Override
    public String toString() {
        return this.description;
    }
}