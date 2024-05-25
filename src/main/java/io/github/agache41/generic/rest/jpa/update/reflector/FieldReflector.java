
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

import io.github.agache41.generic.rest.jpa.dataAccess.PrimaryKey;
import io.github.agache41.generic.rest.jpa.update.Updatable;
import io.github.agache41.generic.rest.jpa.update.Update;
import io.github.agache41.generic.rest.jpa.update.updater.*;
import io.github.agache41.generic.rest.jpa.utils.ReflectionUtils;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import org.jboss.logging.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
 * @param <V> the type parameter
 */
public final class FieldReflector<T, V> {

    private static final Logger log = Logger.getLogger(ClassReflector.class);
    private final String name;
    private final Class<T> enclosingClass;
    private final Class<V> type;
    private final Class<?> firstParameter;
    private final Class<?> secondParameter;
    private final Function<T, V> getter;
    private final BiConsumer<T, V> setter;
    private final Field field;
    private final boolean notNull;
    private final boolean updatable;
    private final boolean isFinal;
    private final boolean isTransient;
    private final boolean isHibernateIntern;
    private final boolean isEager;
    private final boolean valid;
    private final Column columnAnnotation;
    private final Update updateAnnotation;
    private final String description;
    private final int length;
    private Updater<T, T> updater;
    private boolean map;
    private boolean collection;
    private boolean value;

    /**
     * <pre>
     * Instantiates a new Field reflector.
     * </pre>
     *
     * @param enclosingClass the enclosing class
     * @param field          the field
     */
    FieldReflector(final Class<T> enclosingClass,
                   final Field field) {
        this.enclosingClass = enclosingClass;
        this.field = field;
        this.name = field.getName();
        this.type = (Class<V>) field.getType();
        this.isFinal = Modifier.isFinal(field.getModifiers());
        this.isTransient = Modifier.isTransient(field.getModifiers());
        this.isHibernateIntern = field.getName()
                                      .startsWith("$$");
        this.firstParameter = ReflectionUtils.getParameterType(field, 0);
        this.secondParameter = ReflectionUtils.getParameterType(field, 1);
        this.getter = ReflectionUtils.getGetter(this.enclosingClass, this.name, this.type);
        this.setter = ReflectionUtils.getSetter(this.enclosingClass, this.name, this.type);
        this.valid = this.getter != null && this.setter != null;
        if (field.isAnnotationPresent(Column.class)) {
            this.columnAnnotation = field.getAnnotation(Column.class);
        } else {
            this.columnAnnotation = null;
        }
        // if the field is annotated
        if (field.isAnnotationPresent(Update.class) && !field.isAnnotationPresent(Update.excluded.class)) {
            this.updateAnnotation = field.getAnnotation(Update.class);
            // or the class is annotated and the field is not excluded
        } else if (enclosingClass.isAnnotationPresent(Update.class) && !field.isAnnotationPresent(Update.excluded.class)) {
            this.updateAnnotation = enclosingClass.getAnnotation(Update.class);
        } else {
            this.updateAnnotation = null;
        }

        if (field.isAnnotationPresent(OneToMany.class)) {
            this.isEager = field.getAnnotation(OneToMany.class)
                                .fetch()
                                .equals(FetchType.EAGER);
        } else {
            this.isEager = false;
        }

        if (!this.isFinal && !this.isHibernateIntern && this.updateAnnotation != null) {
            this.updatable = true;
            this.notNull = this.updateAnnotation.notNull();
        } else {
            // no update needed.
            this.updatable = false;
            this.notNull = false;
            this.updater = null;
            this.value = false;
            this.collection = false;
            this.map = false;
            this.length = this.length();
            this.description = this.description();
            return;
        }
        this.initialize();
        this.length = this.length();
        this.description = this.description();
    }

