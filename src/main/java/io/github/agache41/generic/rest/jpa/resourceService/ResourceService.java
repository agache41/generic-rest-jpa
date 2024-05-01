
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

import io.github.agache41.generic.rest.jpa.dataAccess.IdGroup;
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
    T get(@PathParam("id") K id);

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
    T postById(K id);

    /**
     * <pre>
     * Returns all the entities for the given table.
     * </pre>
     *
     * @param firstResult the first result
     * @param maxResults  the max results
     * @return the list of entities
     */
    List<T> getAllAsList(final Integer firstResult,
                         final Integer maxResults);

    /**
     * <pre>
     * Finds and returns the corresponding entity for the given list of ids.
     * The id type must be basic (e.g. String, Long) or have a simple rest representation that can be used in a url path segment.
     * </pre>
     *
     * @param ids the list of ids
     * @return the list of entities
     */
    List<T> getByIdsAsList(@PathParam("ids") List<K> ids);

    /**
     * <pre>
     * Finds and returns the corresponding entity for the given list of ids.
     * </pre>
     *
     * @param ids the list of ids
     * @return the list of entities
     */
    List<T> postByIdsAsList(List<K> ids);

    /**
     * <pre>
     * Finds all entities whose value in a specified field is equal the given value.
     * The field can only be of String type.
     * FirstResult parameter will be applied on the sql Query.If not provided it will default to configured value.
     * MaxResults parameter will be applied on the sql Query.If not provided it will default to configured value.
     * </pre>
     *
     * @param stringField the field to use in filter, can only be a string value
     * @param value       the string value to equal
     * @param firstResult the first result
     * @param maxResults  the max results
     * @return the list of entities matching
     */
    List<T> getFilterStringFieldEqualsValueAsList(String stringField,
                                                  String value,
                                                  Integer firstResult,
                                                  Integer maxResults);

    /**
     * <pre>
     * Finds all entities whose value in a specified field is like the given value.
     * The SQL Like operator will be used.
     * The field can only be of String type.
     * FirstResult parameter will be applied on the sql Query.If not provided it will default to configured value.
     * MaxResults parameter will be applied on the sql Query.If not provided it will default to configured value.
     * </pre>
     *
     * @param stringField the field to use in filter, can only be a string value
     * @param value       the string value to equal
     * @param firstResult the first result
     * @param maxResults  the max results
     * @return the list of entities matching
     */
    List<T> getFilterStringFieldLikeValueAsList(String stringField,
                                                String value,
                                                Integer firstResult,
                                                Integer maxResults);

    /**
     * <pre>
     * Finds all entities whose value in a specified field is in the given values list.
     *
     * The field can only be of String type.
     * FirstResult parameter will be applied on the sql Query.If not provided it will default to configured value.
     * MaxResults parameter will be applied on the sql Query.If not provided it will default to configured value.
     * </pre>
     *
     * @param stringField the field to use in filter, can only be a string value
     * @param values      the values list
     * @param firstResult the first result
     * @param maxResults  the max results
     * @return the list of entities matching
     */
    List<T> getFilterStringFieldInValuesAsList(String stringField,
                                               List<String> values,
                                               Integer firstResult,
                                               Integer maxResults);

    /**
     * <pre>
     * Finds all values in a database column whose value is like the given value.
     * The SQL Like operator will be used.
     * The field can only be of String type.
     * Cut parameter tells the minimum value length.If not provided it will default to configured value.
     * MaxResults parameter will be applied on the sql Query.If not provided it will default to configured value.
     * </pre>
     *
     * @param stringField the field to use in filter, can only be a string value
     * @param value       the string value to equal
     * @param cut         the cut
     * @param maxResults  the max results
     * @return the list of entities matching
     */
    List<String> getAutocompleteStringFieldLikeValueAsSortedSet(String stringField,
                                                                String value,
                                                                Integer cut,
                                                                Integer maxResults);

    /**
     * <pre>
     * Finds all values in a database column whose value is like the given value.
     * The SQL Like operator will be used.
     * The field can only be of String type.
     * Result is a aggregation list containing max(id),value, count(id).
     * Specifically if on a row count = 1 then the id can be used as unique for the given value.
     * Cut parameter tells the minimum value length.If not provided it will default to configured value.
     * MaxResults parameter will be applied on the sql Query.If not provided it will default to configured value.
     * </pre>
     *
     * @param stringField the field to use in filter, can only be a string value
     * @param value       the string value to equal
     * @param cut         the cut
     * @param maxResults  the max results
     * @return the list of IdGroup object matching the input value
     */
    List<IdGroup<K>> getAutocompleteIdsStringFieldLikeValueAsList(String stringField,
                                                                  String value,
                                                                  Integer cut,
                                                                  Integer maxResults);

    /**
     * <pre>
     * Finds in Database the entities that equals a given content object.
     * The content object must contain non null values just in the fields that are taking part in the filtering.
     * The other null fields are to be ignored.
     * No nulls can be used in the filtering.
     * Example :
     * content = [name ="abcd", no=2, street=null]
     * result is where name = "abcd" and no = 2
     * FirstResult parameter will be applied on the sql Query.If not provided it will default to configured value.
     * MaxResults parameter will be applied on the sql Query.If not provided it will default to configured value.
     * </pre>
     *
     * @param value       the source
     * @param firstResult the first result
     * @param maxResults  the max results
     * @return the list of entities matching
     */
    List<T> postFilterContentEqualsAsList(T value,
                                          Integer firstResult,
                                          Integer maxResults);


    /**
     * <pre>
     * Finds in Database the entities that are in a given content list of given values.
     * The content object must contain non null values just in the fields that are taking part in the filtering.
     * The other null fields are to be ignored.
     * No nulls can be used in the filtering.
     * Example :
     * content = [name =["abcd","bcde","1234"], no=[2,3], street=null]
     * result is where name in ("abcd","bcde","1234") and no in (2,3)
     * FirstResult parameter will be applied on the sql Query.If not provided it will default to configured value.
     * MaxResults parameter will be applied on the sql Query.If not provided it will default to configured value.
     * </pre>
     *
     * @param values      the source
     * @param firstResult the first result
     * @param maxResults  the max results
     * @return the list of entities matching
     */
    List<T> postFilterContentInAsList(List<T> values,
                                      Integer firstResult,
                                      Integer maxResults);

    /**
     * <pre>
     * Inserts a new entity in the database or updates an existing one.
     * </pre>
     *
     * @param source the source
     * @return the inserted entity
     */
    T post(T source);

    /**
     * <pre>
     * Inserts a list of new entities in the database or updates the existing ones.
     * </pre>
     *
     * @param sources the list of new data
     * @return the inserted entities
     */
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
    List<T> putListAsList(List<T> sources);

    /**
     * <pre>
     * Deletes the entity for the given id.
     * </pre>
     *
     * @param id the id
     */
    void delete(@PathParam("id") K id);

    /**
     * <pre>
     * Deletes all the entities for the given ids in the request Body
     * </pre>
     *
     * @param ids the ids
     */
    void deleteByIds(List<K> ids);

    /**
     * <pre>
     * Deletes all the entities for the given ids.
     * </pre>
     *
     * @param ids the ids
     */
    void deleteByIdsInPath(@PathParam("ids") List<K> ids);

    /**
     * Gets the default configuration object.
     * The deriving classes can override this method by means of a simple getter.
     *
     * @return the config object
     */
    default ResourceServiceConfig getConfig() {
        return new ResourceServiceConfig() {
        };
    }
}
