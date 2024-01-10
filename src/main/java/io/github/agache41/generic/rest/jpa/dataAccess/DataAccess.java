
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

package io.github.agache41.generic.rest.jpa.dataAccess;


import io.github.agache41.generic.rest.jpa.exceptions.ExpectedException;
import io.github.agache41.generic.rest.jpa.exceptions.UnexpectedException;
import io.github.agache41.generic.rest.jpa.update.ClassReflector;
import io.github.agache41.generic.rest.jpa.update.Update;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.criteria.*;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <pre>
 * Generic superset of data access layer methods over an EntityManager.
 *
 * The class is meant to be used standalone and also as a base class.
 * In standalone use it can be injected directly in the code:
 *
 *
 * </pre>
 *
 * @param <ENTITY> the type parameter
 * @param <PK>     the type parameter
 */
@Dependent
@Named("base")
public class DataAccess<ENTITY extends PrimaryKey<PK>, PK> {
    /**
     * <pre>
     * The type of the persisted Object
     * </pre>
     */
    protected final Class<ENTITY> type;

    /**
     * <pre>
     * The ClassReflector of the persisted Object
     * </pre>
     */
    protected final ClassReflector<ENTITY> classReflector;
    /**
     * <pre>
     * The type of the persisted Object Primary Key
     * </pre>
     */
    protected final Class<PK> keyType;
    /**
     * <pre>
     * The Name of this Dao.
     * </pre>
     */
    protected final String name;

    /**
     * <pre>
     * The default EntityManager in use.
     * </pre>
     */
    @Inject
    protected EntityManager em;

    /**
     * <pre>
     * Injection Point Constructor
     * Example how to inject a DataAccess for a class TypeClass with Primary Key PKey:
     * &#064;Inject DataAccess &#x3C;MyClass, PKey&#x3E; myClassDao;
     * </pre>
     *
     * @param ip the underlining injection point, provided by CDI.
     */
    @Inject
    public DataAccess(InjectionPoint ip) {
        this(((Class<ENTITY>) (((ParameterizedType) ip.getType()).getActualTypeArguments()[0])),//
                ((Class<PK>) (((ParameterizedType) ip.getType()).getActualTypeArguments()[1])));//
    }

    /**
     * <pre>
     * Root constructor.
     * </pre>
     *
     * @param type    the type of the persisting Object
     * @param keyType the type of the persisting Object Primary Key
     */
    public DataAccess(Class<ENTITY> type, Class<PK> keyType) {
        this.type = type;
        this.keyType = keyType;
        this.name = DataAccess.class.getSimpleName() + "<" + this.type.getSimpleName() + "," + this.keyType.getSimpleName() + ">";
        this.classReflector = ClassReflector.ofClass(this.type);
    }

    /**
     * <pre>
     * Finds an entity in the database using the Primary Key
     * </pre>
     *
     * @param id the primary key to use, must be not null
     * @return the entity for the primary key or throws ExpectedException if no entity is found
     */
    public ENTITY findById(PK id) {
        return findById(id, true);
    }

    /**
     * <pre>
     * Finds an entity in the database using the Primary Key.
     * </pre>
     *
     * @param id       the primary key to use, must be not null
     * @param expected if a persisted entity must exist
     * @return the entity for the primary key or null if not found. If no entity is found and expected is set to true ExpectedException is thrown.
     * @see jakarta.persistence.EntityManager#find(Class, Object) jakarta.persistence.EntityManager#find(Class, Object)
     */
    public ENTITY findById(PK id, boolean expected) {
        return assertNotNull(em().find(type, assertNotNull(id)), expected);
    }

    /**
     * <pre>
     * Finds an entity in the database using the Primary Key of the provided source entity.
     * Used as a persisted entity locator for transfer object based interactions.
     * </pre>
     *
     * @param source the object that contains the id.
     * @return the persisted entity, if any or ExpectedException if no entity is found
     */
    public ENTITY findPersisted(PrimaryKey<? extends PK> source) {
        return this.findById(assertNotNull(source.getId()));
    }

