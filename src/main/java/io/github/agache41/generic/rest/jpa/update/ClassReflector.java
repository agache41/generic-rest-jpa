
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

package io.github.agache41.generic.rest.jpa.update;

import io.github.agache41.generic.rest.jpa.exceptions.UnexpectedException;
import io.github.agache41.generic.rest.jpa.utils.ReflectionUtils;
import jakarta.validation.constraints.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
public final class ClassReflector<T extends Updateable<T>> {

    private static final Map<Class<?>, ClassReflector<?>> concurrentClassDescriptorsCache = new ConcurrentHashMap<>();
    private final Class<T> clazz;
    /**
     * <pre>
     * The no arguments constructor associated for the type.
     * </pre>
     */
    private final Constructor<T> noArgsConstructor;
    private final Map<String, FieldReflector<T, Object>> reflectors;
    private final Map<String, FieldReflector<T, Object>> updateReflectors;

    private ClassReflector(final Class<T> sourceClass) {

        System.out.println("Class " + sourceClass.getSimpleName());
        this.clazz = sourceClass;

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
    public static <R extends Updateable<R>> ClassReflector<R> ofClass(@NotNull Class<R> clazz) {
        return (ClassReflector<R>) concurrentClassDescriptorsCache.computeIfAbsent(clazz, cls -> new ClassReflector(cls));
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
    public static <R extends Updateable<R>> ClassReflector<R> ofObject(@NotNull R object) {
        return (ClassReflector<R>) concurrentClassDescriptorsCache.computeIfAbsent(object.getClass(), cls -> new ClassReflector(cls));
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
    public boolean update(T destination, T source) {
        return this.updateReflectors.values()
                                    .stream()
                                    .map(fieldReflector -> fieldReflector.update(destination, source))
                                    .reduce(false, (u, n) -> u || n);

    }


    /**
     * <pre>
     * Reflector for the fieldName.
     * </pre>
     *
     * @param fieldName the field name
     * @return the reflector
     */
    public FieldReflector<T, Object> getReflector(String fieldName) {
        FieldReflector<T, Object> fieldReflector = this.reflectors.get(fieldName);
        if (fieldReflector == null)
            throw new UnexpectedException(" No such field " + fieldName + " in " + this.clazz.getSimpleName());
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
    public <V> FieldReflector<T, V> getReflector(String fieldName, Class<V> fieldType) {
        FieldReflector<T, V> fieldReflector = (FieldReflector<T, V>) this.reflectors.get(fieldName);
        if (fieldReflector == null)
            throw new UnexpectedException(" No such field " + fieldName + " in " + this.clazz.getSimpleName());
        if (!fieldType.equals(fieldReflector.getType()))
            throw new UnexpectedException(" Field" + fieldName + " in " + this.clazz.getSimpleName() + " has type " + fieldReflector.getType()
                                                                                                                                    .getSimpleName() + " and not " + fieldType.getSimpleName());
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
    public Object get(T source, String fieldName) {
        return getReflector(fieldName).get(source);
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
    public void set(T source, String fieldName, Object value) {
        getReflector(fieldName).set(source, value);
    }

    /**
     * Map the fields with values of the source object as values in a hash map.
     *
     * @param source the source
     * @return the hash map
     */
    public HashMap<String, Object> mapValues(T source) {
        HashMap<String, Object> result = new LinkedHashMap<>();
        for (FieldReflector<T, ?> fieldReflector : this.reflectors.values()) {
            Object value = fieldReflector.get(source);
            if (value == null) continue;
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
    public HashMap<String, List<Object>> mapValues(List<T> sources) {
        HashMap<String, List<Object>> result = new LinkedHashMap<>();
        for (FieldReflector<T, ?> fieldReflector : this.reflectors.values()) {
            List<Object> values = sources
                    .stream()
                    .map(fieldReflector::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (values.isEmpty()) continue;
            result.put(fieldReflector.getName(), values);
        }
        return result;
    }

    public T newInstance() {
        try {
            return this.noArgsConstructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }


    public static <ENTITY extends Updateable<ENTITY>> ENTITY create(ENTITY value) {
        if (value == null) return null;
        ENTITY result = ClassReflector.ofObject(value)
                                      .newInstance();
        result.update(value);
        return result;
    }
}