    /**
     * <pre>
     * Instantiates a new Field reflector.
     * </pre>
     *
     * @param enclosingClass the enclosing class
     * @param method         the method
     */
    FieldReflector(final Class<T> enclosingClass,
                   final Method method) {
        this.enclosingClass = enclosingClass;
        this.field = null;
        this.name = ReflectionUtils.getSetterFieldName(method);
        this.type = method.getParameterTypes().length == 1 ? (Class<V>) method.getParameterTypes()[0] : null;
        this.isFinal = (this.name == null || this.type == null);
        // using transient to mark setter/getter update without field
        this.isTransient = true;
        this.isHibernateIntern = false;
        this.firstParameter = ReflectionUtils.getParameterType(method, 0, 0);
        this.secondParameter = ReflectionUtils.getParameterType(method, 0, 1);
        this.getter = ReflectionUtils.getGetter(this.enclosingClass, this.name, this.type);
        this.setter = ReflectionUtils.getSetter(this.enclosingClass, this.name, this.type);
        this.valid = this.getter != null && this.setter != null;
        this.columnAnnotation = null;
        // if the method is annotated
        if (method.isAnnotationPresent(Update.class) && !method.isAnnotationPresent(Update.excluded.class)) {
            this.updateAnnotation = method.getAnnotation(Update.class);
            // or the class is annotated and the field is not excluded
        } else if (enclosingClass.isAnnotationPresent(Update.class) && !method.isAnnotationPresent(Update.excluded.class)) {
            this.updateAnnotation = enclosingClass.getAnnotation(Update.class);
        } else {
            this.updateAnnotation = null;
        }

        this.isEager = false;

        if (!this.isFinal && !this.isHibernateIntern && this.updateAnnotation != null) {
            this.updatable = true;
            this.notNull = this.updateAnnotation.notNull();
        } else {
            // no update needed.
            this.updatable = false;
            this.notNull = false;
            this.updater = null;
            this.value = false;
            this.collection = false;
            this.map = false;
            this.length = this.length();
            this.description = this.description();
            return;
        }
        this.initialize();
        this.length = this.length();
        this.description = this.description();
    }

    private int length() {
        //default value;
        int length = Update.defaultLength;
        // a simple field with column
        if (this.columnAnnotation != null && !this.collection && !this.map) {
            length = this.columnAnnotation.length();
        }
        //if update has length
        if (this.updateAnnotation != null && this.updateAnnotation.length() != Update.defaultLength) {
            length = this.updateAnnotation.length();
        }
        return length;
    }

    private void initialize() {
        if (!this.valid) {
            throw new IllegalArgumentException(" Invalid field marked for update " + this);
        }
        if (ReflectionUtils.isClassCollection(this.type) && this.firstParameter != null) {
            this.value = false;
            this.map = false;
            this.collection = true;
            if (Updatable.class.isAssignableFrom(this.firstParameter) && PrimaryKey.class.isAssignableFrom(this.firstParameter)) {
                //collection of entities
                this.updater = new EntityCollectionUpdater<>((BiConsumer<T, Collection<UpdatablePrimaryKey>>) this.setter, (Function<T, Collection<UpdatablePrimaryKey>>) this.getter, this.notNull, (Function<T, Collection<UpdatablePrimaryKey>>) this.getter, (Supplier<UpdatablePrimaryKey>) ReflectionUtils.supplierOf(this.firstParameter));
            } else {
                //collection of simple objects
                this.updater = new CollectionUpdater<>((BiConsumer<T, Collection<Object>>) this.setter, (Function<T, Collection<Object>>) this.getter, this.notNull, (Function<T, Collection<Object>>) this.getter);
            }
        } else if (ReflectionUtils.isClassMap(this.type) && this.firstParameter != null && this.secondParameter != null) {
            this.value = false;
            this.map = true;
            this.collection = false;
            if (Updatable.class.isAssignableFrom(this.secondParameter)) {
                //map of entities
                this.updater = new EntityMapUpdater<>((BiConsumer<T, Map<Object, Updatable>>) this.setter, (Function<T, Map<Object, Updatable>>) this.getter, this.notNull, (Function<T, Map<Object, Updatable>>) this.getter, (Supplier<Updatable>) ReflectionUtils.supplierOf(this.secondParameter));
            } else {
                //map of simple objects
                this.updater = new MapUpdater<>((BiConsumer<T, Map<Object, Object>>) this.setter, (Function<T, Map<Object, Object>>) this.getter, this.notNull, (Function<T, Map<Object, Object>>) this.getter);
            }
        } else if (Updatable.class.isAssignableFrom(this.type)) {
            this.value = false;
            this.map = false;
            this.collection = false;
            // entity
            this.updater = new EntityUpdater<>((BiConsumer<T, Updatable>) this.setter, (Function<T, Updatable>) this.getter, this.notNull, (Function<T, Updatable>) this.getter, (Supplier<Updatable>) ReflectionUtils.supplierOf(this.type));
        } else {
            // simple value
            this.value = true;
            this.map = false;
            this.collection = false;
            this.updater = new ValueUpdater<>(this.setter, this.getter, this.notNull, this.getter);
        }
    }