    /**
     * <pre>
     * Finds in Database one entity that equals a specific value in a specified column.
     * If no entity is found , an ExpectedException is being thrown.
     * If the provided value is null, an UnexpectedException is being thrown.
     * </pre>
     *
     * @param column the column to filter for
     * @param value  the value to filter for
     * @return the persisted entity, if any or ExpectedException if no entity is found
     */
    public ENTITY findByColumnEqualsValue(String column, Object value) {
        return findByColumnEqualsValue(column, value, true, true);
    }

    /**
     * <pre>
     * Finds in Database one entity that equals a specific value in a specified column.
     * </pre>
     *
     * @param column   the column to value for
     * @param value    the value to value for
     * @param notNull  specifies if the value can be null, and in this case the null can used as value.
     * @param expected specifies if an entity should be returned, or else a ExpectedException will be thrown
     * @return the persisted entity
     */
    public ENTITY findByColumnEqualsValue(String column, Object value, boolean notNull, boolean expected) {
        try {
            CriteriaBuilder criteriaBuilder = em().getCriteriaBuilder();
            CriteriaQuery<ENTITY> query = criteriaBuilder.createQuery(type);
            Root<ENTITY> entity = query.from(type);
            return em().createQuery(query.select(entity)
                                         .where(equals(column, value, notNull, entity, criteriaBuilder)))
                       .getSingleResult();
        } catch (NoResultException exception) {
            return resultAs(exception, expected);
        } catch (NonUniqueResultException exception) {
            throw new ExpectedException(this.name + ": Filtered Entity is not unique.");
        }
    }

    /**
     * <pre>
     * Finds in Database one entity that is "like" a specific value in a specified column, using the like operator.
     * </pre>
     *
     * @param column   the column to value for
     * @param value    the value to value for
     * @param notNull  specifies if the value can be null, and in this case the null can used as value.
     * @param expected specifies if an entity should be returned, or else a ExpectedException will be thrown
     * @return the persisted entity
     */
    public ENTITY findByColumnLikeValue(String column, String value, boolean notNull, boolean expected) {
        try {
            CriteriaBuilder criteriaBuilder = em().getCriteriaBuilder();
            CriteriaQuery<ENTITY> query = criteriaBuilder.createQuery(type);
            Root<ENTITY> entity = query.from(type);
            return em().createQuery(query.select(entity)
                                         .where(like(column, value, notNull, entity, criteriaBuilder)))
                       .getSingleResult();
        } catch (NoResultException exception) {
            return resultAs(exception, expected);
        } catch (NonUniqueResultException exception) {
            throw new ExpectedException(this.name + ": Filtered Entity is not unique.");
        }
    }

    /**
     * <pre>
     * Finds all entities.
     * </pre>
     *
     * @return all the entities in a Stream&#x3C;ENTITY&#x3E;
     */
    public Stream<ENTITY> streamAll() {
        CriteriaQuery<ENTITY> query = em().getCriteriaBuilder()
                                          .createQuery(this.type);
        Root<ENTITY> entity = query.from(type);
        return em().createQuery(query.select(entity))
                   .getResultStream();
    }

    /**
     * <pre>
     * Finds all entities with the Primary Key within the given list of ids.
     * </pre>
     *
     * @param ids the ids
     * @return entities in a Stream&#x3C;ENTITY&#x3E;
     */
    public Stream<ENTITY> streamByIds(Collection<? extends PK> ids) {
        return streamByColumnInValues(PrimaryKey.ID, ids);
    }

    /**
     * <pre>
     * Finds all entities with the Primary Key within the given list of object entities.
     * Used as a persisted entity locator for transfer object based interactions.
     * </pre>
     *
     * @param filter the filter
     * @return entities in a Stream&#x3C;ENTITY&#x3E;
     */
    public Stream<ENTITY> streamPersisted(Collection<? extends PrimaryKey<? extends PK>> filter) {
        return streamByIds(filter.stream()
                                 .map(PrimaryKey::getId)
                                 .collect(Collectors.toList()));
    }

