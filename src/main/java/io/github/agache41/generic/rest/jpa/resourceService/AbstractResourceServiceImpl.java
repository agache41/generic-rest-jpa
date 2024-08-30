
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

import io.github.agache41.generic.rest.jpa.dataAccess.DataAccess;
import io.github.agache41.generic.rest.jpa.dataAccess.DataBinder;
import io.github.agache41.generic.rest.jpa.dataAccess.IdGroup;
import io.github.agache41.generic.rest.jpa.dataAccess.PrimaryKey;
import io.github.agache41.generic.rest.jpa.update.TransferObject;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * <pre>
 * Base class for resource REST APIs.
 * The class implements methods for basic REST operations on the underlying Class
 * </pre>
 *
 * @param <TO>     the type parameter
 * @param <ENTITY> the type parameter
 * @param <PK>     the type parameter
 */
public abstract class AbstractResourceServiceImpl<TO extends PrimaryKey<PK> & TransferObject<TO, ENTITY>, ENTITY extends PrimaryKey<PK>, PK> implements ResourceService<TO, PK> {


    /**
     * The data binder handling the binding between TO and ENTITY
     */
    @Inject
    protected DataBinder<TO, ENTITY, PK> dataBinder;


    /**
     * {@inheritDoc}
     */
    @Override
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public TO get(@PathParam("id") final PK id) {
        return this.getDataBinder()
                   .findById(id);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/byId")
    public TO postById(final PK id) {
        return this.get(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/all/asList")
    public List<TO> getAllAsList(@QueryParam("firstResult") final Integer firstResult,
                                 @QueryParam("maxResults") final Integer maxResults,
                                 @Context final UriInfo uriInfo) {
        return this.getDataBinder()
                   .listAll(this.getConfig()
                                .getFirstResult(firstResult), this.getConfig()
                                                                  .getMaxResults(maxResults), uriInfo);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/byIds/{ids}/asList")
    public List<TO> getByIdsAsList(@PathParam("ids") final List<PK> ids) {
        return this.getDataBinder()
                   .listByIds(ids);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/byIds/asList")
    public List<TO> postByIdsAsList(final List<PK> ids) {
        return this.getByIdsAsList(ids);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/filter/{stringField}/equals/{value}/asList")
    public List<TO> getFilterStringFieldEqualsValueAsList(@PathParam("stringField") final String stringField,
                                                          @PathParam("value") final String value,
                                                          @QueryParam("firstResult") final Integer firstResult,
                                                          @QueryParam("maxResults") final Integer maxResults) {
        return this.getDataBinder()
                   .listByColumnEqualsValue(stringField, value, this.getConfig()
                                                                    .getFirstResult(firstResult), this.getConfig()
                                                                                                      .getMaxResults(maxResults));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/filter/{stringField}/like/{value}/asList")
    public List<TO> getFilterStringFieldLikeValueAsList(@PathParam("stringField") final String stringField,
                                                        @PathParam("value") final String value,
                                                        @QueryParam("firstResult") final Integer firstResult,
                                                        @QueryParam("maxResults") final Integer maxResults) {
        return this.getDataBinder()
                   .listByColumnLikeValue(stringField, value, this.getConfig()
                                                                  .getFirstResult(firstResult), this.getConfig()
                                                                                                    .getMaxResults(maxResults));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/filter/{stringField}/in/{values}/asList")
    public List<TO> getFilterStringFieldInValuesAsList(@PathParam("stringField") final String stringField,
                                                       @PathParam("values") final List<String> values,
                                                       @QueryParam("firstResult") final Integer firstResult,
                                                       @QueryParam("maxResults") final Integer maxResults) {
        return this.getDataBinder()
                   .listByColumnInValues(stringField, values, this.getConfig()
                                                                  .getFirstResult(firstResult), this.getConfig()
                                                                                                    .getMaxResults(maxResults));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("autocomplete/{stringField}/like/{value}/asSortedSet")
    public List<String> getAutocompleteStringFieldLikeValueAsSortedSet(@PathParam("stringField") final String stringField,
                                                                       @PathParam("value") final String value,
                                                                       @QueryParam("cut") final Integer cut,
                                                                       @QueryParam("maxResults") final Integer maxResults,
                                                                       @Context final UriInfo uriInfo) {
        if (value == null || value.length() < this.getConfig()
                                                  .getAutocompleteCut(cut)) {
            return Collections.emptyList();
        }
        return this.getDataAccess()
                   .autocompleteByColumnLikeValue(stringField, value, this.getConfig()
                                                                          .getAutocompleteMaxResults(maxResults), uriInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("autocompleteIds/{stringField}/like/{value}/asList")
    public List<IdGroup<PK>> getAutocompleteIdsStringFieldLikeValueAsList(@PathParam("stringField") final String stringField,
                                                                          @PathParam("value") final String value,
                                                                          @QueryParam("cut") final Integer cut,
                                                                          @QueryParam("maxResults") final Integer maxResults,
                                                                          @Context final UriInfo uriInfo) {
        if (value == null || value.length() < this.getConfig()
                                                  .getAutocompleteCut(cut)) {
            return Collections.emptyList();
        }
        return this.getDataAccess()
                   .autocompleteIdsByColumnLikeValue(stringField, value, this.getConfig()
                                                                             .getAutocompleteMaxResults(maxResults), uriInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/filter/content/equals/value/asList")
    public List<TO> postFilterContentEqualsAsList(final Map<String, Object> value,
                                                  @QueryParam("firstResult") final Integer firstResult,
                                                  @QueryParam("maxResults") final Integer maxResults) {
        return this.getDataBinder()
                   .listByContentEquals(value, this.getConfig()
                                                   .getFirstResult(firstResult), this.getConfig()
                                                                                     .getMaxResults(maxResults));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/filter/content/in/values/asList")
    public List<TO> postFilterContentInAsList(final Map<String, List<Object>> values,
                                              @QueryParam("firstResult") final Integer firstResult,
                                              @QueryParam("maxResults") final Integer maxResults) {
        return this.getDataBinder()
                   .listByContentInValues(values, this.getConfig()
                                                      .getFirstResult(firstResult), this.getConfig()
                                                                                        .getMaxResults(maxResults));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TO post(final TO to) {
        return this.getDataBinder()
                   .persist(to);
        //return this.doVerify(inserted);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/list/asList")
    public List<TO> postListAsList(final List<TO> toList) {
        return this.getDataBinder()
                   .persist(toList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TO put(final TO to) {
        return this.getDataBinder()
                   .updateById(to);
        //return this.doVerify(updated);
    }

//    /**
//     * Verifies that the updated Entity has the same content in the database.
//     *
//     * @param updated the updated
//     * @return the t
//     */
//    protected TO doVerify(final TO updated) {
//        if (!this.getConfig()
//                 .getVerify()) {
//            return updated;
//        }
//        final PK id = updated.getId();
//        if (id == null) {
//            throw new RuntimeException(" Verify fail " + updated + " has null id! ");
//        }
//        final TO actual = this.getDataAccess()
//                              .findById(id);
//        if (!updated.equals(actual)) {
//            throw new RuntimeException(" Verify fail " + updated + " <> " + actual);
//        }
//        return actual;
//    }
//
//    /**
//     * Does verify methode on a list.
//     *
//     * @param updated the updated
//     * @return the list
//     */
//    protected List<TO> doVerify(final List<TO> updated) {
//        return updated.stream()
//                      .map(this::doVerify)
//                      .collect(Collectors.toList());
//    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/list/asList")
    public List<TO> putListAsList(final List<TO> toList) {
        return this.getDataBinder()
                   .updateByIds(toList, true);
        //return this.doVerify(updated);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") final PK id) {
        this.getDataAccess()
            .removeById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @DELETE
    @Path("/byIds")
    public void deleteByIds(final List<PK> ids) {
        this.getDataAccess()
            .removeByIds(ids);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @DELETE
    @Path("/byIds/{ids}")
    public void deleteByIdsInPath(@PathParam("ids") final List<PK> ids) {
        this.getDataAccess()
            .removeByIds(ids);
    }

    /**
     * The data binder getter
     *
     * @return the data binder
     */
    public DataBinder<TO, ENTITY, PK> getDataBinder() {
        return this.dataBinder;
    }

    /**
     * The data access getter
     *
     * @return the data binder
     */
    public DataAccess<ENTITY, PK> getDataAccess() {
        return this.getDataBinder()
                   .getDataAccess();
    }
}