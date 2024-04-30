
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
import io.github.agache41.generic.rest.jpa.update.Update;
import io.github.agache41.generic.rest.jpa.update.Updateable;
import io.github.agache41.generic.rest.jpa.update.reflector.ClassReflector;
import io.github.agache41.generic.rest.jpa.update.reflector.FieldReflector;
import io.github.agache41.generic.rest.jpa.utils.ReflectionUtils;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
public class DataAccess<ENTITY extends PrimaryKey<PK> & Updateable<ENTITY>, PK> {

    /**
     * <pre>
     * The type of the persisted Object
     * </pre>
     */
    protected final Class<ENTITY> type;
    /**
     * <pre>
     * The no arguments constructor associated for the type.
     * </pre>
     */
    protected final Constructor<ENTITY> noArgsConstructor;
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
     * List with fields that are to be eager fetched.
     * </pre>
     */
    protected final List<String> eagerFields;
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
    public DataAccess(final InjectionPoint ip) {
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
    public DataAccess(final Class<ENTITY> type,
                      final Class<PK> keyType) {
        this.type = type;
        this.noArgsConstructor = ReflectionUtils.getNoArgsConstructor(type);
        this.keyType = keyType;
        this.name = DataAccess.class.getSimpleName() + "<" + this.type.getSimpleName() + "," + this.keyType.getSimpleName() + ">";
        this.classReflector = ClassReflector.ofClass(this.type);
        this.eagerFields = this.classReflector.getReflectors()
                                              .values()
                                              .stream()
                                              .filter(FieldReflector::isEager)
                                              .map(FieldReflector::getName)
                                              .collect(Collectors.toList());
    }

    /**
     * <pre>
     * Finds an entity in the database using the Primary Key
     * </pre>
     *
     * @param id the primary key to use, must be not null
     * @return the entity for the primary key or throws ExpectedException if no entity is found
     */
    public ENTITY findById(final PK id) {
        return this.findById(id, true);
    }

    /**
     * <pre>
     * Finds an entity in the database using the Primary Key.
     * </pre>
     *
     * @param id       the primary key to use, must be not null
     * @param expected if a persisted entity must exist
     * @return the entity for the primary key or null if not found. If no entity is found and expected is set to true ExpectedException is thrown.
     * @see jakarta.persistence.EntityManager#find(Class, Object) jakarta.persistence.EntityManager#find(Class, Object)jakarta.persistence.EntityManager#find(Class, Object)
     */
    public ENTITY findById(final PK id,
                           final boolean expected) {
        return this.assertNotNull(this.em()
                                      .find(this.type, this.assertNotNull(id)), expected);
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
    public ENTITY findPersisted(final PrimaryKey<PK> source) {
        return this.findById(this.assertNotNull(source.getId()));
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
    public ENTITY findByColumnEqualsValue(final String column,
                                          final Object value) {
        return this.findByColumnEqualsValue(column, value, true, true);
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
    public ENTITY findByColumnEqualsValue(final String column,
                                          final Object value,
                                          final boolean notNull,
                                          final boolean expected) {
        try {
            final CriteriaQuery<ENTITY> query = this.query();
            final Root<ENTITY> entity = this.entity(query);
            return this.em()
                       .createQuery(query.select(entity)
                                         .where(this.equals(column, value, notNull, entity)))
                       .getSingleResult();
        } catch (final NoResultException exception) {
            return this.resultAs(exception, expected);
        } catch (final NonUniqueResultException exception) {
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
    public ENTITY findByColumnLikeValue(final String column,
                                        final String value,
                                        final boolean notNull,
                                        final boolean expected) {
        try {
            final CriteriaQuery<ENTITY> query = this.query();
            final Root<ENTITY> entity = this.entity(query);
            return this.em()
                       .createQuery(query.select(entity)
                                         .where(this.like(column, value, notNull, entity)))
                       .getSingleResult();
        } catch (final NoResultException exception) {
            return this.resultAs(exception, expected);
        } catch (final NonUniqueResultException exception) {
            throw new ExpectedException(this.name + ": Filtered Entity is not unique.");
        }
    }

    /**
     * <pre>
     * Finds all entities.
     * </pre>
     *
     * @param firstResult the first result
     * @param maxResults  the max results
     * @return all the entities in a Stream&#x3C;ENTITY&#x3E;
     */
    public Stream<ENTITY> streamAll(final int firstResult,
                                    final int maxResults) {
        final CriteriaQuery<ENTITY> query = this.query();
        final Root<ENTITY> entity = this.entity(query);
        return this.em()
                   .createQuery(query.select(entity))
                   .setFirstResult(firstResult)
                   .setMaxResults(maxResults)
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
    public Stream<ENTITY> streamByIds(final Collection<? extends PK> ids) {
        return this.streamByColumnInValues(PrimaryKey.ID, ids, 0, ids.size());
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
    public Stream<ENTITY> streamPersisted(final Collection<? extends PrimaryKey<PK>> filter) {
        return this.streamByIds(filter.stream()
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
     * @param firstResult  the first result
     * @param maxResults   the max results
     * @return entities in a Stream&#x3C;ENTITY&#x3E;
     */
    public Stream<ENTITY> streamByColumnEqualsValue(final String filterColumn,
                                                    final Object value,
                                                    final int firstResult,
                                                    final int maxResults) {
        return this.streamByColumnEqualsValue(filterColumn, value, firstResult, maxResults, true);
    }

    /**
     * <pre>
     * Finds all entities whose value in a specified column is equal the given value.
     * </pre>
     *
     * @param column      the column to value for
     * @param value       the value to value for
     * @param firstResult the first result
     * @param maxResults  the max results
     * @param notNull     specifies if the value can be null, and in this case the null can be used as a value.
     * @return entities in a Stream&#x3C;ENTITY&#x3E;
     */
    public Stream<ENTITY> streamByColumnEqualsValue(final String column,
                                                    final Object value,
                                                    final int firstResult,
                                                    final int maxResults,
                                                    final boolean notNull) {
        final CriteriaQuery<ENTITY> query = this.query();
        final Root<ENTITY> entity = this.entity(query);
        return this.em()
                   .createQuery(query.select(entity)
                                     .where(this.equals(column, value, notNull, entity)))
                   .setFirstResult(firstResult)
                   .setMaxResults(maxResults)
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
     * @param value       the Object holding the content (the values columns)
     * @param firstResult the first result
     * @param maxResults  the max results
     * @return the persisted entity
     */
    public Stream<ENTITY> streamByContentEquals(final ENTITY value,
                                                final int firstResult,
                                                final int maxResults) {
        final CriteriaQuery<ENTITY> query = this.query();
        final Root<ENTITY> entity = this.entity(query);
        final HashMap<String, Object> mapValues = this.classReflector.mapValues(value);
        return this.em()
                   .createQuery(query.select(entity)
                                     .where(this.equals(mapValues, entity)))
                   .setFirstResult(firstResult)
                   .setMaxResults(maxResults)
                   .getResultStream();
    }

    /**
     * <pre>
     * Finds all entities whose value in a specified column are like the given value.
     * The SQL Like operator is used.
     * </pre>
     *
     * @param column      the column to value for
     * @param value       the value to compare
     * @param firstResult the first result
     * @param maxResults  the max results
     * @return entities in a Stream&#x3C;ENTITY&#x3E;
     */
    public Stream<ENTITY> streamByColumnLikeValue(final String column,
                                                  final String value,
                                                  final int firstResult,
                                                  final int maxResults) {
        return this.streamByColumnLikeValue(column, value, firstResult, maxResults, true);
    }

    /**
     * <pre>
     * Finds all entities whose value in a specified column are like the given value.
     * The SQL Like operator is used.
     * </pre>
     *
     * @param column     the column to value for
     * @param value      the value to compare
     * @param maxResults the max results
     * @return entities in a Stream&#x3C;ENTITY&#x3E;
     */
    public Stream<String> autocompleteByColumnLikeValue(final String column,
                                                        final String value,
                                                        final int maxResults) {

        final CriteriaQuery<String> query = this.cb()
                                                .createQuery(String.class);
        final Root<ENTITY> entity = query.from(this.type);
        return this.em()
                   .createQuery(query.select(entity.get(column))
                                     .distinct(true)
                                     .where(this.like(column, value, true, entity))
                                     .orderBy(this.cb()
                                                  .asc(entity.get(column))))
                   .setMaxResults(maxResults)
                   .getResultStream();
    }

    public Stream<IdGroup<PK>> autocompleteIdsByColumnLikeValue(final String column,
                                                                final String value,
                                                                final int maxResults) {

        final CriteriaQuery query = this.cb()
                                        .createQuery();
        final Root<ENTITY> entity = query.from(this.type);
        final CriteriaQuery multiselect = query.multiselect(this.cb()
                                                                .max(entity.get(PrimaryKey.ID)), entity.get(column), this.cb()
                                                                                                                         .count(entity));
        return this.em()
                   .createQuery(multiselect
                                        .where(this.like(column, value, true, entity))
                                        .groupBy(entity.get(column))
                                        .orderBy(this.cb()
                                                     .asc(entity.get(column))))
                   .setMaxResults(maxResults)
                   .getResultStream()
                   .map(IdGroup<PK>::new);
    }

    /**
     * <pre>
     * Finds all entities whose value in a specified column are like the given value.
     * The SQL Like operator is used.
     * </pre>
     *
     * @param column      the column to value for
     * @param value       the value to compare
     * @param firstResult the first result
     * @param maxResults  the max results
     * @param notNull     specifies if the value can be null, and in this case the null can be used as a value.
     * @return entities in a Stream&#x3C;ENTITY&#x3E;
     */
    public Stream<ENTITY> streamByColumnLikeValue(final String column,
                                                  final String value,
                                                  final int firstResult,
                                                  final int maxResults,
                                                  final boolean notNull) {
        final CriteriaQuery<ENTITY> query = this.query();
        final Root<ENTITY> entity = this.entity(query);
        return this.em()
                   .createQuery(query.select(entity)
                                     .where(this.like(column, value, notNull, entity)))
                   .setFirstResult(firstResult)
                   .setMaxResults(maxResults)
                   .getResultStream();
    }

    /**
     * <pre>
     * Finds all entities whose value in a specified column is in the given list of filtered values.
     * </pre>
     *
     * @param column      the column to equal values for
     * @param values      the list of filtered values
     * @param firstResult the first result
     * @param maxResults  the max results
     * @return entities in a Stream&#x3C;ENTITY&#x3E;
     */
    public Stream<ENTITY> streamByColumnInValues(final String column,
                                                 final Collection<? extends Object> values,
                                                 final int firstResult,
                                                 final int maxResults) {
        return this.streamByColumnInValues(column, values, firstResult, maxResults, true);
    }

    /**
     * <pre>
     * Finds all entities whose value in a specified column is in the given list of filtered values.
     * </pre>
     *
     * @param column      the column to values for
     * @param values      the List of filtered values
     * @param firstResult the first result
     * @param maxResults  the max results
     * @param notNull     if list of filtered values can be null : specifies if the values value can be null, and in this case the null is used as values.
     * @return entities in a Stream&#x3C;ENTITY&#x3E;
     */
    public Stream<ENTITY> streamByColumnInValues(final String column,
                                                 final Collection<? extends Object> values,
                                                 final int firstResult,
                                                 final int maxResults,
                                                 final boolean notNull) {
        final CriteriaQuery<ENTITY> query = this.query();
        final Root<ENTITY> entity = this.entity(query);
        return this.em()
                   .createQuery(query.select(entity)
                                     .where(this.in(column, values, notNull, entity)))
                   .setFirstResult(firstResult)
                   .setMaxResults(maxResults)
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
     * @param values      the values
     * @param firstResult the first result
     * @param maxResults  the max results
     * @return the persisted entity
     */
    public Stream<ENTITY> streamByContentInValues(final List<ENTITY> values,
                                                  final int firstResult,
                                                  final int maxResults) {
        final CriteriaQuery<ENTITY> query = this.query();
        final Root<ENTITY> entity = this.entity(query);
        final HashMap<String, List<Object>> mapValues = this.classReflector.mapValues(values);
        return this.em()
                   .createQuery(query.select(entity)
                                     .where(this.in(mapValues, entity)))
                   .setFirstResult(firstResult)
                   .setMaxResults(maxResults)
                   .getResultStream();
    }

    /**
     * <pre>
     * Deletes the given entity
     * </pre>
     *
     * @param entity the given entity
     * @see jakarta.persistence.EntityManager#remove(Object) jakarta.persistence.EntityManager#remove(Object)jakarta.persistence.EntityManager#remove(Object)
     */
    public void remove(final ENTITY entity) {
        this.em()
            .remove(this.assertNotNull(entity));
    }

    /**
     * <pre>
     * Delete one entity using the given Primary Key
     * </pre>
     *
     * @param id the primary key to look for
     */
    public void removeById(final PK id) {
        this.removeByColumnEqualsValue(PrimaryKey.ID, id, false);
    }

    /**
     * <pre>
     * Delete more entities using the given Primary Keys
     * </pre>
     *
     * @param ids the primary key to filter for
     */
    public void removeByIds(final Collection<PK> ids) {
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
    public void removeByColumnEqualsValue(final String column,
                                          final Object value,
                                          final boolean notNull) {
        this.streamByColumnEqualsValue(column, value, 0, Integer.MAX_VALUE, true)
            .forEach(this::remove);
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
    public void removeByColumnInValues(final String column,
                                       final Collection<? extends Object> values,
                                       final boolean notNull) {
        this.streamByColumnInValues(column, values, 0, Integer.MAX_VALUE, notNull)
            .forEach(this::remove);
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
    public ENTITY updateById(final ENTITY source) {
        final ENTITY persisted = this.findPersisted(source);
        persisted.update(source);
        return persisted;
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
    public Stream<ENTITY> updateByIds(final Collection<ENTITY> sources) {
        return this.updateByIds(sources, true);
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
    public Stream<ENTITY> updateByIds(final Collection<ENTITY> sources,
                                      final boolean allExpected) {
        final Map<PK, ENTITY> persistedMap = this.asMap(this.streamPersisted(sources));
        return sources.stream()
                      .map(source -> {
                          final PK id = source.getId();
                          if (persistedMap.containsKey(id)) {
                              final ENTITY entity = persistedMap.get(id);
                              entity.update(source);
                              return entity;
                          } else if (allExpected) {
                              throw new UnexpectedException(this.name + ": Missing Entity in Update for PK=" + id.toString());
                          } else {
                              return source;
                          }
                      });
    }

    /**
     * <pre>
     * Merges the entity
     * </pre>
     *
     * @param entity the entity
     * @return the merged entity
     * @see jakarta.persistence.EntityManager#merge(Object) jakarta.persistence.EntityManager#merge(Object)jakarta.persistence.EntityManager#merge(Object)
     */
    public ENTITY merge(final ENTITY entity) {
        return this.em()
                   .merge(this.assertNotNull(entity));
    }

    /**
     * <pre>
     * Merges all the entities in the list, returning the results in a stream.
     * </pre>
     *
     * @param sources the sources
     * @return the merged entities in a Stream&#x3C;ENTITY&#x3E;
     * @see jakarta.persistence.EntityManager#merge(Object) jakarta.persistence.EntityManager#merge(Object)jakarta.persistence.EntityManager#merge(Object)
     */
    public Stream<ENTITY> mergeAll(final Collection<ENTITY> sources) {
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
     * @see jakarta.persistence.EntityManager#persist(Object) jakarta.persistence.EntityManager#persist(Object)jakarta.persistence.EntityManager#persist(Object)
     */
    public ENTITY persist(final ENTITY source) {
        final ENTITY newEntity = this.newInstance(this.assertNotNull(source));
        this.em()
            .persist(newEntity);
        return newEntity;
    }

    /**
     * <pre>
     * Persists all the entities in the list, returning the results in a stream.
     * </pre>
     *
     * @param sources the sources
     * @return the merged entities in a Stream&#x3C;ENTITY&#x3E;
     * @see jakarta.persistence.EntityManager#persist(Object) jakarta.persistence.EntityManager#persist(Object)jakarta.persistence.EntityManager#persist(Object)
     */
    public Stream<ENTITY> persistAll(final Collection<ENTITY> sources) {
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
    public ENTITY put(final ENTITY source) {
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
    public Stream<ENTITY> putAll(final Collection<ENTITY> sources) {
        return sources.stream()
                      .map(this::put);

    }

    /**
     * Returns the Root for the Entity Query
     * Implements the Join Left eager Fetch for all subsequent queries using this root.
     *
     * @param query the Criteria Query
     * @return the root entity
     */
    protected Root<ENTITY> entity(final CriteriaQuery<ENTITY> query) {
        return query.from(this.type);
    }

    /**
     * Returns the Criteria Builder
     *
     * @return the Criteria Builder
     */
    protected CriteriaBuilder cb() {
        return this.em()
                   .getCriteriaBuilder();
    }

    /**
     * Return the base of the query
     *
     * @return the Criteria Query
     */
    protected CriteriaQuery<ENTITY> query() {
        return this.cb()
                   .createQuery(this.type);
    }

    /**
     * <pre>
     * Builder for the equals expression.
     * </pre>
     *
     * @param column  the column to filter for
     * @param value   the value to filter for
     * @param notNull if the value van be null
     * @param entity  the entity root
     * @return the criteria builder expression
     */
    protected Expression<Boolean> equals(String column,
                                         final Object value,
                                         final boolean notNull,
                                         final Root<ENTITY> entity) {
        column = this.columnFrom(column);
        if (this.applyFilter(value, notNull)) {
            return this.cb()
                       .equal(entity.get(column), value);
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
     * @param values the values in a hash map
     * @param entity the entity root
     * @return the criteria builder expression
     */
    protected Expression<Boolean> equals(final HashMap<String, Object> values,
                                         final Root<ENTITY> entity) {
        return values.entrySet()
                     .stream()
                     .map(entry -> this.cb()
                                       .equal(entity.get(entry.getKey()), entry.getValue()))
                     .collect(Collectors.reducing(this.cb()::and))
                     .orElseThrow(() -> new IllegalArgumentException(" Bad Content " + values));
    }

    /**
     * <pre>
     * Builder for the like expression.
     * </pre>
     *
     * @param column  the column to filter for
     * @param value   the value to filter for
     * @param notNull if the value van be null
     * @param entity  the entity root
     * @return the criteria builder expression
     */
    protected Expression<Boolean> like(String column,
                                       final String value,
                                       final boolean notNull,
                                       final Root<ENTITY> entity) {
        column = this.columnFrom(column);
        if (this.applyFilter(value, notNull)) {
            return this.cb()
                       .like(entity.get(column), value);
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
    protected Expression<Boolean> in(String column,
                                     final Collection<? extends Object> values,
                                     final boolean notNull,
                                     final Root<ENTITY> entity) {
        column = this.columnFrom(column);
        if (this.applyFilter(values, notNull)) {
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
     * @param values the map of values to filter for
     * @param entity the entity root
     * @return the criteria builder expression
     */
    protected Expression<Boolean> in(final Map<String, List<Object>> values,
                                     final Root<ENTITY> entity) {
        return values.entrySet()
                     .stream()
                     .map(entry -> entity.get(entry.getKey())
                                         .in(entry.getValue()))
                     .collect(Collectors.reducing(this.cb()::and))
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
    protected <R> R assertNotNull(final R source) {
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
    protected ENTITY assertNotNull(final ENTITY source,
                                   final boolean expected) {
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
    protected <T> T resultAs(final NoResultException exception,
                             final boolean expected) {
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
    protected String columnFrom(final String column) {
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
    protected <R> boolean applyFilter(final R value,
                                      final boolean notNull) {
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
        return this.em;
    }

    /**
     * <pre>
     * Type of the persisted Object
     * </pre>
     *
     * @return the Type of the persisted Object
     */
    public Class<ENTITY> getType() {
        return this.type;
    }

    /**
     * <pre>
     * Type of the persisted Object Primary Key
     * </pre>
     *
     * @return the Type of the persisted Object Primary Key
     */
    public Class<PK> getKeyType() {
        return this.keyType;
    }

    /**
     * <pre>
     * Name of this DataAccess
     * </pre>
     *
     * @return the Name of this DataAccess
     */
    public String getName() {
        return this.name;
    }

    /**
     * <pre>
     * Collects the input stream of entities in a Map with keys from the primary key.
     * </pre>
     *
     * @param sources the sources
     * @return the map
     */
    public Map<PK, ENTITY> asMap(final Stream<ENTITY> sources) {
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
    public List<ENTITY> asList(final Stream<ENTITY> sources) {
        return sources.collect(Collectors.toList());
    }

    /**
     * New instance entity.
     *
     * @param source the source
     * @return the entity
     */
    public ENTITY newInstance(final ENTITY source) {
        try {
            final ENTITY entity = this.noArgsConstructor.newInstance();
            entity.update(source);
            return entity;
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