    /**
     * <pre>
     * Finds all entities whose value in a specified column is equal the given value
     * </pre>
     *
     * @param filterColumn the column to value for
     * @param value        the value to value for, must be not null or else exception will be thrown.
     * @return entities in a Stream&#x3C;ENTITY&#x3E;
     */
    public Stream<ENTITY> streamByColumnEqualsValue(String filterColumn, Object value) {
        return streamByColumnEqualsValue(filterColumn, value, true);
    }

    /**
     * <pre>
     * Finds all entities whose value in a specified column is equal the given value.
     * </pre>
     *
     * @param column  the column to value for
     * @param value   the value to value for
     * @param notNull specifies if the value can be null, and in this case the null can be used as a value.
     * @return entities in a Stream&#x3C;ENTITY&#x3E;
     */
    public Stream<ENTITY> streamByColumnEqualsValue(String column, Object value, boolean notNull) {
        CriteriaBuilder criteriaBuilder = em().getCriteriaBuilder();
        CriteriaQuery<ENTITY> query = criteriaBuilder.createQuery(type);
        Root<ENTITY> entity = query.from(type);
        return em().createQuery(query.select(entity)
                                     .where(equals(column, value, notNull, entity, criteriaBuilder)))
                   .getResultStream();

    }

    /**
     * <pre>
     * Finds in Database the entities that equals a given content object.
     * The content object must contain non null values just in the fields that are taking part in the filtering.
     * The other null fields are to be ignored.
     * No nulls can be used in the filtering.
     * Example :
     * content = [name ="abcd", no=2, street=null]
     * result is where name = "abcd" and no = 2
     * </pre>
     *
     * @param value the Object holding the content (the values columns)
     * @return the persisted entity
     */
    public Stream<ENTITY> streamByContentEquals(ENTITY value) {
        CriteriaBuilder criteriaBuilder = em().getCriteriaBuilder();
        CriteriaQuery<ENTITY> query = criteriaBuilder.createQuery(type);
        Root<ENTITY> entity = query.from(type);
        HashMap<String, Object> mapValues = this.classReflector.mapValues(value);
        return em().createQuery(query.select(entity)
                                     .where(equals(mapValues, entity, criteriaBuilder)))
                   .getResultStream();
    }

    /**
     * <pre>
     * Finds all entities whose value in a specified column are like the given value.
     * The SQL Like operator is used.
     * </pre>
     *
     * @param column the column to value for
     * @param value  the value to compare
     * @return entities in a Stream&#x3C;ENTITY&#x3E;
     */
    public Stream<ENTITY> streamByColumnLikeValue(String column, String value) {
        return this.streamByColumnLikeValue(column, value, true);
    }

    /**
     * <pre>
     * Finds all entities whose value in a specified column are like the given value.
     * The SQL Like operator is used.
     * </pre>
     *
     * @param column  the column to value for
     * @param value   the value to compare
     * @param notNull specifies if the value can be null, and in this case the null can be used as a value.
     * @return entities in a Stream&#x3C;ENTITY&#x3E;
     */
    public Stream<ENTITY> streamByColumnLikeValue(String column, String value, boolean notNull) {
        CriteriaBuilder criteriaBuilder = em().getCriteriaBuilder();
        CriteriaQuery<ENTITY> query = criteriaBuilder.createQuery(type);
        Root<ENTITY> entity = query.from(type);
        return em().createQuery(query.select(entity)
                                     .where(like(column, value, notNull, entity, criteriaBuilder)))
                   .getResultStream();
    }

    /**
     * <pre>
     * Finds all entities whose value in a specified column is in the given list of filtered values.
     * </pre>
     *
     * @param column the column to equal values for
     * @param values the list of filtered values
     * @return entities in a Stream&#x3C;ENTITY&#x3E;
     */
    public Stream<ENTITY> streamByColumnInValues(String column, Collection<? extends Object> values) {
        return streamByColumnInValues(column, values, true);
    }

