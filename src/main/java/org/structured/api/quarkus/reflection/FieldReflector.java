package org.structured.api.quarkus.reflection;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.structured.api.quarkus.dao.PrimaryKey;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

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

    public static Method getGetter(Class<?> definingClass, Field field) {
        try {
            // the getter method to use
            return definingClass.getDeclaredMethod(
                    (boolean.class.equals(field.getType()) ?
                            GETTER_PRIM_BOOL_PREFIX :
                            GETTER_PREFIX) +
                            StringUtils.capitalize(field.getName()));
        } catch (SecurityException | NoSuchMethodException e) { // getter is faulty
            throw new IllegalArgumentException(e.getMessage() + " when getting getter for " + field.getName() + " in class " + definingClass.getCanonicalName(), e);
        }
    }

    public static Method getSetter(Class<?> definingClass, Field field) {
        try {
            // the setter method to use
            return definingClass.getDeclaredMethod(
                    SETTER_PREFIX +
                            StringUtils.capitalize(field.getName()),
                    field.getType());
        } catch (SecurityException | NoSuchMethodException e) { // setter is faulty
            throw new IllegalArgumentException(e.getMessage() + " when getting setter for " + field.getName() + " in class " + definingClass.getCanonicalName(), e);
        }
    }

    public static boolean isClassCollection(Class c) {
        return Collection.class.isAssignableFrom(c);
    }

    public static boolean isCollection(Object ob) {
        return ob != null && isClassCollection(ob.getClass());
    }

    public static boolean isMapCollection(Class c) {
        return Map.class.isAssignableFrom(c);
    }

    public static boolean isMap(Object ob) {
        return ob != null && isMapCollection(ob.getClass());
    }

    /**
     * Given the source and destination objects, does the update for the associated field.
     *
     * @param destination
     * @param source
     * @return
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

    public Object get(T source) {
        try {
            return this.readMethod.invoke(source);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void set(T source, Object value) {
        try {
            this.writeMethod.invoke(source);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String getName() {
        return name;
    }

    public boolean isUpdateable() {
        return updateable;
    }

    public boolean isValid() {
        return valid;
    }
}
