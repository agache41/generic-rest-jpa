
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
import io.github.agache41.generic.rest.jpa.update.Updatable;
import io.github.agache41.generic.rest.jpa.update.Update;
import io.github.agache41.generic.rest.jpa.utils.ReflectionUtils;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.UriInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.agache41.generic.rest.jpa.dataAccess.PrimaryKey.ID;
import static jakarta.transaction.Transactional.TxType.REQUIRED;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

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
@Transactional(REQUIRED)
public class DataAccess<ENTITY extends PrimaryKey<PK> & Updatable<ENTITY>, PK> {

    /**
     * The constant findById must be used as a suffix for Named Queries. Syntax is ClassName.findById
     */
    public static final String findById = "findById";
    /**
     * The constant deleteById must be used as a suffix for Named Queries. Syntax is ClassName.deleteById
     */
    public static final String deleteById = "deleteById";
    /**
     * The constant listAll ust be used as a suffix for Named Queries. Syntax is ClassName.listAll
     */
    public static final String listAll = "listAll";
    /**
     * The Set of reserved names to be excepted from URL Parameters.
     */
    protected static final Set<String> reserved = Stream.of("cut", "maxResults", "firstResult", "orderBy")
                                                        .collect(Collectors.toSet());
    /**
     * The Regex matching orderBy URL Parameter content.
     */
    protected static final Pattern orderByColumn = Pattern.compile("(?i)([a-zA-Z_$.0-9]+)(\\s(asc|desc))?");
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
//    /**
//     * <pre>
//     * The ClassReflector of the persisted Object
//     * </pre>
//     */
//    protected final ClassReflector<ENTITY> classReflector;
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
//    /**
//     * <pre>
//     * List with fields that are to be eager fetched.
//     * </pre>
//     */
//    protected final List<String> eagerFields;
    /**
     * The Persisted field names.
     */
    protected final Predicate<Map.Entry<String, ?>> notReservedNames;
    /**
     * THe named queries available in the entity.
     */
    protected final Set<String> namedQueries;
    /**
     * The Find by id named query.
     */
    protected final String findByIdNamedQuery;
    /**
     * The Delete by id named query.
     */
    protected final String deleteByIdNamedQuery;
    /**
     * The List all named query.
     */
    protected final String listAllNamedQuery;
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
//        this.classReflector = ClassReflector.ofClass(this.type);
//        this.eagerFields = this.classReflector.getReflectors()
//                                              .values()
//                                              .stream()
//                                              .filter(FieldReflector::isEager)
//                                              .map(FieldReflector::getName)
//                                              .collect(Collectors.toList());
        this.notReservedNames = entry -> !reserved.contains(entry.getKey());
        this.namedQueries = this.findEntityNamedQueries();
        this.findByIdNamedQuery = this.type.getSimpleName() + "." + findById;
        this.deleteByIdNamedQuery = this.type.getSimpleName() + "." + deleteById;
        this.listAllNamedQuery = this.type.getSimpleName() + "." + listAll;
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
     * If a named query findById is annotated on the entity, this will be used.
     * </pre>
     *
     * @param id       the primary key to use, must be not null
     * @param expected if a persisted entity must exist
     * @return the entity for the primary key or null if not found. If no entity is found and expected is set to true ExpectedException is thrown.
     * @see jakarta.persistence.EntityManager#find(Class, Object) jakarta.persistence.EntityManager#find(Class, Object)jakarta.persistence.EntityManager#find(Class, Object)jakarta.persistence.EntityManager#find(Class, Object)jakarta.persistence.EntityManager#find(Class, Object)jakarta.persistence.EntityManager#find(Class, Object)jakarta.persistence.EntityManager#find(Class, Object)jakarta.persistence.EntityManager#find(Class, Object)jakarta.persistence.EntityManager#find(Class, Object)
     */
    public ENTITY findById(final PK id,
                           final boolean expected) {
        if (this.namedQueries.contains(this.findByIdNamedQuery)) {
            return this.em()
                       .createNamedQuery(this.findByIdNamedQuery, this.type)
                       .setParameter(ID, id)
                       .getSingleResult();
        } else {
            return this.assertNotNull(this.em()
                                          .find(this.type, this.assertNotNull(id)), expected);
        }
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
     * The method creates a select all query and applies the given filter paramter.
     * If a named query is provided in the entity (named ClassName.listAll), then this query will be used as a base
     * and the given parameter will be applied.
     * </pre>
     *
     * @param firstResult the first result
     * @param maxResults  the max results
     * @param uriInfo     the uri info
     * @return all the entities in a Stream&#x3C;ENTITY&#x3E;
     */
    public List<ENTITY> listAll(final int firstResult,
                                final int maxResults,
                                final UriInfo uriInfo) {
        final TypedQuery<ENTITY> typedQuery;
        if (this.namedQueries.contains(this.listAllNamedQuery)) {
            typedQuery = this.em()
                             .createNamedQuery(this.listAllNamedQuery, this.type);
            final Map<String, List<Object>> filterQueryParams = this.filterQueryParams(uriInfo);
            if (!filterQueryParams.isEmpty()) {
                filterQueryParams.entrySet()
                                 .stream()
                                 .forEach(entry -> typedQuery.setParameter(entry.getKey(), entry.getValue()));
            }
        } else {
            final CriteriaQuery<ENTITY> criteriaQuery = this.query();
            final Root<ENTITY> entity = this.entity(criteriaQuery);
            CriteriaQuery<ENTITY> select = criteriaQuery.select(entity);
            final Map<String, List<Object>> filterQueryParams = this.filterQueryParams(uriInfo);
            if (!filterQueryParams.isEmpty()) {
                select = select.where(this.in(filterQueryParams, entity));
            }
            final LinkedHashMap<String, Boolean> orderBy = this.orderByQueryParams(uriInfo);
            select.orderBy(orderBy.entrySet()
                                  .stream()
                                  .map(entry -> {
                                      if (entry.getValue()) {
                                          return this.cb()
                                                     .asc(this.attr(entity, entry.getKey()));
                                      } else {
                                          return this.cb()
                                                     .desc(this.attr(entity, entry.getKey()));
                                      }
                                  })
                                  .collect(toList()));
            typedQuery = this.em()
                             .createQuery(select);
        }
        return typedQuery.setFirstResult(firstResult)
                         .setMaxResults(maxResults)
                         .getResultList();
    }

    /**
     * <pre>
     * Finds all entities with the Primary Key within the given list of ids.
     * </pre>
     *
     * @param ids the ids
     * @return entities in a Stream&#x3C;ENTITY&#x3E;
     */
    public List<ENTITY> listByIds(final Collection<? extends PK> ids) {
        return this.listByColumnInValues(ID, ids, 0, ids.size());
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
    public Map<PK, ENTITY> mapPersisted(final Collection<? extends PrimaryKey<PK>> filter) {
        return this.listByIds(filter.stream()
                                    .map(PrimaryKey::getId)
                                    .collect(toList()))
                   .stream()
                   .collect(Collectors.toMap(PrimaryKey<PK>::getId, Function.identity()));
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
    public List<ENTITY> listByColumnEqualsValue(final String filterColumn,
                                                final Object value,
                                                final int firstResult,
                                                final int maxResults) {
        return this.listByColumnEqualsValue(filterColumn, value, firstResult, maxResults, true);
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
    public List<ENTITY> listByColumnEqualsValue(final String column,
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
                   .getResultList();

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
     * @param mapValues   the map values
     * @param firstResult the first result
     * @param maxResults  the max results
     * @return the persisted entity
     */
    public List<ENTITY> listByContentEquals(final Map<String, Object> mapValues,
                                            final int firstResult,
                                            final int maxResults) {
        final CriteriaQuery<ENTITY> query = this.query();
        final Root<ENTITY> entity = this.entity(query);
        return this.em()
                   .createQuery(query.select(entity)
                                     .where(this.equals(mapValues, entity)))
                   .setFirstResult(firstResult)
                   .setMaxResults(maxResults)
                   .getResultList();
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
    public List<ENTITY> listByColumnLikeValue(final String column,
                                              final String value,
                                              final int firstResult,
                                              final int maxResults) {
        return this.listByColumnLikeValue(column, value, firstResult, maxResults, true);
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
     * @param uriInfo    the uri info
     * @return entities in a Stream&#x3C;ENTITY&#x3E;
     */
    public List<String> autocompleteByColumnLikeValue(final String column,
                                                      final String value,
                                                      final int maxResults,
                                                      final UriInfo uriInfo) {

        final CriteriaQuery<String> query = this.cb()
                                                .createQuery(String.class);
        final Root<ENTITY> entity = query.from(this.type);
        final Path<String> attr = this.attr(entity, column);
        return this.em()
                   .createQuery(query.select(attr)
                                     .distinct(true)
                                     .where(this.filterQueryParamsAnd(this.like(column, value, true, entity), uriInfo, entity))
                                     .orderBy(this.cb()
                                                  .asc(attr)))
                   .setMaxResults(maxResults)
                   .getResultList();
    }

    /**
     * Processes multipath Entity attributes. Example id.name
     *
     * @param <Y>    the type parameter
     * @param entity the entity
     * @param name   the name
     * @return the path
     */
    protected <Y> Path<Y> attr(final Root<ENTITY> entity,
                               final String name) {
        if (!name.contains(".")) {
            return entity.get(name);
        }
        Path<Y> result = null;
        for (final String path : name.split("\\.")) {
            if (result == null) {
                result = entity.get(path);
            } else {
                result = result.get(path);
            }
        }
        return result;
    }

    /**
     * <pre>
     * Finds all entities whose value in a specified column are like the given value.
     * The SQL Like operator is used.
     * Result is a aggregation list containing max(id),value, count(id).
     * Specifically if on a row count = 1 then the id can be used as unique for the given value.
     *
     * </pre>
     *
     * @param column     the column
     * @param value      the value
     * @param maxResults the max results
     * @param uriInfo    the uri info
     * @return the stream
     */
    public List<IdGroup<PK>> autocompleteIdsByColumnLikeValue(final String column,
                                                              final String value,
                                                              final int maxResults,
                                                              final UriInfo uriInfo) {

        final CriteriaQuery<Tuple> query = this.cb()
                                               .createTupleQuery();
        final Root<ENTITY> entity = query.from(this.type);
        final Path<PK> id = entity.get(ID);
        final Path<String> attr = this.attr(entity, column);
        final CriteriaQuery<Tuple> multiselect = query.multiselect(id, attr);
        return this.em()
                   .createQuery(multiselect.where(this.filterQueryParamsAnd(this.like(column, value, true, entity), uriInfo, entity))
                                           .orderBy(this.cb()
                                                        .asc(attr), this.cb()
                                                                        .asc(id)))
                   .getResultStream()
                   .map(IdEntry<PK>::new)
                   .collect(Collectors.groupingBy(IdEntry::getValue, collectingAndThen(toList(), IdGroup<PK>::new)))
                   .values()
                   .stream()
                   .limit(maxResults)
                   .sorted(Comparator.comparing(IdGroup::getValue))
                   .collect(toList());
    }

    /**
     * Generates a filter expression from the query params that can be chained.
     *
     * @param expression the expression
     * @param uriInfo    the uri info
     * @param entity     the entity
     * @return the expression
     */
    protected Expression<Boolean> filterQueryParamsAnd(final Expression<Boolean> expression,
                                                       final UriInfo uriInfo,
                                                       final Root<ENTITY> entity) {

        final Map<String, List<Object>> filterQueryParams = this.filterQueryParams(uriInfo);
        if (filterQueryParams.isEmpty()) {
            return expression;
        }
        return this.cb()
                   .and(expression, this.in(filterQueryParams, entity));
    }

    /**
     * Generates a filter expression from the query params.
     *
     * @param uriInfo the uri info
     * @return the map
     */
    protected Map<String, List<Object>> filterQueryParams(final UriInfo uriInfo) {
        if (uriInfo == null) {
            return Collections.emptyMap();
        }
        return uriInfo.getQueryParameters()
                      .entrySet()
                      .stream()
                      .filter(this.notReservedNames)
                      .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue()
                                                                                 .stream()
                                                                                 .map(string -> (Object) string)
                                                                                 .collect(toList())));
    }

    private LinkedHashMap<String, Boolean> orderByQueryParams(final UriInfo uriInfo) {
        final LinkedHashMap<String, Boolean> result = new LinkedHashMap<>();
        if (uriInfo == null) {
            return result;
        }
        final List<String> orderBy = uriInfo.getQueryParameters()
                                            .get("orderBy");
        if (orderBy == null || orderBy.isEmpty()) {
            return result;
        }
        for (final String entry : orderBy) {
            final Matcher matcher = orderByColumn.matcher(entry);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("OrderBy parameter [orderBy=" + entry + "] does not match pattern [" + orderByColumn.pattern() + "]");
            }
            result.put(matcher.group(1), !"desc".equalsIgnoreCase(matcher.group(3)));
        }
        return result;
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
    public List<ENTITY> listByColumnLikeValue(final String column,
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
                   .getResultList();
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
    public List<ENTITY> listByColumnInValues(final String column,
                                             final Collection<? extends Object> values,
                                             final int firstResult,
                                             final int maxResults) {
        return this.listByColumnInValues(column, values, firstResult, maxResults, true);
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
    public List<ENTITY> listByColumnInValues(final String column,
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
                   .getResultList();
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
     * @param mapValues   the map values
     * @param firstResult the first result
     * @param maxResults  the max results
     * @return the persisted entity
     */
    public List<ENTITY> listByContentInValues(final Map<String, List<Object>> mapValues,
                                              final int firstResult,
                                              final int maxResults) {
        final CriteriaQuery<ENTITY> query = this.query();
        final Root<ENTITY> entity = this.entity(query);
        return this.em()
                   .createQuery(query.select(entity)
                                     .where(this.in(mapValues, entity)))
                   .setFirstResult(firstResult)
                   .setMaxResults(maxResults)
                   .getResultList();
    }

    /**
     * <pre>
     * Deletes the given entity
     * </pre>
     *
     * @param entity the given entity
     * @see jakarta.persistence.EntityManager#remove(Object) jakarta.persistence.EntityManager#remove(Object)jakarta.persistence.EntityManager#remove(Object)jakarta.persistence.EntityManager#remove(Object)jakarta.persistence.EntityManager#remove(Object)jakarta.persistence.EntityManager#remove(Object)jakarta.persistence.EntityManager#remove(Object)jakarta.persistence.EntityManager#remove(Object)jakarta.persistence.EntityManager#remove(Object)
     */
    public void remove(final ENTITY entity) {
        if (this.namedQueries.contains(this.deleteByIdNamedQuery)) {
            this.em()
                .createNamedQuery(this.deleteByIdNamedQuery)
                .setParameter(ID, entity.getId())
                .executeUpdate();
        } else {
            this.em()
                .remove(this.assertNotNull(entity));
        }
    }

    /**
     * <pre>
     * Delete one entity using the given Primary Key
     * </pre>
     *
     * @param id the primary key to look for
     */
    public void removeById(final PK id) {
        this.removeByColumnEqualsValue(ID, id, false);
    }

    /**
     * <pre>
     * Delete more entities using the given Primary Keys
     * </pre>
     *
     * @param ids the primary key to filter for
     */
    public void removeByIds(final Collection<PK> ids) {
        this.removeByColumnInValues(ID, ids, false);
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
        this.listByColumnEqualsValue(column, value, 0, Integer.MAX_VALUE, true)
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
        this.listByColumnInValues(column, values, 0, Integer.MAX_VALUE, notNull)
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
    public List<ENTITY> updateByIds(final Collection<ENTITY> sources) {
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
    public List<ENTITY> updateByIds(final Collection<ENTITY> sources,
                                    final boolean allExpected) {
        final Map<PK, ENTITY> persistedMap = this.mapPersisted(sources);
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
                      })
                      .collect(toList());
    }

    /**
     * <pre>
     * Merges the entity
     * </pre>
     *
     * @param entity the entity
     * @return the merged entity
     * @see jakarta.persistence.EntityManager#merge(Object) jakarta.persistence.EntityManager#merge(Object)jakarta.persistence.EntityManager#merge(Object)jakarta.persistence.EntityManager#merge(Object)jakarta.persistence.EntityManager#merge(Object)jakarta.persistence.EntityManager#merge(Object)jakarta.persistence.EntityManager#merge(Object)jakarta.persistence.EntityManager#merge(Object)jakarta.persistence.EntityManager#merge(Object)
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
     * @see jakarta.persistence.EntityManager#merge(Object) jakarta.persistence.EntityManager#merge(Object)jakarta.persistence.EntityManager#merge(Object)jakarta.persistence.EntityManager#merge(Object)jakarta.persistence.EntityManager#merge(Object)jakarta.persistence.EntityManager#merge(Object)jakarta.persistence.EntityManager#merge(Object)jakarta.persistence.EntityManager#merge(Object)jakarta.persistence.EntityManager#merge(Object)
     */
    public List<ENTITY> mergeAll(final Collection<ENTITY> sources) {
        return sources.stream()
                      .map(this::merge)
                      .collect(toList());
    }

    /**
     * <pre>
     * Persists an entity
     * </pre>
     *
     * @param source the source
     * @return the persisted entity
     * @see jakarta.persistence.EntityManager#persist(Object) jakarta.persistence.EntityManager#persist(Object)jakarta.persistence.EntityManager#persist(Object)jakarta.persistence.EntityManager#persist(Object)jakarta.persistence.EntityManager#persist(Object)jakarta.persistence.EntityManager#persist(Object)jakarta.persistence.EntityManager#persist(Object)jakarta.persistence.EntityManager#persist(Object)jakarta.persistence.EntityManager#persist(Object)
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
     * @return the persisted entities in a Stream&#x3C;ENTITY&#x3E;
     * @see jakarta.persistence.EntityManager#persist(Object) jakarta.persistence.EntityManager#persist(Object)jakarta.persistence.EntityManager#persist(Object)jakarta.persistence.EntityManager#persist(Object)jakarta.persistence.EntityManager#persist(Object)jakarta.persistence.EntityManager#persist(Object)jakarta.persistence.EntityManager#persist(Object)jakarta.persistence.EntityManager#persist(Object)jakarta.persistence.EntityManager#persist(Object)
     */
    public List<ENTITY> persistAll(final Collection<ENTITY> sources) {
        return sources.stream()
                      .map(this::persist)
                      .collect(toList());
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
    protected Expression<Boolean> equals(final String column,
                                         final Object value,
                                         final boolean notNull,
                                         final Root<ENTITY> entity) {
        if (this.applyFilter(value, notNull)) {
            return this.cb()
                       .equal(this.attr(entity, column), value);
        } else {
            return this.attr(entity, column)
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
    protected Expression<Boolean> equals(final Map<String, Object> values,
                                         final Root<ENTITY> entity) {
        return values.entrySet()
                     .stream()
                     .filter(this.notReservedNames)
                     .map(entry -> this.cb()
                                       .equal(entity.get(entry.getKey()), entry.getValue()))
                     .collect(Collectors.reducing(this.cb()::and))
                     .orElseThrow(() -> new IllegalArgumentException(" Bad Filter Content " + values + " please specify at least one valid field for the field = value (equals) clause! "));
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
    protected Expression<Boolean> like(final String column,
                                       final String value,
                                       final boolean notNull,
                                       final Root<ENTITY> entity) {
        if (this.applyFilter(value, notNull)) {
            return this.cb()
                       .like(this.attr(entity, column), value);
        } else {
            return this.attr(entity, column)
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
    protected Expression<Boolean> in(final String column,
                                     final Collection<? extends Object> values,
                                     final boolean notNull,
                                     final Root<ENTITY> entity) {
        if (this.applyFilter(values, notNull)) {
            return this.attr(entity, column)
                       .in(values);
        } else {
            return this.attr(entity, column)
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
                     .filter(this.notReservedNames)
                     .map(entry -> this.attr(entity, entry.getKey())
                                       .in(entry.getValue()))
                     .collect(Collectors.reducing(this.cb()::and))
                     .orElseThrow(() -> new IllegalArgumentException(" Bad Filter Content " + values + " please provide at least one field for the field in (..values) clause!"));
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
     * New instance entity.
     *
     * @param source the source
     * @return the entity
     */
    public ENTITY newInstance(final ENTITY source) {
        try {
            final ENTITY entity = this.noArgsConstructor.newInstance();
            source.update(entity);
            return entity;
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find entity named queries set.
     *
     * @return the set
     */
    protected Set<String> findEntityNamedQueries() {
        final Set<String> entityNamedQueries = new HashSet<>();
        final NamedQueries namedQueries = this.type.getAnnotation(NamedQueries.class);
        if (namedQueries != null) {
            Stream.of(namedQueries.value())
                  .map(NamedQuery::name)
                  .forEach(entityNamedQueries::add);
        }
        final NamedQuery namedQuery = this.type.getAnnotation(NamedQuery.class);
        if (namedQuery != null) {
            entityNamedQueries.add(namedQuery.name());
        }
        return entityNamedQueries;
    }
}