    /**
     * <pre>
     * Finds all entities whose value in a specified column is in the given list of filtered values.
     * </pre>
     *
     * @param column  the column to values for
     * @param values  the List of filtered values
     * @param notNull if list of filtered values can be null : specifies if the values value can be null, and in this case the null is used as values.
     * @return entities in a Stream&#x3C;ENTITY&#x3E;
     */
    public Stream<ENTITY> streamByColumnInValues(String column, Collection<? extends Object> values, boolean notNull) {
        CriteriaQuery<ENTITY> query = em().getCriteriaBuilder()
                                          .createQuery(this.type);
        Root<ENTITY> entity = query.from(type);
        return em().createQuery(query.select(entity)
                                     .where(in(column, values, notNull, entity)))
                   .getResultStream();
    }

    /**
     * <pre>
     * Finds in Database the entities that are in a given content list of given values.
     * The content object must contain non null values just in the fields that are taking part in the filtering.
     * The other null fields are to be ignored.
     * No nulls can be used in the filtering.
     * Example :
     * content = [name =["abcd","bcde","1234"], no=[2,3], street=null]
     * result is where name in ("abcd","bcde","1234") and no in (2,3)
     * </pre>
     *
     * @param values the values
     * @return the persisted entity
     */
    public Stream<ENTITY> streamByContentInValues(List<ENTITY> values) {
        CriteriaBuilder criteriaBuilder = em().getCriteriaBuilder();
        CriteriaQuery<ENTITY> query = criteriaBuilder.createQuery(type);
        Root<ENTITY> entity = query.from(type);
        HashMap<String, List<Object>> mapValues = this.classReflector.mapValues(values);
        return em().createQuery(query.select(entity)
                                     .where(in(mapValues, entity, criteriaBuilder)))
                   .getResultStream();
    }

    /**
     * <pre>
     * Deletes the given entity
     * </pre>
     *
     * @param entity the given entity
     * @see jakarta.persistence.EntityManager#remove(Object) jakarta.persistence.EntityManager#remove(Object)
     */
    public void remove(ENTITY entity) {
        em().remove(assertNotNull(entity));
    }

    /**
     * <pre>
     * Delete one entity using the given Primary Key
     * </pre>
     *
     * @param id the primary key to look for
     */
    public void removeById(PK id) {
        this.removeByColumnEqualsValue(PrimaryKey.ID, id, false);
    }


    /**
     * <pre>
     * Delete more entities using the given Primary Keys
     * </pre>
     *
     * @param ids the primary key to filter for
     */
    public void removeByIds(Collection<PK> ids) {
        this.removeByColumnInValues(PrimaryKey.ID, ids, false);
    }

    /**
     * <pre>
     * Delete more entities that equal a given value in a column.
     * </pre>
     *
     * @param column  the column holding the value
     * @param value   the value to equal
     * @param notNull specifies if the value can be null, and in this case the null is used as value.
     */
    public void removeByColumnEqualsValue(String column, Object value, boolean notNull) {
        CriteriaBuilder criteriaBuilder = em().getCriteriaBuilder();
        CriteriaDelete<ENTITY> delete = criteriaBuilder.createCriteriaDelete(this.type);
        Root<ENTITY> entity = delete.from(type);
        em().createQuery(delete.where(equals(column, value, notNull, entity, criteriaBuilder)))
            .executeUpdate();
    }

    /**
     * <pre>
     * Delete more entities that match a given list of values in a specified column.
     * </pre>
     *
     * @param column  the column holding the value
     * @param values  the list of filtered values
     * @param notNull if list of filtered values can be null :  specifies if the values value can be null, and in this case the null is used as values.
     */
    public void removeByColumnInValues(String column, Collection<? extends Object> values, boolean notNull) {
        CriteriaBuilder criteriaBuilder = em().getCriteriaBuilder();
        CriteriaDelete<ENTITY> delete = criteriaBuilder.createCriteriaDelete(this.type);
        Root<ENTITY> entity = delete.from(type);
        em().createQuery(delete.where(in(column, values, notNull, entity)))
            .executeUpdate();
    }

