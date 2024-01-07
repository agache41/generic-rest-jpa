
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

package org.structured.api.quarkus.resourceService;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.structured.api.quarkus.dataAccess.DataAccess;
import org.structured.api.quarkus.dataAccess.PrimaryKey;
import org.structured.api.quarkus.update.ClassReflector;
import org.structured.api.quarkus.update.FieldReflector;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * <pre>
 * Base class for resource REST APIs.
 * The class implements methods for basic REST operations on the underlying Class
 * </pre>
 *
 * @param <T> the type parameter
 * @param <K> the type parameter
 */
public abstract class AbstractResourceServiceImpl<T extends PrimaryKey<K>, K> implements ResourceService<T, K> {

    /**
     * <pre>
     * Default data access layer , used for communicating with the database.
     * </pre>
     */
    @Inject
    @Named("base")
    protected DataAccess<T, K> dataAccess;

    /**
     * <pre>
     * Getter for the data access layer.
     * </pre>
     *
     * @return the data access
     */
    public DataAccess<T, K> getDataAccess() {
        return dataAccess;
    }

    /**
     * <pre>
     * Finds and returns the corresponding entity for the given id.
     * The id type must be basic (e.g. String, Long) or have a simple rest representation that can be used in a url path segment.
     *
     * </pre>
     *
     * @param id the id
     * @return the corresponding entity at the provided id. If no entity is found, an Expected will be thrown.
     */
    @Override
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public T get(@PathParam("id") K id) {
        return getDataAccess().findById(id);
    }

    /**
     * <pre>
     * Returns all the entities for the given table.
     * </pre>
     *
     * @return the list of entities
     */
    @Override
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/all/asList")
    public List<T> getAllAsList() {
        return getDataAccess().streamAll()
                              .collect(Collectors.toList());
    }

    /**
     * <pre>
     * Finds and returns the corresponding entity for the given list of ids.
     * The id type must be basic (e.g. String, Long) or have a simple rest representation that can be used in a url path segment.
     * </pre>
     *
     * @param ids the list of ids
     * @return the list of entities
     */
    @Override
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/byIds/{ids}/asList")
    public List<T> getByIdsAsList(@PathParam("ids") List<K> ids) {
        return getDataAccess().streamByIds(ids)
                              .collect(Collectors.toList());
    }

    /**
     * <pre>
     * Finds and returns the corresponding entity for the given list of ids.
     * </pre>
     *
     * @param ids the list of ids
     * @return the list of entities
     */
    @Override
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/byIds/asList")
    public List<T> postByIdsAsList(List<K> ids) {
        return getDataAccess().streamByIds(ids)
                              .collect(Collectors.toList());
    }

    /**
     * <pre>
     * Finds all entities whose value in a specified field is equal the given value.
     * The field can only be of String type.
     * </pre>
     *
     * @param stringField the field to use in filter, can only be a string value
     * @param value       the string value to equal
     * @return the list of entities matching
     */
    @Override
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/filter/{stringField}/equals/{value}/asList")
    public List<T> getFilterStringFieldEqualsValueAsList(@PathParam("stringField") String stringField, @PathParam("value") String value) {
        return getDataAccess().streamByColumnEqualsValue(stringField, value)
                              .collect(Collectors.toList());
    }

    /**
     * <pre>
     * Finds all entities whose value in a specified field is like the given value.
     * The SQL Like operator will be used.
     * The field can only be of String type.
     * </pre>
     *
     * @param stringField the field to use in filter, can only be a string value
     * @param value       the string value to equal
     * @return the list of entities matching
     */
    @Override
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/filter/{stringField}/like/{value}/asList")
    public List<T> getFilterStringFieldLikeValueAsList(@PathParam("stringField") String stringField, @PathParam("value") String value) {
        return getDataAccess().streamByColumnLikeValue(stringField, value)
                              .collect(Collectors.toList());
    }

