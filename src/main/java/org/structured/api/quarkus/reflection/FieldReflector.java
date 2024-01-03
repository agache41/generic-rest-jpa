
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

package org.structured.api.quarkus.reflection;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import org.structured.api.quarkus.dataAccess.PrimaryKey;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import static org.structured.api.quarkus.utils.StringUtils.capitalize;

/**
 * <pre>
 * The type Fieldreflector.
 * The class processes a single field and builds the necessary structure for the update pattern.
 * </pre>
 *
 * @param <T> the type parameter
 */
public final class FieldReflector<T, V> {
    private static final String GETTER_PREFIX = "get";
    private static final String GETTER_PRIM_BOOL_PREFIX = "is";
    private static final String SETTER_PREFIX = "set";
    private static final Object[] NULL = new Object[]{null};
    private final String name;
    private final Class<T> enclosingClass;
    private final Class<V> type;
    private Method getter;
    private Method setter;
    private boolean notNull;
    private boolean updateable;
    private boolean valid;

    /**
     * <pre>
     * Instantiates a new Field reflector.
     * </pre>
     *
     * @param enclosingClass the enclosing class
     * @param field          the field
     */
    FieldReflector(Class<T> enclosingClass, Field field) {
        this.enclosingClass = enclosingClass;
        this.name = field.getName();
        this.type = (Class<V>) field.getType();
        try {
            this.setter = this.getSetter();
            this.getter = this.getGetter();
            this.notNull = field.getAnnotation(Write.class)
                                .notNull()
                    || field.isAnnotationPresent(NotNull.class);
            this.updateable = (field.isAnnotationPresent(Write.class)
                    || enclosingClass.isAnnotationPresent(Write.class))
                    && !field.isAnnotationPresent(Id.class)
                    && !field.isAnnotationPresent(Write.excluded.class);
            this.valid = true;
        } catch (Exception e) {
            //
        }
    }

    /**
     * <pre>
     * Locates the getter Method in the Class.
     * </pre>
     *
     * @return the getter
     */
    public Method getGetter() {
        try {
            // the getter method to use
            return this.enclosingClass.getDeclaredMethod(
                    (boolean.class.equals(this.type) ?
                            GETTER_PRIM_BOOL_PREFIX :
                            GETTER_PREFIX) +
                            capitalize(this.name));
        } catch (SecurityException | NoSuchMethodException e) { // getter is faulty
            throw new IllegalArgumentException(e.getMessage() + " when getting getter for " + getName() + " in class " + enclosingClass.getCanonicalName(), e);
        }
    }

    /**
     * <pre>
     * Locates the setter Method in the class.
     * </pre>
     *
     * @return the setter
     */
    private Method getSetter() {
        try {
            // the setter method to use
            return this.enclosingClass.getDeclaredMethod(
                    SETTER_PREFIX +
                            capitalize(this.name),
                    this.type);
        } catch (SecurityException | NoSuchMethodException e) { // setter is faulty
            throw new IllegalArgumentException(e.getMessage() + " when getting setter for " + getName() + " in class " + enclosingClass.getCanonicalName(), e);
        }
    }

    /**
     * <pre>
     * Tells if the given class is a Collection.
     * </pre>
     *
     * @param c the c
     * @return the boolean
     */
    public static boolean isClassCollection(Class c) {
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
    public static boolean isMapCollection(Class c) {
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
        return ob != null && isMapCollection(ob.getClass());
    }

    /**
     * <pre>
     * Given the source and destination objects, does the update for the associated field.
     * </pre>
     *
     * @param destination the destination
     * @param source      the source
     * @return object
     */
    public Object update(T destination, T source) {
        try {
            final Object sourceValue = getter.invoke(source);
            // not null default case
            if (null == sourceValue && this.notNull) {
                return null;
            }
            // null case for entities
            if (sourceValue instanceof PrimaryKey<?>) {
                PrimaryKey<?> entity = (PrimaryKey) sourceValue;
                //if the provide entity is nullified
                if (entity.getId() == null) {
                    if (this.notNull) {
                        // not null default case
                        return null;
                    } // set field to null to break relation.
                    return setter.invoke(destination, NULL);
                }
            }
            // fix for using cascade all on hibernate
            if (isCollection(sourceValue)) {
                final Collection destinationCollection = (Collection) getter.invoke(destination);
                if (destinationCollection != null) {
                    destinationCollection.clear();
                    destinationCollection.addAll((Collection) sourceValue);
                    return destinationCollection;
                }
            }
            if (isMap(sourceValue)) {
                final Map destinationMap = (Map) getter.invoke(destination);
                if (destinationMap != null) {
                    destinationMap.clear();
                    destinationMap.putAll((Map) sourceValue);
                    return destinationMap;
                }
            }
            return setter.invoke(destination, sourceValue);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * <pre>
     * Object Getter.
     * </pre>
     *
     * @param source the source
     * @return the object
     */
    public V get(T source) {
        try {
            return (V) this.getter.invoke(source);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * <pre>
     * Object Setter
     * </pre>
     *
     * @param source the source
     * @param value  the value
     */
    public void set(T source, V value) {
        try {
            this.setter.invoke(source);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * <pre>
     * The Name of this field.
     * </pre>
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * <pre>
     * The enclosing class.
     * </pre>
     *
     * @return the enclosing class.
     */
    public Class<?> getEnclosingClass() {
        return enclosingClass;
    }

    /**
     * <pre>
     * The data Type of this field.
     * </pre>
     *
     * @return the type
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * <pre>
     * Tells if this field is marked as updatable.
     * </pre>
     *
     * @return the boolean
     */
    public boolean isUpdateable() {
        return updateable;
    }

    /**
     * <pre>
     * Tells if this FieldReflector is correctly constructed and can be used.
     * </pre>
     *
     * @return the boolean
     */
    public boolean isValid() {
        return valid;
    }


}