    /**
     * <pre>
     * Delete all entities persisted in the associated Table.
     * </pre>
     */
    public void removeAll() {
        CriteriaDelete<ENTITY> delete = em().getCriteriaBuilder()
                                            .createCriteriaDelete(this.type);
        em().createQuery(delete)
            .executeUpdate();
    }

    /**
     * <pre>
     * Updates an entity.
     * The code locates the corresponding persisted entity based on the provided primary key.
     * The Entity with the given id must exist in the Database or a UnexpectedException is thrown.
     * The persisted entity is then updated from the source entity using only the fields marked with @ {@link Update } annotation
     * </pre>
     *
     * @param source the object that contains the id and is the source for update
     * @return the persisted entity.
     */
    public ENTITY updateById(ENTITY source) {
        return findPersisted(source).update(source);
    }

    /**
     * <pre>
     * Updates multiple entities.
     * The code locates the corresponding persisted entities based on the provided primary keys.
     * All the Entities with the given id must exist in the Database or a UnexpectedException is thrown.
     * The persisted entities are then updated from the source entities using only the fields marked with @ {@link Update } annotation
     * </pre>
     *
     * @param sources the Collection of objects that contains the ids and is the source for update
     * @return the persisted entities in a Stream&#x3C;ENTITY&#x3E;
     */
    public Stream<ENTITY> updateByIds(Collection<ENTITY> sources) {
        return updateByIds(sources, true);
    }

    /**
     * <pre>
     * Updates multiple entities.
     * The code locates the corresponding persisted entities based on the provided primary keys.
     * The persisted entities are then updated from the source entities using only the fields marked with @ {@link Update } annotation
     * </pre>
     *
     * @param sources     the Collection of objects that contains the ids and is the source for update
     * @param allExpected is set to true, all the Entities with the given id must exist in the Database or a UnexpectedException is thrown.
     * @return the persisted entities in a Stream&#x3C;ENTITY&#x3E;
     */
    public Stream<ENTITY> updateByIds(Collection<ENTITY> sources, boolean allExpected) {
        final Map<PK, ENTITY> persistedMap = asMap(streamPersisted(sources));
        return sources.stream()
                      .map(source -> {
                          PK id = source.getId();
                          if (persistedMap.containsKey(id)) return persistedMap.get(id)
                                                                               .update(source);
                          else if (allExpected)
                              throw new UnexpectedException(this.name + ": Missing Entity in Update for PK=" + id.toString());
                          else return source;
                      });
    }

    /**
     * <pre>
     * Merges the entity
     * </pre>
     *
     * @param entity the entity
     * @return the merged entity
     * @see jakarta.persistence.EntityManager#merge(Object) jakarta.persistence.EntityManager#merge(Object)
     */
    public ENTITY merge(ENTITY entity) {
        return em().merge(assertNotNull(entity));
    }

    /**
     * <pre>
     * Merges all the entities in the list, returning the results in a stream.
     * </pre>
     *
     * @param sources the sources
     * @return the merged entities in a Stream&#x3C;ENTITY&#x3E;
     * @see jakarta.persistence.EntityManager#merge(Object) jakarta.persistence.EntityManager#merge(Object)
     */
    public Stream<ENTITY> mergeAll(Collection<ENTITY> sources) {
        return sources.stream()
                      .map(this::merge);
    }

    /**
     * <pre>
     * Persists an entity
     * </pre>
     *
     * @param source the source
     * @return the persisted entity
     * @see jakarta.persistence.EntityManager#persist(Object) jakarta.persistence.EntityManager#persist(Object)
     */
    public ENTITY persist(ENTITY source) {
        em().persist(assertNotNull(source));
        return source;
    }

    /**
     * <pre>
     * Persists all the entities in the list, returning the results in a stream.
     * </pre>
     *
     * @param sources the sources
     * @return the merged entities in a Stream&#x3C;ENTITY&#x3E;
     * @see jakarta.persistence.EntityManager#persist(Object) jakarta.persistence.EntityManager#persist(Object)
     */
    public Stream<ENTITY> persistAll(Collection<ENTITY> sources) {
        return sources.stream()
                      .map(this::persist);
    }


