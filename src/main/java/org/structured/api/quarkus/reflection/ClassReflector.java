package org.structured.api.quarkus.reflection;

import org.structured.api.quarkus.exceptions.UnexpectedException;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <pre>
 * The type Classreflector.
 * The class processes a given class and builds the necessary structure for implementing the update pattern.
 * It reflects the properties and methods of the given class and builds a dynamic class cache.
 * Typical usage :
 *      T source;
 *      T destination;
 *      destination = ClassReflector.ofObject(destination).update(destination, source);
 * </pre>
 *
 * @param <T> the type parameter
 */
public final class ClassReflector<T> {

    private static final Map<Class<?>, ClassReflector<?>> concurrentClassDescriptorsCache = new ConcurrentHashMap<>();
    private final Map<String, FieldReflector<T>> reflectors;
    private final Map<String, FieldReflector<T>> updateReflectors;

    private ClassReflector(final Class<T> sourceClass) {

        this.reflectors = getDeclaredFields(sourceClass)
                .stream()
                .map(field -> new FieldReflector<>(sourceClass, field))
                .filter(FieldReflector::isValid)
                .collect(Collectors.toMap(FieldReflector::getName, Function.identity()));

        this.updateReflectors = this.reflectors.values()
                .stream()
                .filter(FieldReflector::isUpdateable)
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
    public static <R> ClassReflector<R> ofClass(Class<R> clazz) {
        return (ClassReflector<R>) concurrentClassDescriptorsCache.computeIfAbsent(clazz, ClassReflector::new);
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
    public static <R> ClassReflector<R> ofObject(R object) {
        return (ClassReflector<R>) concurrentClassDescriptorsCache.computeIfAbsent(object.getClass(), ClassReflector::new);
    }


    /**
     * <pre>
     * Given a source and destination, it will update all corresponding fields according to the @Write annotation.
     * </pre>
     *
     * @param destination the destination
     * @param source      the source
     * @return t
     */
    public T update(T destination, T source) {
        this.updateReflectors.values()
                .forEach(fieldReflector -> fieldReflector.update(destination, source));
        return destination;
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
        FieldReflector fieldReflector = this.reflectors.get(fieldName);
        if (fieldReflector == null)
            throw new UnexpectedException(" No such field " + fieldName + " in " + source.getClass()
                    .getSimpleName());
        return fieldReflector.get(source);
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
        FieldReflector fieldReflector = this.reflectors.get(fieldName);
        if (fieldReflector == null)
            throw new UnexpectedException(" No such field " + fieldName + " in " + source.getClass()
                    .getSimpleName());
        fieldReflector.set(source, value);
    }

    /**
     * <pre>
     * Returns a list of declared fields in the class and in the base class(es).
     * </pre>
     *
     * @param cls the cls
     * @return the declared fields
     */
    private List<Field> getDeclaredFields(Class<?> cls) {
        List<Field> declaredFields = new LinkedList<>();
        while (cls != null) {
            Collections.addAll(declaredFields, cls.getDeclaredFields());
            cls = cls.getSuperclass();
        }
        return declaredFields;
    }
}