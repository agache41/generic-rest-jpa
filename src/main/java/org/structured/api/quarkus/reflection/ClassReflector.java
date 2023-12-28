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
     * Given an object of a class, it returns the associated classdescriptor.
     * There will be just one class descriptor per class (Singleton)
     *
     * @param clazz
     * @return
     */
    public static <R> ClassReflector<R> ofClass(Class<R> clazz) {
        return (ClassReflector<R>) concurrentClassDescriptorsCache.computeIfAbsent(clazz, ClassReflector::new);
    }

    public static <R> ClassReflector<R> ofObject(R object) {
        return (ClassReflector<R>) concurrentClassDescriptorsCache.computeIfAbsent(object.getClass(), ClassReflector::new);
    }


    /**
     * Given a source and destination, it will update all corresponding fields according to the @Write annotation.
     *
     * @param destination
     * @param source
     * @return
     */
    public T update(T destination, T source) {
        this.updateReflectors.values()
                .forEach(fieldReflector -> fieldReflector.update(destination, source));
        return destination;
    }

    public Object get(T source, String fieldName) {
        FieldReflector fieldReflector = this.reflectors.get(fieldName);
        if (fieldReflector == null)
            throw new UnexpectedException(" No such field " + fieldName + " in " + source.getClass()
                    .getSimpleName());
        return fieldReflector.get(source);
    }

    public void set(T source, String fieldName, Object value) {
        FieldReflector fieldReflector = this.reflectors.get(fieldName);
        if (fieldReflector == null)
            throw new UnexpectedException(" No such field " + fieldName + " in " + source.getClass()
                    .getSimpleName());
        fieldReflector.set(source, value);
    }

    public List<Field> getDeclaredFields(Class<?> cls) {
        List<Field> declaredFields = new LinkedList<>();
        while (cls != null) {
            Collections.addAll(declaredFields, cls.getDeclaredFields());
            cls = cls.getSuperclass();
        }
        return declaredFields;
    }
}