    /**
     * <pre>
     * Finds all entities whose value in a specified field is in the given values list.
     *
     * The field can only be of String type.
     * </pre>
     *
     * @param stringField the field to use in filter, can only be a string value
     * @param values      the values list
     * @return the list of entities matching
     */
    @Override
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/filter/{stringField}/in/{values}/asList")
    public List<T> getFilterStringFieldInValuesAsList(@PathParam("stringField") String stringField, @PathParam("values") List<String> values) {
        return getDataAccess().streamByColumnInValues(stringField, values)
                              .collect(Collectors.toList());
    }

    /**
     * <pre>
     * Finds all entities whose value in a specified field is like the given value.
     * The SQL Like operator will be used.
     * The field can only be of String type.
     * </pre>
     *
     * @param stringField the field to use in filter, can only be a string value
     * @param value       the string value to equal
     * @return the list of entities matching
     */
    @Override
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("autocomplete/{stringField}/like/{value}/asSortedSet")
    public List<String> getAutocompleteStringFieldLikeValueAsSortedSet(@PathParam("stringField") String stringField,
                                                                       @PathParam("value") String value) {
        if (value == null || value.length() < 4) return Collections.emptyList();
        FieldReflector<T, String> fieldReflector = ClassReflector.ofClass(getDataAccess().getType())
                                                                 .getReflector(stringField, String.class);
        return getDataAccess()
                .streamByColumnLikeValue(stringField, value)
                .map(fieldReflector::get)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
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
     * @param value the source
     * @return the list of entities matching
     */
    @Override
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/filter/content/equals/value/asList")
    public List<T> postFilterContentEqualsAsList(T value) {
        return getDataAccess().streamByContentEquals(value)
                              .collect(Collectors.toList());
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
     * @param values the source
     * @return the list of entities matching
     */
    @Override
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/filter/content/in/values/asList")
    public List<T> postFilterContentInAsList(List<T> values) {
        return getDataAccess().streamByContentInValues(values)
                              .collect(Collectors.toList());
    }

    /**
     * <pre>
     * Inserts a new entity in the database or updates an existing one.
     * </pre>
     *
     * @param source the source
     * @return the inserted entity
     */
    @Override
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public T post(T source) {
        return getDataAccess().merge(source);
    }

    /**
     * <pre>
     * Inserts a list of new entities in the database or updates the existing ones.
     * </pre>
     *
     * @param sources the list of new data
     * @return the inserted entities
     */
    @Override
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/list/asList")
    public List<T> postListAsList(List<T> sources) {
        return getDataAccess().mergeAll(sources)
                              .collect(Collectors.toList());
    }

    /**
     * <pre>
     * Updates an existing entity by id.
     * The Entity with the given id must exist in the Database or a UnexpectedException is thrown.
     * </pre>
     *
     * @param source the source
     * @return the updated entity
     */
    @Override
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public T put(T source) {
        return getDataAccess().updateById(source);
    }

    /**
     * <pre>
     * Updates existing entities by id.
     * The Entities with the given ids must exist in the Database or a UnexpectedException is thrown.
     * </pre>
     *
     * @param sources the source
     * @return the updated entities
     */
    @Override
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/list/asList")
    public List<T> putListAsList(List<T> sources) {
        return getDataAccess().updateByIds(sources)
                              .collect(Collectors.toList());
    }

    /**
     * <pre>
     * Deletes the entity for the given id.
     * </pre>
     *
     * @param id the id
     */
    @Override
    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") K id) {
        getDataAccess().removeById(id);
    }

    /**
     * <pre>
     * Deletes all the entities for the given ids.
     * </pre>
     *
     * @param ids the ids
     */
    @Override
    @DELETE
    @Path("/byIds")
    public void deleteByIds(List<K> ids) {
        getDataAccess().removeByIds(ids);
    }


    /**
     * <pre>
     * Deletes all the entities for the given ids.
     * </pre>
     *
     * @param ids the ids
     */
    @Override
    @DELETE
    @Path("/byIds/{ids}")
    public void deleteByIdsInPath(@PathParam("ids") List<K> ids) {
        getDataAccess().removeByIds(ids);
    }

}