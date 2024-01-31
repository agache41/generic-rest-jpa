
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

package io.github.agache41.generic.rest.jpa.resourceService;

import io.github.agache41.generic.rest.jpa.dataAccess.PrimaryKey;
import jakarta.ws.rs.PathParam;

import java.util.List;

/**
 * The interface Resource service defines the REST Methods to be used on the Entity Domain Model
 *
 * @param <T> the type parameter
 * @param <K> the type parameter
 */
public interface ResourceService<T extends PrimaryKey<K>, K> {

    int autocompleteCut = 4;

    int getAutocompleteCut();

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
    /*@GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")*/
    T get(@PathParam("id") K id);

    /**
     * <pre>
     * Returns all the entities for the given table.
     * </pre>
     *
     * @return the list of entities
     */
    /*@GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/all/asList")*/
    List<T> getAllAsList();

    /**
     * <pre>
     * Finds and returns the corresponding entity for the given list of ids.
     * The id type must be basic (e.g. String, Long) or have a simple rest representation that can be used in a url path segment.
     * </pre>
     *
     * @param ids the list of ids
     * @return the list of entities
     */
    /*@GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/byIds/{ids}/asList")*/
    List<T> getByIdsAsList(@PathParam("ids") List<K> ids);

    /**
     * <pre>
     * Finds and returns the corresponding entity for the given list of ids.
     * </pre>
     *
     * @param ids the list of ids
     * @return the list of entities
     */
    /*@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/byIds/asList")*/
    List<T> postByIdsAsList(List<K> ids);

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
    /*@GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/filter/{stringField}/equals/{value}/asList")*/
    List<T> getFilterStringFieldEqualsValueAsList(@PathParam("stringField") String stringField,
                                                  @PathParam("value") String value);

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
    /*@GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/filter/{stringField}/like/{value}/asList")*/
    List<T> getFilterStringFieldLikeValueAsList(@PathParam("stringField") String stringField,
                                                @PathParam("value") String value);

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
    /*@GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/filter/{stringField}/in/{values}/asList")*/
    List<T> getFilterStringFieldInValuesAsList(@PathParam("stringField") String stringField,
                                               @PathParam("values") List<String> values);

    /**
     * <pre>
     * Finds all values in a database column whose value is like the given value.
     * The SQL Like operator will be used.
     * The field can only be of String type.
     * </pre>
     *
     * @param stringField the field to use in filter, can only be a string value
     * @param value       the string value to equal
     * @return the list of entities matching
     */
    /*@GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/autocomplete/{stringField}/like/{value}/asSortedSet")*/
    List<String> getAutocompleteStringFieldLikeValueAsSortedSet(@PathParam("stringField") String stringField,
                                                                @PathParam("value") String value);

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
    /*@POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/filter/content/equals/value/asList")*/
    List<T> postFilterContentEqualsAsList(T value);


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
    /*@POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/filter/content/in/values/asList")*/
    List<T> postFilterContentInAsList(List<T> values);

    /**
     * <pre>
     * Inserts a new entity in the database or updates an existing one.
     * </pre>
     *
     * @param source the source
     * @return the inserted entity
     */
    /*@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)*/
    T post(T source);

    /**
     * <pre>
     * Inserts a list of new entities in the database or updates the existing ones.
     * </pre>
     *
     * @param sources the list of new data
     * @return the inserted entities
     */
    /*@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/list/asList")*/
    List<T> postListAsList(List<T> sources);

    /**
     * <pre>
     * Updates an existing entity by id.
     * The Entity with the given id must exist in the Database or a UnexpectedException is thrown.
     * </pre>
     *
     * @param source the source
     * @return the updated entity
     */
    /*@PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)*/
    T put(T source);

    /**
     * <pre>
     * Updates existing entities by id.
     * The Entities with the given ids must exist in the Database or a UnexpectedException is thrown.
     * </pre>
     *
     * @param sources the source
     * @return the updated entities
     */
    /*@PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/list/asList")*/
    List<T> putListAsList(List<T> sources);

    /**
     * <pre>
     * Deletes the entity for the given id.
     * </pre>
     *
     * @param id the id
     */
    /*@DELETE
    @Path("/{id}")*/
    void delete(@PathParam("id") K id);

    /**
     * <pre>
     * Deletes all the entities for the given ids in the request Body
     * </pre>
     *
     * @param ids the ids
     */
    /*@DELETE
    @Path("/byIds")*/
    void deleteByIds(List<K> ids);

    /**
     * <pre>
     * Deletes all the entities for the given ids.
     * </pre>
     *
     * @param ids the ids
     */
    /*@DELETE
    @Path("/byIds/{ids}")*/
    void deleteByIdsInPath(@PathParam("ids") List<K> ids);
}
