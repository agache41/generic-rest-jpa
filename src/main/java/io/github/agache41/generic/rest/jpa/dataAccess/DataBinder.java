
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

import io.github.agache41.generic.rest.jpa.exceptions.UnexpectedException;
import io.github.agache41.generic.rest.jpa.update.TransferObject;
import io.github.agache41.generic.rest.jpa.update.Update;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.UriInfo;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static jakarta.transaction.Transactional.TxType.REQUIRED;
import static java.util.stream.Collectors.toList;

/**
 * The type Data binder.
 *
 * @param <TO>     the type parameter
 * @param <ENTITY> the type parameter
 * @param <PK>     the type parameter
 */
@Dependent
@Transactional(REQUIRED)
public class DataBinder<TO extends PrimaryKey<PK> & TransferObject<TO, ENTITY>, ENTITY extends PrimaryKey<PK>, PK> {

    /**
     * <pre>
     * The type of the TO Object
     * </pre>
     */
    protected final Class<TO> toCLass;

    /**
     * <pre>
     * The type of the ENTITY Object
     * </pre>
     */
    protected final Class<ENTITY> entityClass;
    /**
     * <pre>
     * The type of the PK Object
     * </pre>
     */
    protected final Class<PK> pkClass;

    /**
     * <pre>
     * The Name of this Dao.
     * </pre>
     */
    protected final String name;
    /**
     * The To creator.
     */
    protected final Creator<TO> toCreator;
    /**
     * The Entity creator.
     */
    protected final Creator<ENTITY> entityCreator;
    /**
     * <pre>
     * Default data access layer , used for communicating with the database.
     * </pre>
     */
    @Inject
    @Named("base")
    protected DataAccess<ENTITY, PK> dataAccess;

    /**
     * Instantiates a new Data binder.
     *
     * @param ip the ip
     */
    @Inject
    public DataBinder(final InjectionPoint ip) {
        this(((Class<TO>) (((ParameterizedType) ip.getType()).getActualTypeArguments()[0])), //
             ((Class<ENTITY>) (((ParameterizedType) ip.getType()).getActualTypeArguments()[1])),//
             ((Class<PK>) (((ParameterizedType) ip.getType()).getActualTypeArguments()[2])));//
    }

    /**
     * Instantiates a new Data binder.
     *
     * @param toCLass     the to c lass
     * @param entityClass the entity class
     * @param pkClass     the pk class
     */
    public DataBinder(final Class<TO> toCLass,
                      final Class<ENTITY> entityClass,
                      final Class<PK> pkClass) {
        this.toCLass = toCLass;
        this.toCreator = new Creator<>(this.toCLass);
        this.entityClass = entityClass;
        this.entityCreator = new Creator<>(entityClass);
        this.pkClass = pkClass;
        this.name = DataBinder.class.getSimpleName() + "<" + this.toCLass.getSimpleName() + "," + this.entityClass.getSimpleName() + "," + this.pkClass.getSimpleName() + ">";
    }

    /**
     * Find by id to.
     *
     * @param id the id
     * @return the to
     */
    public TO findById(final PK id) {
        final ENTITY entity = this.getDataAccess()
                                  .findById(id);
        return this.render(entity);
    }

    /**
     * List all list.
     *
     * @param firstResult the first result
     * @param maxResults  the max results
     * @param uriInfo     the uri info
     * @return the list
     */
    public List<TO> listAll(final Integer firstResult,
                            final Integer maxResults,
                            final UriInfo uriInfo) {
        return this.render(this.getDataAccess()
                               .listAll(firstResult, maxResults, uriInfo));
    }

    /**
     * List by ids list.
     *
     * @param ids the ids
     * @return the list
     */
    public List<TO> listByIds(final List<PK> ids) {
        return this.render(this.getDataAccess()
                               .listByIds(ids));
    }

    /**
     * List by column equals value list.
     *
     * @param stringField the string field
     * @param value       the value
     * @param firstResult the first result
     * @param maxResults  the max results
     * @return the list
     */
    public List<TO> listByColumnEqualsValue(final String stringField,
                                            final String value,
                                            final Integer firstResult,
                                            final Integer maxResults) {
        return this.render(this.getDataAccess()
                               .listByColumnEqualsValue(stringField, value, firstResult, maxResults));
    }