    /**
     * <pre>
     * Persists or updates an entity using the update mechanism of the annotation @ {@link Update },
     * depending on the entity state, if it is already persisted or not.
     * </pre>
     *
     * @param source the source
     * @return entity entity
     */
    public ENTITY put(ENTITY source) {
        if (null == source.getId()) {
            return this.persist(source);
        } else {
            return this.updateById(source);
        }
    }

    /**
     * <pre>
     * Persists or updates a list of entities
     * using the update mechanism of the annotation @ {@link Update},
     * depending on the entity state, if it is already persisted or not.
     * </pre>
     *
     * @param sources the sources
     * @return stream stream
     */
    public Stream<ENTITY> putAll(Collection<ENTITY> sources) {
        return sources.stream()
                      .map(this::put);

    }

    /**
     * <pre>
     * Builder for the equals expression.
     * </pre>
     *
     * @param column          the column to filter for
     * @param value           the value to filter for
     * @param notNull         if the value van be null
     * @param entity          the entity root
     * @param criteriaBuilder the criteria builder
     * @return the criteria builder expression
     */
    protected Expression<Boolean> equals(String column, Object value, boolean notNull, Root<ENTITY> entity, CriteriaBuilder criteriaBuilder) {
        column = columnFrom(column);
        if (applyFilter(value, notNull)) {
            return criteriaBuilder.equal(entity.get(column), value);
        } else {
            return entity.get(column)
                         .isNull();
        }
    }

    /**
     * <pre>
     * Builder for the equals expression extracting the matching values from a given map.
     * Example :
     * map = name ="abc", no =2;
     * result is where name = "abc" and no=2
     * </pre>
     *
     * @param values          the values in a hash map
     * @param entity          the entity root
     * @param criteriaBuilder the criteria builder
     * @return the criteria builder expression
     */
    protected Expression<Boolean> equals(HashMap<String, Object> values, Root<ENTITY> entity, CriteriaBuilder criteriaBuilder) {
        return values.entrySet()
                     .stream()
                     .map(entry -> criteriaBuilder.equal(entity.get(entry.getKey()), entry.getValue()))
                     .collect(Collectors.reducing(criteriaBuilder::and))
                     .orElseThrow(() -> new IllegalArgumentException(" Bad Content " + values));
    }

    /**
     * <pre>
     * Builder for the like expression.
     * </pre>
     *
     * @param column          the column to filter for
     * @param value           the value to filter for
     * @param notNull         if the value van be null
     * @param entity          the entity root
     * @param criteriaBuilder the criteria builder
     * @return the criteria builder expression
     */
    protected Expression<Boolean> like(String column, String value, boolean notNull, Root<ENTITY> entity, CriteriaBuilder criteriaBuilder) {
        column = columnFrom(column);
        if (applyFilter(value, notNull)) {
            return criteriaBuilder.like(entity.get(column), value);
        } else {
            return entity.get(column)
                         .isNull();
        }
    }

    /**
     * <pre>
     * Builder for the in expression
     * </pre>
     *
     * @param column  the column to filter for
     * @param values  the values to filter for
     * @param notNull if the value van be null
     * @param entity  the entity root
     * @return the criteria builder expression
     */
    protected Expression<Boolean> in(String column, Collection<? extends Object> values, boolean notNull, Root<ENTITY> entity) {
        column = columnFrom(column);
        if (applyFilter(values, notNull)) {
            return entity.get(column)
                         .in(values);
        } else {
            return entity.get(column)
                         .isNull();
        }
    }


