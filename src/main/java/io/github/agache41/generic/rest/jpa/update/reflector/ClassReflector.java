
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
import io.github.agache41.generic.rest.jpa.update.TransferObject;
import io.github.agache41.generic.rest.jpa.update.Update;
import io.github.agache41.generic.rest.jpa.utils.ReflectionUtils;
import org.jboss.logging.Logger;

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
 *      T transferObject;
 *      S entity;
 *      destination = {@link ClassReflector#ofClass(Class)}.update(transferObject, entity);
 * </pre>
 *
 * @param <T> the type parameter
 * @param <S> the type parameter
 */
public final class ClassReflector<T, S> {

    private static final Logger log = Logger.getLogger(ClassReflector.class);

    /**
     * <pre>
     * The cache map of available ClassReflectors, reachable by class
     * </pre>
     */
    private static final Map<Class<?>, Map<Class<?>, ClassReflector<?, ?>>> concurrentClassReflectorCache = new ConcurrentHashMap<>();

    /**
     * <pre>
     * The main type of ClassReflector (the type of the transfer Object)
     * </pre>
     */
    private final Class<T> clazz;

    /**
     * <pre>
     * The type of the entity used
     * </pre>
     */
    private final Class<S> associatedClass;

    /**
     * <pre>
     * The no arguments constructor associated for the transfer object type.
     * </pre>
     */
    private final Constructor<T> noArgsConstructor;
    /**
     * <pre>
     * The cache map of all available reflectors for fields, reachable by field name
     * </pre>
     */
    private final Map<String, FieldReflector<T, S, ?, ?>> reflectors;
    /**
     * <pre>
     * The cache map of all available reflectors for fields marked for update, reachable by field name
     * </pre>
     */
    private final Map<String, FieldReflector<T, S, ?, ?>> updateReflectors;

    /**
     * <pre>
     * The array of all available reflectors for fields marked for update.
     * The array is provided for fast iteration purposes.
     * </pre>
     */
    private final FieldReflector[] updateReflectorsArray;
    /**
     * <pre>
     *  The array of all available reflectors for fields holding simple types (no entities, collections or maps) for update.
     *  The array is provided for fast iteration purposes.
     * </pre>
     */
    private final FieldReflector[] valueReflectorsArray;
    /**
     * <pre>
     * The description of this ClassReflector, saved to be reused by toString  Method.
     * It actually holds a generated copy of the type class source code,
     * helpfully when debugging issues.
     * </pre>
     */
    private final String description;

    /**
     * <pre>
     * Tells of this class is final. (Ex. String)
     * </pre>
     */
    private final boolean isFinal;

    /**
     * <pre>
     * Constructs this ClassReflector.
     * The constructor is used when invoking a .forClass() static method.
     * </pre>
     */
    private ClassReflector(final Class<T> sourceClass,
                           final Class<S> associatedClass) {

        this.clazz = sourceClass;

        this.associatedClass = associatedClass;

        this.isFinal = Modifier.isFinal(sourceClass.getModifiers());

        this.noArgsConstructor = ReflectionUtils.getNoArgsConstructor(sourceClass);

        this.reflectors = ReflectionUtils.getDeclaredFields(sourceClass)
                                         .stream()
                                         .map(field -> new FieldReflector<>(sourceClass, associatedClass, field))
                                         .filter(FieldReflector::isValid)
                                         .collect(Collectors.toMap(FieldReflector::getName, Function.identity()));

        this.reflectors.putAll(ReflectionUtils.getDeclaredMethods(sourceClass)
                                              .stream()
                                              .filter(method -> method.isAnnotationPresent(Update.class))
                                              .map(method -> new FieldReflector<>(sourceClass, associatedClass, method))
                                              .filter(FieldReflector::isValid)
                                              //filter already mapped fields reflectors
                                              .filter(fieldReflector -> !this.reflectors.containsKey(fieldReflector.getName()))
                                              .collect(Collectors.toMap(FieldReflector::getName, Function.identity())));

        this.updateReflectors = this.reflectors.values()
                                               .stream()
                                               .filter(FieldReflector::isActiv)
                                               .collect(Collectors.toMap(FieldReflector::getName, Function.identity()));
        this.updateReflectorsArray = this.updateReflectors.values()
                                                          .stream()
                                                          .sorted(Comparator.comparing(FieldReflector::getOrder))
                                                          .collect(Collectors.toList())
                                                          .toArray(new FieldReflector[this.updateReflectors.size()]);

        final List<FieldReflector<T, S, ?, ?>> valueReflectors = this.updateReflectors.values()
                                                                                      .stream()
                                                                                      .filter(FieldReflector::isValue)
                                                                                      .collect(Collectors.toList());
        this.valueReflectorsArray = valueReflectors.toArray(new FieldReflector[valueReflectors.size()]);

        this.description = this.description();

        log.debugf("ClassReflector is parsing :\r\n %s \r\n", this.toString());
    }

    /**
     * <pre>
     * Given a class, it returns the ClassReflector using the same type as associated.
     * There will be just one class descriptor per class (Singleton)
     * </pre>
     *
     * @param <R>   the type parameter
     * @param clazz the clazz
     * @return class reflector
     */
    public static <R> ClassReflector<R, R> ofClass(final Class<R> clazz) {
        return ofClass(clazz, clazz);
    }

    /**
     * <pre>
     * Given a class, it returns the ClassReflector for the associated Type.
     * There will be just one class descriptor per class and associated type (Singleton)
     * </pre>
     *
     * @param <R>             the type parameter
     * @param <U>             the associated type parameter
     * @param clazz           the clazz
     * @param associatedClass the associated class
     * @return class reflector
     */
    public static <R, U> ClassReflector<R, U> ofClass(final Class<R> clazz,
                                                      final Class<U> associatedClass) {
        return (ClassReflector<R, U>) concurrentClassReflectorCache.computeIfAbsent(clazz, cls -> new ConcurrentHashMap<>())
                                                                   .computeIfAbsent(associatedClass, cls -> new ClassReflector(cls, associatedClass));
    }

    /**
     * <pre>
     * Given an object of a class, it returns the associated ClassReflector.
     * There will be just one class descriptor per class (Singleton)
     * </pre>
     *
     * @param <R>    the type parameter
     * @param object the object of type
     * @return the class reflector
     */
    public static <R> ClassReflector<R, R> ofObject(final R object) {
        return ofClass((Class<R>) object.getClass());
    }

    /**
     * <pre>
     * Given an transferObject of a class, it returns the associated ClassReflector.
     * There will be just one class descriptor per class (Singleton)
     * </pre>
     *
     * @param <R>            the type parameter
     * @param <U>            the associated type parameter
     * @param transferObject the transferObject
     * @param entity         the associated transferObject
     * @return the class reflector
     */
    public static <R, U> ClassReflector<R, U> ofObject(final R transferObject,
                                                       final U entity) {
        return ofClass((Class<R>) transferObject.getClass(), (Class<U>) entity.getClass());
    }

    /**
     * Clone r.
     *
     * @param <R>            the type parameter
     * @param transferObject the transfer object
     * @return the r
     */
    public static <R> R clone(final R transferObject) {
        final ClassReflector<R, R> classReflector = ClassReflector.ofObject(transferObject);
        final R result = classReflector.newInstance();
        classReflector.update(transferObject, result);
        return result;
    }


    private String description() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("public class ");
        stringBuilder.append(this.clazz.getSimpleName());
        if (this.clazz.equals(this.associatedClass)) {
            stringBuilder.append(" implements SelfTransferObject<");
            stringBuilder.append(this.clazz.getSimpleName());
        } else {
            stringBuilder.append(" implements TransferObject<");
            stringBuilder.append(this.clazz.getSimpleName());
            stringBuilder.append(",");
            stringBuilder.append(this.associatedClass.getSimpleName());
        }
        stringBuilder.append("> { \r\n");
        this.reflectors.values()
                       .stream()
                       .sorted(Comparator.comparing(FieldReflector::getOrder))
                       .map(Objects::toString)
                       .forEach(stringBuilder::append);
        stringBuilder.append(" }\r\n");
        return stringBuilder.toString();
    }

    /**
     * <pre>
     * Given a entity and a transferObject,
     * it will update all corresponding fields in the entity based on the fields annotated with the @ {@link Update} annotation in the transfer object.
     * </pre>
     *
     * @param transferObject the transferObject
     * @param entity         the entity
     * @return the transferObject
     */
    public boolean update(final T transferObject,
                          final S entity) {
        boolean updated = false;
        for (final FieldReflector reflector : this.updateReflectorsArray) {
            updated |= reflector.update(transferObject, entity);
        }
        return updated;
    }

    /**
     * <pre>
     * Given a entity and a transferObject,
     * it will update all corresponding fields in the transfer object annotated with the @ {@link Update} annotation based on the corresponding fields in the entity.
     * </pre>
     *
     * @param transferObject the transferObject
     * @param entity         the entity
     * @return the transferObject
     */
    public T render(final T transferObject,
                    final S entity) {
        final boolean updated = false;
        for (final FieldReflector reflector : this.updateReflectorsArray) {
            reflector.render(transferObject, entity);
        }
        return transferObject;
    }

    /**
     * Tells if the two objects are equal from the Update perspective.
     *
     * @param destination the destination
     * @param source      the source
     * @return the boolean
     */
    @Deprecated
    public boolean areEqual(final T destination,
                            final Object source) {
        if (destination == null) {
            return source == null;
        }
        if (source == null) {
            log.debugf("Found un-equal to null");
            return false;
        }

        if (!source.getClass()
                   .equals(this.clazz)) {
            log.debugf("Found un-equal type classes!");
            return false;
        }
        boolean result = true;
        for (final FieldReflector reflector : this.updateReflectorsArray) {
            final Object valueL = reflector.get(destination);
            final Object valueR = reflector.get(source);
            if (valueL == null) {
                result &= valueR == null;
            } else if (valueR == null) {
                log.debugf("Found un-equal field %s.%s left=%s right=null", this.clazz.getSimpleName(), reflector.getName(), valueL.toString());
                result = false;
            } else if (TransferObject.class.isAssignableFrom(valueR.getClass())) {
                if (!((TransferObject<?, ?>) valueL).updateEquals(valueR)) {
                    log.debugf("Found un-equal field %s.%s left=%s right=%s", this.clazz.getSimpleName(), reflector.getName(), valueL.toString(), valueR.toString());
                    result = false;
                }
            } else {
                if (!Objects.equals(valueL, valueR)) {
                    log.debugf("Found un-equal field %s.%s left=%s right=%s", this.clazz.getSimpleName(), reflector.getName(), valueL.toString(), valueR.toString());
                    result = false;
                }
            }
        }
        return result;
    }

    /**
     * <pre>
     * Reflector for the fieldName.
     * </pre>
     *
     * @param <TV>      the type parameter
     * @param <TS>      the type parameter
     * @param fieldName the field name
     * @return the reflector
     */
    public <TV, TS> FieldReflector<T, S, TV, TS> getReflector(final String fieldName) {
        final FieldReflector<T, S, TV, TS> fieldReflector = (FieldReflector<T, S, TV, TS>) this.reflectors.get(fieldName);
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
     * @param <TV>      the type parameter
     * @param <TS>      the type parameter
     * @param fieldName the field name
     * @param fieldType the field type
     * @return the reflector
     */
    public <TV, TS> FieldReflector<T, S, TV, TS> getReflector(final String fieldName,
                                                              final Class<TV> fieldType) {
        final FieldReflector<T, S, TV, TS> fieldReflector = (FieldReflector<T, S, TV, TS>) this.reflectors.get(fieldName);
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
     * @param source       the source
     * @param nonTransient the non transient
     * @return the hash map
     */
    public HashMap<String, Object> mapValues(final T source,
                                             final boolean nonTransient) {
        final HashMap<String, Object> result = new LinkedHashMap<>();
        for (final FieldReflector<T, S, ?, ?> fieldReflector : this.valueReflectorsArray) {
            if (nonTransient && fieldReflector.isTransient()) {
                continue;
            }
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
     * @param sources      the sources
     * @param nonTransient the non transient
     * @return the hash map
     */
    public HashMap<String, List<Object>> mapValues(final List<T> sources,
                                                   final boolean nonTransient) {
        final HashMap<String, List<Object>> result = new LinkedHashMap<>();
        for (final FieldReflector<T, S, ?, ?> fieldReflector : this.valueReflectorsArray) {
            if (nonTransient && fieldReflector.isTransient()) {
                continue;
            }
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

    /**
     * Creates a new instance of current type;
     *
     * @return the t
     */
    public T newInstance() {
        try {
            return this.noArgsConstructor.newInstance();
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the cache map of all available reflectors for fields, reachable by field name.
     *
     * @return the reflectors
     */
    public Map<String, FieldReflector<T, S, ?, ?>> getReflectors() {
        return this.reflectors;
    }

    /**
     * Gets the cache map of all available reflectors for fields marked for update, reachable by field name.
     *
     * @return the update reflectors
     */
    public Map<String, FieldReflector<T, S, ?, ?>> getUpdateReflectors() {
        return this.updateReflectors;
    }

    /**
     * Gets all available reflectors for fields marked for update.
     *
     * @return the field reflector [ ]
     */
    public FieldReflector[] getUpdateReflectorsArray() {
        return this.updateReflectorsArray;
    }

    /**
     * Gets all available reflectors for fields holding values for update.
     *
     * @return the field reflector [ ]
     */
    public FieldReflector[] getValueReflectorsArray() {
        return this.valueReflectorsArray;
    }

    /**
     * Tells if the class is final
     *
     * @return the boolean
     */
    public boolean isFinal() {
        return this.isFinal;
    }

    @Override
    public String toString() {
        return this.description;
    }


}