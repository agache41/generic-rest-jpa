package org.structured.api.quarkus.rest;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.structured.api.quarkus.dao.DataAccess;
import org.structured.api.quarkus.dao.PrimaryKey;
import org.structured.api.quarkus.reflection.ClassReflector;
import org.structured.api.quarkus.reflection.FieldReflector;

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
public abstract class Resource<T extends PrimaryKey<K>, K> {

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
    protected DataAccess<T, K> getDataAccess() {
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
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/all/asList")
    public List<T> getAllAsList() {
        return getDataAccess().streamAll().collect(Collectors.toList());
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
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/byIds/{ids:.*}")
    public List<T> getByIdsAsList(@PathParam("ids") List<K> ids) {
        return getDataAccess().streamByIds(ids).collect(Collectors.toList());
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
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/byIds/asList")
    public List<T> postByIdsAsList(List<K> ids) {
        return getDataAccess().streamByIds(ids).collect(Collectors.toList());
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
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/filter/{stringField}/equals/{value}/asList")
    public List<T> getFilterStringFieldEqualsValueAsList(@PathParam("stringField") String stringField, @PathParam("value") String value) {
        return getDataAccess().streamByColumnEqualsValue(stringField, value).collect(Collectors.toList());
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
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/filter/{stringField}/like/{value}/asList")
    public List<T> getFilterStringFieldLikeValueAsList(@PathParam("stringField") String stringField, @PathParam("value") String value) {
        return getDataAccess().streamByColumnLikeValue(stringField, value).collect(Collectors.toList());
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
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("autocomplete/{stringField}/like/{value}")
    public List<String> getAutocompleteStringFieldLikeValueAsList(@PathParam("stringField") String stringField, @PathParam("value") String value) {
        FieldReflector<T> fieldReflector = ClassReflector.ofClass(dataAccess.getType()).getReflector(stringField);
        return getDataAccess()
                .streamByColumnLikeValue(stringField, value)
                .map(entity -> (String) fieldReflector.get(entity))
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
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/list/asList")
    public List<T> postListAsList(List<T> sources) {
        return getDataAccess().mergeAll(sources).collect(Collectors.toList());
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
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/list/asList")
    public List<T> putListAsList(List<T> sources) {
        return getDataAccess().updateByIds(sources).collect(Collectors.toList());
    }

    /**
     * <pre>
     * Deletes the entity for the given id.
     * </pre>
     *
     * @param id the id
     */
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
    @DELETE
    @Path("/list")
    public void deleteList(List<K> ids) {
        getDataAccess().removeByIds(ids);
    }


}