    /**
     * <pre>
     * Builder for the in expression extracting the matching values from a given map.
     * Example :
     *     map = name =["abc","bcd","123"], no =2;
     *     result is
     *      where name in ("abc","bcd","123") and no in (2)
     * </pre>
     *
     * @param values          the map of values to filter for
     * @param entity          the entity root
     * @param criteriaBuilder the criteria builder
     * @return the criteria builder expression
     */
    protected Expression<Boolean> in(Map<String, List<Object>> values, Root<ENTITY> entity, CriteriaBuilder criteriaBuilder) {
        return values.entrySet()
                     .stream()
                     .map(entry -> entity.get(entry.getKey())
                                         .in(entry.getValue()))
                     .collect(Collectors.reducing(criteriaBuilder::and))
                     .orElseThrow(() -> new IllegalArgumentException(" Bad Content " + values));
    }

    /**
     * <pre>
     * Asserts that argument it not null.
     * </pre>
     *
     * @param <R>    the type parameter
     * @param source the source
     * @return the input if not null. If null is found, an UnexpectedException is thrown.
     */
    protected <R> R assertNotNull(R source) {
        if (null == source) {
            throw new UnexpectedException(this.name + ": not null expected");
        }
        return source;
    }

    /**
     * <pre>
     * Asserts that argument it not null.
     * </pre>
     *
     * @param source   the source
     * @param expected if null is expected
     * @return the input if not null. If null is found and expected is true an ExpectedException is thrown.
     */
    protected ENTITY assertNotNull(ENTITY source, boolean expected) {
        if (expected && null == source) {
            throw new ExpectedException(this.name + ": Entity was not found");
        }
        return source;
    }

    /**
     * <pre>
     * Interprets the NoResultException parameter based on the notNull parameter.
     * </pre>
     *
     * @param <T>       the type parameter
     * @param exception the given exception
     * @param expected  if exception was expected or not and should be rethrown
     * @return the t
     */
    protected <T> T resultAs(NoResultException exception, boolean expected) {
        if (expected) {
            throw new ExpectedException(this.name + ": Entity was not found", exception);
        }
        return null;
    }

    /**
     * <pre>
     * Checks an input String if it can be used as a column name.
     * </pre>
     *
     * @param column the column
     * @return the string
     */
    protected String columnFrom(String column) {
        if (column == null || column.isEmpty()) {
            throw new UnexpectedException(this.name + ":Expected a table column name.");
        }
        return column;
    }

    /**
     * <pre>
     * Applies a boolean filter.
     * </pre>
     *
     * @param <R>     the type parameter
     * @param value   the value
     * @param notNull the not null
     * @return the boolean
     */
    protected <R> boolean applyFilter(R value, boolean notNull) {
        if (null == value) {
            if (notNull) {
                throw new UnexpectedException(this.name + ": Expecting not null value.");
            }
            return false;
        }
        return !(value instanceof Collection<?>) || !((Collection<?>) value).isEmpty();
    }

    /**
     * <pre>
     * Returns the current in use Entity Manager
     * Derived classes muss override this method to use another persistence unit.
     * </pre>
     *
     * @return the current Entity Manager
     */
    public EntityManager em() {
        return em;
    }

    /**
     * <pre>
     * Type of the persisted Object
     * </pre>
     *
     * @return the Type of the persisted Object
     */
    public Class<ENTITY> getType() {
        return type;
    }

    /**
     * <pre>
     * Type of the persisted Object Primary Key
     * </pre>
     *
     * @return the Type of the persisted Object Primary Key
     */
    public Class<PK> getKeyType() {
        return keyType;
    }

    /**
     * <pre>
     * Name of this DataAccess
     * </pre>
     *
     * @return the Name of this DataAccess
     */
    public String getName() {
        return name;
    }

    /**
     * <pre>
     * Collects the input stream of entities in a Map with keys from the primary key.
     * </pre>
     *
     * @param sources the sources
     * @return the map
     */
    public Map<PK, ENTITY> asMap(Stream<ENTITY> sources) {
        return sources.collect(Collectors.toMap(PrimaryKey::getId, Function.identity()));
    }

    /**
     * <pre>
     * Collects the input stream of entities in a list.
     * </pre>
     *
     * @param sources the sources
     * @return the list
     */
    public List<ENTITY> asList(Stream<ENTITY> sources) {
        return sources.collect(Collectors.toList());
    }
}
