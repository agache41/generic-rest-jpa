
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

import io.github.agache41.generic.rest.jpa.dataAccess.PrimaryKey;
import io.github.agache41.generic.rest.jpa.update.updater.*;
import io.github.agache41.generic.rest.jpa.utils.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <pre>
 * The type Fieldreflector.
 * The class processes a single field and builds the necessary structure for the update pattern.
 * </pre>
 *
 * @param <T> the type parameter
 */
public final class FieldReflector<T extends Updateable<T>, V> {
    private final String name;
    private final Class<T> enclosingClass;
    private final Class<V> type;
    private final Class<?> firstParameter;
    private final Class<?> secondParameter;
    private final Function<T, V> getter;
    private final BiConsumer<T, V> setter;
    private final boolean notNull;
    private final boolean updatable;
    private final boolean valid;
    private final Updater<T, T> updater;

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
        this.firstParameter = ReflectionUtils.getParameterType(field, 0);
        this.secondParameter = ReflectionUtils.getParameterType(field, 1);
        boolean lValid;
        Function<T, V> lGetter = null;
        BiConsumer<T, V> lSetter = null;
        try {
            lGetter = ReflectionUtils.getGetter(this.enclosingClass, this.name, this.type);
            lSetter = ReflectionUtils.getSetter(this.enclosingClass, this.name, this.type);
            lValid = true;
        } catch (Exception e) {
            lValid = false;
        }
        this.valid = lValid;

        if (!this.valid) {
            this.getter = null;
            this.setter = null;
            this.updater = null;
            this.updatable = false;
            this.notNull = false;
            return;
        }
        this.getter = lGetter;
        this.setter = lSetter;

        // if the field is annotated
        if (field.isAnnotationPresent(Update.class)) {
            this.updatable = true;
            this.notNull = field.getAnnotation(Update.class)
                                .notNull();
            // or the class is annotated and the field is not excluded
        } else if (enclosingClass.isAnnotationPresent(Update.class) && !field.isAnnotationPresent(Update.excluded.class)) {
            this.updatable = true;
            this.notNull = enclosingClass.getAnnotation(Update.class)
                                         .notNull();
        } else {
            this.updatable = false;
            this.notNull = false;
        }

        System.out.println("private\t"
                + this.type.getSimpleName()
                + (this.firstParameter == null ? "" : "<" + this.firstParameter.getSimpleName()
                + (this.secondParameter == null ? "" : "," + this.secondParameter.getSimpleName())
                + ">")
                + "\t\t"
                + this.name + ";");

        if (ReflectionUtils.isClassCollection(this.type) && this.firstParameter != null) {
            if (Updateable.class.isAssignableFrom(firstParameter)
                    && PrimaryKey.class.isAssignableFrom(firstParameter)) {
                //collection of entities
                this.updater = new EntityCollectionUpdater<>(
                        (BiConsumer<T, Collection<UpdateableAndPrimaryKey>>) this.setter,
                        (Function<T, Collection<UpdateableAndPrimaryKey>>) this.getter,
                        this.notNull,
                        (Function<T, Collection<UpdateableAndPrimaryKey>>) this.getter,
                        (Supplier<UpdateableAndPrimaryKey>) ReflectionUtils.supplierOf(firstParameter));
            } else {
                //collection of simple objects
                this.updater = new CollectionUpdater<>(
                        (BiConsumer<T, Collection<Object>>) setter,
                        (Function<T, Collection<Object>>) getter,
                        this.notNull,
                        (Function<T, Collection<Object>>) getter);
            }
        } else if (ReflectionUtils.isClassMap(this.type) && firstParameter != null && secondParameter != null) {
            if (Updateable.class.isAssignableFrom(secondParameter)) {
                //map of entities
                this.updater = new EntityMapUpdater<>(
                        (BiConsumer<T, Map<Object, Updateable>>) setter,
                        (Function<T, Map<Object, Updateable>>) getter,
                        this.notNull,
                        (Function<T, Map<Object, Updateable>>) getter,
                        (Supplier<Updateable>) ReflectionUtils.supplierOf(secondParameter));
            } else {
                //map of simple objects
                this.updater = new MapUpdater<>(
                        (BiConsumer<T, Map<Object, Object>>) setter,
                        (Function<T, Map<Object, Object>>) getter,
                        this.notNull,
                        (Function<T, Map<Object, Object>>) getter);
            }
        } else if (Updateable.class.isAssignableFrom(this.type)) {
            // entity
            this.updater = new ValueEntityUpdater<>(
                    (BiConsumer<T, Updateable>) setter,
                    (Function<T, Updateable>) getter,
                    this.notNull,
                    (Function<T, Updateable>) getter,
                    (Supplier<Updateable>) ReflectionUtils.supplierOf(this.type));
        } else {
            // simple value
            this.updater = new ValueUpdater<>(setter, getter, this.notNull, getter);
        }
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
    public boolean update(T destination, T source) {
        return this.updater.update(destination, source);
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
        return this.getter.apply(source);
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
        this.setter.accept(source, value);
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
    public boolean isUpdatable() {
        return updatable;
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
