
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

package io.github.agache41.generic.rest.jpa.utils;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The type Reflection utils.
 */
public class ReflectionUtils {

    private static final String GETTER_PREFIX = "get";
    private static final String GETTER_PRIM_BOOL_PREFIX = "is";
    private static final String SETTER_PREFIX = "set";

    /**
     * <pre>
     * Returns a list of declared fields in the class and in the base class(es).
     * </pre>
     *
     * @param cls the cls
     * @return the declared fields
     */
    public static List<Field> getDeclaredFields(Class<?> cls) {
        List<Field> declaredFields = new LinkedList<>();
        while (cls != null && !cls.equals(Object.class)) {
            Collections.addAll(declaredFields, cls.getDeclaredFields());
            cls = cls.getSuperclass();
        }
        return declaredFields;
    }


    /**
     * Gets getter.
     *
     * @param <T>            the type parameter
     * @param <V>            the type parameter
     * @param enclosingClass the enclosing class
     * @param name           the name
     * @param type           the type
     * @return the getter
     */
    public static <T, V> Function<T, V> getGetter(Class<T> enclosingClass, String name, Class<V> type) {
        final Method getterMethod = getGetterMethod(enclosingClass, name, type);
        return object -> {
            try {
                return (V) getterMethod.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        };
    }


    /**
     * Gets setter.
     *
     * @param <T>            the type parameter
     * @param <V>            the type parameter
     * @param enclosingClass the enclosing class
     * @param name           the name
     * @param type           the type
     * @return the setter
     */
    public static <T, V> BiConsumer<T, V> getSetter(Class<T> enclosingClass, String name, Class<V> type) {
        final Method setterMethod = getSetterMethod(enclosingClass, name, type);
        return (object, value) -> {
            try {
                setterMethod.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        };
    }


    /**
     * <pre>
     * Locates the setter Method in the class.
     * </pre>
     *
     * @param <T>            the type parameter
     * @param <V>            the type parameter
     * @param enclosingClass the enclosing class
     * @param name           the name
     * @param type           the type
     * @return the setter
     */
    public static <T, V> Method getSetterMethod(Class<T> enclosingClass, String name, Class<V> type) {
        try {
            // the setter method to use
            return enclosingClass.getDeclaredMethod(
                    getSetterName(name),
                    type);
        } catch (SecurityException | NoSuchMethodException e) { // setter is faulty
            throw new IllegalArgumentException(e.getMessage() + " looking for method " + enclosingClass.getSimpleName() + "." + getSetterName(StringUtils.capitalize(name)) + "( " + type.getSimpleName() + " value )", e);
        }
    }

    /**
     * <pre>
     * Locates the getter Method in the Class.
     * </pre>
     *
     * @param <T>            the type parameter
     * @param <V>            the type parameter
     * @param enclosingClass the enclosing class
     * @param name           the name
     * @param type           the type
     * @return the getter
     */
    public static <T, V> Method getGetterMethod(Class<T> enclosingClass, String name, Class<V> type) {
        try {
            // the getter method to use
            return enclosingClass.getDeclaredMethod(getGetterName(StringUtils.capitalize(name), type));
        } catch (SecurityException | NoSuchMethodException e) { // getter is faulty
            throw new IllegalArgumentException(e.getMessage() + " looking for method " + enclosingClass.getCanonicalName() + "." + getGetterName(StringUtils.capitalize(name), type) + "()", e);
        }
    }

    /**
     * Gets setter name.
     *
     * @param name the name
     * @return the setter name
     */
    public static String getSetterName(String name) {
        return SETTER_PREFIX +
                StringUtils.capitalize(name);

    }

    /**
     * Gets getter name.
     *
     * @param name the name
     * @param type the type
     * @return the getter name
     */
    public static String getGetterName(String name, Class<?> type) {
        return (boolean.class.equals(type) || Boolean.class.equals(type) ?
                GETTER_PRIM_BOOL_PREFIX :
                GETTER_PREFIX) +
                StringUtils.capitalize(name);
    }


    /**
     * Gets the no args constructor.
     *
     * @param <T>            the type parameter
     * @param enclosingClass the enclosing class
     * @return the no args constructor
     */
    public static <T> Constructor<T> getNoArgsConstructor(Class<T> enclosingClass) {
        try {
            return enclosingClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e.getMessage() + " looking for no arguments constructor " + enclosingClass.getCanonicalName() + "." + enclosingClass.getCanonicalName() + "()", e);
        }
    }

    /**
     * Gets the no args constructor.
     *
     * @param <T>            the type parameter
     * @param enclosingClass the enclosing class
     * @return the no args constructor
     */
    public static <T> Supplier<T> supplierOf(Class<T> enclosingClass) {
        final Constructor<T> constructor = getNoArgsConstructor(enclosingClass);
        return () -> {
            try {
                return constructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e.getMessage() + "by new " + enclosingClass.getCanonicalName() + "()", e);
            }
        };
    }

    /**
     * <pre>
     * Tells if the given class is a Collection.
     * </pre>
     *
     * @param c the c
     * @return the boolean
     */
    public static boolean isClassCollection(Class<?> c) {
        return Collection.class.isAssignableFrom(c);
    }

    /**
     * <pre>
     * Tells if the given object is a Collection.
     * </pre>
     *
     * @param ob the ob
     * @return the boolean
     */
    public static boolean isCollection(Object ob) {
        return ob != null && isClassCollection(ob.getClass());
    }

    /**
     * <pre>
     * Tells if the given class is a Map.
     * </pre>
     *
     * @param c the c
     * @return the boolean
     */
    public static boolean isClassMap(Class<?> c) {
        return Map.class.isAssignableFrom(c);
    }

    /**
     * <pre>
     * Tells if the given objects is a Map.
     * </pre>
     *
     * @param ob the ob
     * @return the boolean
     */
    public static boolean isMap(Object ob) {
        return ob != null && isClassMap(ob.getClass());
    }


    public static Class<?> getParameterType(Field field, int index) {
        Type genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedType))
            return null;
        ParameterizedType pType = (ParameterizedType) genericType;
        Type[] actualTypeArguments = pType.getActualTypeArguments();
        if (actualTypeArguments.length <= index)
            return null;
        return (Class<?>) actualTypeArguments[index];
    }
}