    /**
     * <pre>
     * Given the source and destination objects, does the update for the associated field.
     * </pre>
     *
     * @param destination the destination
     * @param source      the source
     * @return object boolean
     */
    public boolean update(final T destination,
                          final T source) {
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
    public V get(final T source) {
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
    public void set(final T source,
                    final V value) {
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
        return this.name;
    }

    /**
     * <pre>
     * The enclosing class.
     * </pre>
     *
     * @return the enclosing class.
     */
    public Class<?> getEnclosingClass() {
        return this.enclosingClass;
    }

    /**
     * <pre>
     * The data Type of this field.
     * </pre>
     *
     * @return the type
     */
    public Class<?> getType() {
        return this.type;
    }

    /**
     * <pre>
     * Tells if this field is marked as updatable.
     * </pre>
     *
     * @return the boolean
     */
    public boolean isUpdatable() {
        return this.updatable;
    }

    /**
     * <pre>
     * Tells if this FieldReflector is correctly constructed and can be used.
     * </pre>
     *
     * @return the boolean
     */
    public boolean isValid() {
        return this.valid;
    }


    /**
     * Gets first parameter.
     *
     * @return the first parameter
     */
    public Class<?> getFirstParameter() {
        return this.firstParameter;
    }

    /**
     * Gets second parameter.
     *
     * @return the second parameter
     */
    public Class<?> getSecondParameter() {
        return this.secondParameter;
    }

    /**
     * Gets getter.
     *
     * @return the getter
     */
    public Function<T, V> getGetter() {
        return this.getter;
    }

    /**
     * Gets setter.
     *
     * @return the setter
     */
    public BiConsumer<T, V> getSetter() {
        return this.setter;
    }

    /**
     * Is not null boolean.
     *
     * @return the boolean
     */
    public boolean isNotNull() {
        return this.notNull;
    }

    /**
     * Is map boolean.
     *
     * @return the boolean
     */
    public boolean isMap() {
        return this.map;
    }

    /**
     * Is collection boolean.
     *
     * @return the boolean
     */
    public boolean isCollection() {
        return this.collection;
    }

    /**
     * Is value boolean.
     *
     * @return the boolean
     */
    public boolean isValue() {
        return this.value;
    }

    /**
     * Gets updater.
     *
     * @return the updater
     */
    public Updater<T, T> getUpdater() {
        return this.updater;
    }

    /**
     * Is final boolean.
     *
     * @return the boolean
     */
    public boolean isFinal() {
        return this.isFinal;
    }

    /**
     * Is transient boolean.
     *
     * @return the boolean
     */
    public boolean isTransient() {
        return this.isTransient;
    }

    /**
     * Is eager boolean.
     *
     * @return the boolean
     */
    public boolean isEager() {
        return this.isEager;
    }

    /**
     * Gets column annotation.
     *
     * @return the column annotation
     */
    public Column getColumnAnnotation() {
        return this.columnAnnotation;
    }

    public Update getUpdateAnnotation() {
        return this.updateAnnotation;
    }

    public int getLength() {
        return this.length;
    }

    @Override
    public String toString() {
        return this.description;
    }

    private String description() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\r\n");
        if (!this.valid) {
            stringBuilder.append("[Warning] Invalid Field (no valid setter and getter) [Warning]");
            return stringBuilder.toString();
        }
        if (this.updatable) {
            stringBuilder.append("\t@Update");
            if (this.length != Update.defaultLength || !this.notNull) {
                stringBuilder.append("(");
            }
            if (this.length != Update.defaultLength) {
                stringBuilder.append("length = ");
                stringBuilder.append(this.length);
            }
            if (this.length != Update.defaultLength && !this.notNull) {
                stringBuilder.append(", ");
            }
            if (!this.notNull) {
                stringBuilder.append("notNull = false");
            }
            if (this.length != -1 || !this.notNull) {
                stringBuilder.append(")");
            }
            stringBuilder.append("\r\n");
        }
        stringBuilder.append("\tprivate\t");
        stringBuilder.append(this.type.getSimpleName());
        if (this.firstParameter != null) {
            stringBuilder.append("<");
            stringBuilder.append(this.firstParameter.getSimpleName());
            if (this.secondParameter != null) {
                stringBuilder.append(",");
                stringBuilder.append(this.secondParameter.getSimpleName());
            }
            stringBuilder.append(">");
        }
        stringBuilder.append("\t\t");
        stringBuilder.append(this.name);
        stringBuilder.append("; \r\n");
        return stringBuilder.toString();
    }

    private interface UpdatablePrimaryKey<T extends Updatable<T>, PK> extends Updatable<T>, PrimaryKey<PK> {
    }

}