    /**
     * List by column like value list.
     *
     * @param stringField the string field
     * @param value       the value
     * @param firstResult the first result
     * @param maxResults  the max results
     * @return the list
     */
    public List<TO> listByColumnLikeValue(final String stringField,
                                          final String value,
                                          final Integer firstResult,
                                          final Integer maxResults) {
        return this.render(this.getDataAccess()
                               .listByColumnLikeValue(stringField, value, firstResult, maxResults));
    }

    /**
     * List by column in values list.
     *
     * @param stringField the string field
     * @param values      the values
     * @param firstResult the first result
     * @param maxResults  the max results
     * @return the list
     */
    public List<TO> listByColumnInValues(final String stringField,
                                         final List<String> values,
                                         final Integer firstResult,
                                         final Integer maxResults) {
        return this.render(this.getDataAccess()
                               .listByColumnInValues(stringField, values, firstResult, maxResults));
    }

    /**
     * List by content equals list.
     *
     * @param value       the value
     * @param firstResult the first result
     * @param maxResults  the max results
     * @return the list
     */
    public List<TO> listByContentEquals(final Map<String, Object> value,
                                        final Integer firstResult,
                                        final Integer maxResults) {
        return this.render(this.getDataAccess()
                               .listByContentEquals(value, firstResult, maxResults));
    }

    /**
     * List by content in values list.
     *
     * @param values      the values
     * @param firstResult the first result
     * @param maxResults  the max results
     * @return the list
     */
    public List<TO> listByContentInValues(final Map<String, List<Object>> values,
                                          final Integer firstResult,
                                          final Integer maxResults) {
        return this.render(this.getDataAccess()
                               .listByContentInValues(values, firstResult, maxResults));
    }

    /**
     * Persist to.
     *
     * @param to the to
     * @return the to
     */
    public TO persist(final TO to) {
        final ENTITY entity = to.create(this.entityCreator.create());
        final ENTITY inserted = this.getDataAccess()
                                    .persist(entity);
        return this.render(inserted);
    }

    /**
     * Persist list.
     *
     * @param toList the to list
     * @return the list
     */
    public List<TO> persist(final List<TO> toList) {
        return toList.stream()
                     .map(this::persist)
                     .collect(Collectors.toList());
    }

    /**
     * <pre>
     * Updates an entity.
     * The code locates the corresponding persisted entity based on the provided primary key.
     * The Entity with the given id must exist in the Database or a UnexpectedException is thrown.
     * The persisted entity is then updated from the source to using only the fields marked with @ {@link Update } annotation
     * </pre>
     *
     * @param to the object that contains the id and is the source for update
     * @return the to for the persisted entity.
     */
    public TO updateById(final TO to) {
        final ENTITY persisted = this.getDataAccess()
                                     .findPersisted(to);
        to.update(persisted);
        return this.render(persisted);
    }

    /**
     * <pre>
     * Updates multiple entities.
     * The code locates the corresponding persisted entities based on the provided primary keys.
     * The persisted entities are then updated from the source entities using only the fields marked with @ {@link Update } annotation
     * </pre>
     *
     * @param toCollection the Collection of TO objects that contains the ids and is the source for update
     * @param allExpected  is set to true, all the Entities with the given id must exist in the Database or a UnexpectedException is thrown.
     * @return the persisted entities in a Stream&#x3C;ENTITY&#x3E;
     */
    public List<TO> updateByIds(final Collection<TO> toCollection,
                                final boolean allExpected) {
        final Map<PK, ENTITY> persistedMap = this.getDataAccess()
                                                 .mapPersisted(toCollection);
        return toCollection.stream()
                           .map(to -> {
                               final PK id = to.getId();
                               if (persistedMap.containsKey(id)) {
                                   final ENTITY entity = persistedMap.get(id);
                                   to.update(entity);
                                   return this.render(entity);
                               } else if (allExpected) {
                                   throw new UnexpectedException(this.name + ": Missing Entity in Update for PK=" + id.toString());
                               } else {
                                   //todo: log warning
                                   return to;
                               }
                           })
                           .collect(toList());
    }

    /**
     * Render to.
     *
     * @param entity the entity
     * @return the to
     */
    protected TO render(final ENTITY entity) {
        return this.toCreator.create()
                             .render(entity);
    }

    private List<TO> render(final List<ENTITY> entities) {
        return entities.stream()
                       .map(this::render)
                       .collect(Collectors.toList());
    }

    /**
     * <pre>
     * Getter for the data access layer.
     * </pre>
     *
     * @return the data access
     */
    public DataAccess<ENTITY, PK> getDataAccess() {
        return this.dataAccess;
    }
}
