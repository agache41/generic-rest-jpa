package org.structured.api.quarkus.rest;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
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
@Transactional
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
     * Updates an existing entity by id.
     * The Entity with the given id must exist in the Database or a UnexpectedException is thrown.
     * </pre>
     *
     * @param source the source
     * @return the t
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public T put(T source) {
        return getDataAccess().updateById(source);
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
     * Returns all the entities for the given table.
     * </pre>
     *
     * @return the list of entities
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/all")
    public List<T> getAll( ){
        return getDataAccess().streamAll().collect(Collectors.toList());
    }

}