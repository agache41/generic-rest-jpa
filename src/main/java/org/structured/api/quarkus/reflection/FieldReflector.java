package org.structured.api.quarkus.reflection;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import org.structured.api.quarkus.dao.PrimaryKey;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

/**
 * <pre>
 * The type Fieldreflector.
 * The class processes a single field and builds the necessary structure for the update pattern.
 * </pre>
 *
 * @param <T> the type parameter
 */
public final class FieldReflector<T> {
    private static final String GETTER_PREFIX = "get";
    private static final String GETTER_PRIM_BOOL_PREFIX = "is";
    private static final String SETTER_PREFIX = "set";
    private static final Object[] NULL = new Object[]{null};
    private final String name;
    private Method readMethod;
    private Method writeMethod;
    private boolean notNull;
    private boolean updateable;
    private boolean valid;

    /**
     * <pre>
     * Instantiates a new Field reflector.
     * </pre>
     *
     * @param workingClass the working class
     * @param field        the field
     */
    FieldReflector(Class<T> workingClass, Field field) {
        this.name = field.getName();
        try {
            this.writeMethod = FieldReflector.getSetter(workingClass, field);
            this.readMethod = FieldReflector.getGetter(workingClass, field);
            this.notNull = field.getAnnotation(Write.class)
                    .notNull()
                    || field.isAnnotationPresent(NotNull.class);
            this.updateable = (field.isAnnotationPresent(Write.class)
                    || workingClass.isAnnotationPresent(Write.class))
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
     * @param definingClass the defining class
     * @param field         the field
     * @return the getter
     */
    public static Method getGetter(Class<?> definingClass, Field field) {
        try {
            // the getter method to use
            return definingClass.getDeclaredMethod(
                    (boolean.class.equals(field.getType()) ?
                            GETTER_PRIM_BOOL_PREFIX :
                            GETTER_PREFIX) +
                            capitalize(field.getName()));
        } catch (SecurityException | NoSuchMethodException e) { // getter is faulty
            throw new IllegalArgumentException(e.getMessage() + " when getting getter for " + field.getName() + " in class " + definingClass.getCanonicalName(), e);
        }
    }

    /**
     * <pre>
     * Locates the setter Method in the class.
     * </pre>
     *
     * @param definingClass the defining class
     * @param field         the field
     * @return the setter
     */
    public static Method getSetter(Class<?> definingClass, Field field) {
        try {
            // the setter method to use
            return definingClass.getDeclaredMethod(
                    SETTER_PREFIX +
                            capitalize(field.getName()),
                    field.getType());
        } catch (SecurityException | NoSuchMethodException e) { // setter is faulty
            throw new IllegalArgumentException(e.getMessage() + " when getting setter for " + field.getName() + " in class " + definingClass.getCanonicalName(), e);
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
            final Object sourceValue = readMethod.invoke(source);
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
                    return writeMethod.invoke(destination, NULL);
                }
            }
            // fix for using cascade all on hibernate
            if (isCollection(sourceValue)) {
                final Collection destinationCollection = (Collection) readMethod.invoke(destination);
                if (destinationCollection != null) {
                    destinationCollection.clear();
                    destinationCollection.addAll((Collection) sourceValue);
                    return destinationCollection;
                }
            }
            if (isMap(sourceValue)) {
                final Map destinationMap = (Map) readMethod.invoke(destination);
                if (destinationMap != null) {
                    destinationMap.clear();
                    destinationMap.putAll((Map) sourceValue);
                    return destinationMap;
                }
            }
            return writeMethod.invoke(destination, sourceValue);
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
    public Object get(T source) {
        try {
            return this.readMethod.invoke(source);
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
    public void set(T source, Object value) {
        try {
            this.writeMethod.invoke(source);
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

    /**
     * <pre>
     * Capitalizes a string
     * </pre>
     * @param input
     * @return
     */
    private static String capitalize(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }
}
