package org.structured.api.quarkus.rest;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.structured.api.quarkus.dao.DataAccess;
import org.structured.api.quarkus.dao.PrimaryKey;

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
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/get/list/asList")
    public List<T> getListAsList(List<K> ids) {
        return getDataAccess().streamByIds(ids).collect(Collectors.toList());
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