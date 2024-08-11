
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
import io.github.agache41.generic.rest.jpa.update.TransferObject;
import io.github.agache41.generic.rest.jpa.update.Updatable;
import io.github.agache41.generic.rest.jpa.update.Update;
import io.github.agache41.generic.rest.jpa.update.updater.*;
import io.github.agache41.generic.rest.jpa.utils.ReflectionUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.jboss.logging.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.agache41.generic.rest.jpa.update.Update.defaultOrder;

/**
 * <pre>
 * The type Fieldreflector.
 * The class processes a single field and builds the necessary structure for the update pattern.
 * </pre>
 *
 * @param <T>  the type parameter
 * @param <TV> the type parameter
 */
public final class FieldReflector<T, S, TV, SV> {

    private static final Logger log = Logger.getLogger(ClassReflector.class);
    private final String name;
    private final String associatedName;
    private final Class<T> enclosingClass;
    private final Class<S> associatedClass;
    private final Class<TV> type;
    private final Class<SV> associatedType;
    private final Class<?> firstParameter;
    private final Class<?> secondParameter;
    private final Function<T, TV> getter;
    private final BiConsumer<T, TV> setter;
    private final Function<S, SV> associatedGetter;
    private final BiConsumer<S, SV> associatedSetter;
    private final Field field;
    private final boolean dynamic;
    private final boolean updatable;
    private final boolean isFinal;
    private final boolean isTransient;
    private final boolean isHibernateIntern;
    private final boolean isEager;
    private final boolean valid;
    private final boolean activ;
    private final Column columnAnnotation;
    private final Update updateAnnotation;
    private final String description;
    private final int length;
    private final boolean id;
    private final boolean nullable;
    private final boolean insertable;
    private final int order;
    private Updater<T, S> renderer;
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
                   final Class<S> associatedClass,
                   final Field field) {
        this.enclosingClass = enclosingClass;
        this.associatedClass = associatedClass;
        this.field = field;
        this.name = field.getName();
        this.type = (Class<TV>) field.getType();
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
        this.id = field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(EmbeddedId.class);
        this.order = this.order(this.field);
        if (field.isAnnotationPresent(OneToMany.class)) {
            this.isEager = field.getAnnotation(OneToMany.class)
                                .fetch()
                                .equals(FetchType.EAGER);
        } else {
            this.isEager = false;
        }

        if (this.valid && !this.isFinal && !this.isHibernateIntern && this.updateAnnotation != null) {
            this.activ = true;
            this.dynamic = this.updateAnnotation.dynamic();
            this.updatable = this.updateAnnotation.updatable() && (this.columnAnnotation == null || this.columnAnnotation.updatable());
            this.nullable = this.updateAnnotation.nullable() && (this.columnAnnotation == null || this.columnAnnotation.nullable()) && !field.isAnnotationPresent(NotNull.class);
            this.insertable = this.updateAnnotation.insertable() && (this.columnAnnotation == null || this.columnAnnotation.insertable());
            this.associatedName = "".equals(this.updateAnnotation.name()) ? this.name : this.updateAnnotation.name();
            this.associatedType = (Class<SV>) ReflectionUtils.getFieldType(this.associatedClass, this.associatedName);
            this.associatedGetter = ReflectionUtils.getGetter(this.associatedClass, this.associatedName, this.associatedType);
            this.associatedSetter = ReflectionUtils.getSetter(this.associatedClass, this.associatedName, this.associatedType);
            //todo: check null and throw
        } else {
            // no update needed.
            this.activ = false;
            this.dynamic = false;
            this.updatable = false;
            this.nullable = false;
            this.insertable = false;
            this.associatedName = null;
            this.associatedType = null;
            this.associatedGetter = null;
            this.associatedSetter = null;
            this.renderer = null;
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
                   final Class<S> associatedClass,
                   final Method method) {
        this.enclosingClass = enclosingClass;
        this.associatedClass = associatedClass;
        this.field = null;
        this.name = ReflectionUtils.getSetterFieldName(method);
        this.type = method.getParameterTypes().length == 1 ? (Class<TV>) method.getParameterTypes()[0] : null;
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
        this.order = this.order(method);
        this.id = false;
        this.isEager = false;

        if (this.valid && !this.isFinal && !this.isHibernateIntern && this.updateAnnotation != null) {
            this.activ = true;
            this.dynamic = this.updateAnnotation.dynamic();
            this.updatable = this.updateAnnotation.updatable();
            this.insertable = this.updateAnnotation.insertable();
            this.nullable = this.updateAnnotation.nullable();
            this.associatedName = "".equals(this.updateAnnotation.name()) ? this.name : this.updateAnnotation.name();
            this.associatedType = (Class<SV>) ReflectionUtils.getFieldType(this.associatedClass, this.associatedName);
            this.associatedGetter = ReflectionUtils.getGetter(this.associatedClass, this.associatedName, this.associatedType);
            this.associatedSetter = ReflectionUtils.getSetter(this.associatedClass, this.associatedName, this.associatedType);
            //todo: check null and throw
        } else {
            // no update needed.
            this.activ = false;
            this.dynamic = false;
            this.updatable = false;
            this.nullable = false;
            this.insertable = false;
            this.associatedName = null;
            this.associatedType = null;
            this.associatedGetter = null;
            this.associatedSetter = null;
            this.renderer = null;
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
                this.renderer = new EntityCollectionUpdater<>((BiConsumer<T, Collection<UpdatablePrimaryKey>>) this.setter, (Function<T, Collection<UpdatablePrimaryKey>>) this.getter, this.dynamic, (Function<S, Collection<UpdatablePrimaryKey>>) this.associatedGetter, (Supplier<UpdatablePrimaryKey>) ReflectionUtils.supplierOf(this.firstParameter));
            } else {
                //collection of simple objects
                this.renderer = new CollectionUpdater<>((BiConsumer<T, Collection<Object>>) this.setter, (Function<T, Collection<Object>>) this.getter, this.dynamic, (Function<S, Collection<Object>>) this.associatedGetter);
            }
        } else if (ReflectionUtils.isClassMap(this.type) && this.firstParameter != null && this.secondParameter != null) {
            this.value = false;
            this.map = true;
            this.collection = false;
            if (Updatable.class.isAssignableFrom(this.secondParameter)) {
                //map of entities
                this.renderer = new EntityMapUpdater<>((BiConsumer<T, Map<Object, Updatable>>) this.setter, (Function<T, Map<Object, Updatable>>) this.getter, this.dynamic, (Function<S, Map<Object, Updatable>>) this.associatedGetter, (Supplier<Updatable>) ReflectionUtils.supplierOf(this.secondParameter));
            } else {
                //map of simple objects
                this.renderer = new MapUpdater<>((BiConsumer<T, Map<Object, Object>>) this.setter, (Function<T, Map<Object, Object>>) this.getter, this.dynamic, (Function<S, Map<Object, Object>>) this.associatedGetter);
            }
        } else if (TransferObject.class.isAssignableFrom(this.type)) {
            this.value = false;
            this.map = false;
            this.collection = false;
            // entity
            this.renderer = new EntityUpdater<>(this.getter, this.setter, (Supplier<Updatable>) ReflectionUtils.supplierOf(this.type), this.dynamic, this.associatedGetter, this.associatedSetter, (Supplier<Updatable>) ReflectionUtils.supplierOf(this.type));
        } else {
            // simple value
            this.value = true;
            this.map = false;
            this.collection = false;
            this.renderer = new ValueUpdater<>(this.getter, this.setter, this.dynamic, this.associatedGetter, this.associatedSetter);
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
                          final S source) {
        return this.renderer.update(destination, source);
    }

    /**
     * <pre>
     * Object Getter.
     * </pre>
     *
     * @param source the source
     * @return the object
     */
    public TV get(final T source) {
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
                    final TV value) {
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
    public Function<T, TV> getGetter() {
        return this.getter;
    }

    /**
     * Gets setter.
     *
     * @return the setter
     */
    public BiConsumer<T, TV> getSetter() {
        return this.setter;
    }

    /**
     * Is not null boolean.
     *
     * @return the boolean
     */
    public boolean isDynamic() {
        return this.dynamic;
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
    public Updater<T, S> getRenderer() {
        return this.renderer;
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
     * Is activ boolean.
     *
     * @return the boolean
     */
    public boolean isActiv() {
        return this.activ;
    }


    /**
     * Is nullable boolean.
     *
     * @return the boolean
     */
    public boolean isNullable() {
        return this.nullable;
    }

    /**
     * Is insertable boolean.
     *
     * @return the boolean
     */
    public boolean isInsertable() {
        return this.insertable;
    }


    /**
     * Gets column annotation.
     *
     * @return the column annotation
     */
    public Column getColumnAnnotation() {
        return this.columnAnnotation;
    }

    /**
     * Gets update annotation.
     *
     * @return the update annotation
     */
    public Update getUpdateAnnotation() {
        return this.updateAnnotation;
    }

    /**
     * Gets length.
     *
     * @return the length
     */
    public int getLength() {
        return this.length;
    }

    /**
     * Is id boolean.
     *
     * @return the boolean
     */
    public boolean isId() {
        return this.id;
    }

    /**
     * Gets order value.
     *
     * @return the order
     */
    public int getOrder() {
        return this.order;
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
        if (this.id) {
            stringBuilder.append("\t@Id\r\n");
        }
        if (this.valid) {
            if (this.activ) {
                stringBuilder.append("\t@Update");
                boolean before = false;
                if (this.length != Update.defaultLength || !this.dynamic || this.order != defaultOrder || !this.updatable || !this.nullable || !this.insertable) {
                    stringBuilder.append("(");
                }
                if (this.order != defaultOrder) {
                    if (before) {
                        stringBuilder.append(", ");
                    } else {
                        before = true;
                    }
                    stringBuilder.append("order = ");
                    stringBuilder.append(this.order);
                }
                if (this.length != Update.defaultLength) {
                    if (before) {
                        stringBuilder.append(", ");
                    } else {
                        before = true;
                    }
                    stringBuilder.append("length = ");
                    stringBuilder.append(this.length);
                }
                if (!this.nullable) {
                    if (before) {
                        stringBuilder.append(", ");
                    } else {
                        before = true;
                    }
                    stringBuilder.append("nullable = false");
                }
                if (!this.updatable) {
                    if (before) {
                        stringBuilder.append(", ");
                    } else {
                        before = true;
                    }
                    stringBuilder.append("updatable = false");
                }
                if (!this.insertable) {
                    if (before) {
                        stringBuilder.append(", ");
                    } else {
                        before = true;
                    }
                    stringBuilder.append("insertable = false");
                }
                if (!this.dynamic) {
                    if (before) {
                        stringBuilder.append(", ");
                    } else {
                        before = true;
                    }
                    stringBuilder.append("dynamic = false");
                }
                if (this.length != -1 || !this.dynamic || this.order != defaultOrder) {
                    stringBuilder.append(")");
                }
                stringBuilder.append("\r\n");
            } else {
                stringBuilder.append("\t@Update.excluded\r\n");
            }
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

    private int index(final String[] order) {
        for (int index = 0; index < order.length; index++) {
            if (Objects.equals(this.name, order[index])) {
                return index;
            }
        }
        return defaultOrder;
    }

    private int order(final Field field) {
        int result = defaultOrder;
        if (this.enclosingClass.isAnnotationPresent(Update.class)) {
            final int index = this.index(this.enclosingClass.getAnnotation(Update.class)
                                                            .propertiesOrder());
            if (defaultOrder != index) {
                result = index;
            }
        }
        if (field.isAnnotationPresent(Update.class) && field.getAnnotation(Update.class)
                                                            .order() != defaultOrder) {
            if (defaultOrder != field.getAnnotation(Update.class)
                                     .order()) {
                result = field.getAnnotation(Update.class)
                              .order();
            }
        }
        return result;
    }

    private int order(final Method method) {
        int result = defaultOrder;
        if (this.enclosingClass.isAnnotationPresent(Update.class)) {
            final int index = this.index(this.enclosingClass.getAnnotation(Update.class)
                                                            .propertiesOrder());
            if (index != defaultOrder) {
                result = index;
            }
        }
        if (method.isAnnotationPresent(Update.class) && method.getAnnotation(Update.class)
                                                              .order() != defaultOrder) {
            if (defaultOrder != method.getAnnotation(Update.class)
                                      .order()) {
                result = method.getAnnotation(Update.class)
                               .order();
            }
        }
        return result;
    }

    private interface UpdatablePrimaryKey<T extends Updatable<T>, PK> extends Updatable<T>, PrimaryKey<PK> {
    }

}
