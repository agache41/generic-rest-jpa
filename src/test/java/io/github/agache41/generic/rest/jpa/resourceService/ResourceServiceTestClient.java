
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
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ResourceServiceTestClient<T extends PrimaryKey<K>, K> implements ResourceService<T, K> {

    static {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    protected final Class<T> clazz;
    protected final String path;


    public ResourceServiceTestClient(final Class<T> clazz,
                                     final String path) {
        assertNotNull(clazz, " Please provide a class !");
        this.clazz = clazz;
        assertFalse(path == null || path.isEmpty(), " Please provide a ResourceService Path !");
        this.path = path;
    }

    @Override
    public T get(final K id) {
        return given().contentType(ContentType.JSON)
                      .when()
                      .accept(ContentType.JSON)
                      .get(this.path + "/{id}", id)
                      .then()
                      .statusCode(200)
                      .extract()
                      .body()
                      .as(this.clazz);
    }

    @Override
    public List<T> getAllAsList(final Integer firstResult,
                                final Integer maxResults) {
        return given().contentType(ContentType.JSON)
                      .when()
                      .accept(ContentType.JSON)
                      .get(this.path + "/all/asList")
                      .then()
                      .statusCode(200)
                      .extract()
                      .body()
                      .jsonPath()
                      .getList(".", this.clazz);
    }

    @Override
    public List<T> getByIdsAsList(final List<K> ids) {
        return given().contentType(ContentType.JSON)
                      .when()
                      .accept(ContentType.JSON)
                      .get(this.path + "/byIds/{ids}/asList", ids)
                      .then()
                      .statusCode(200)
                      .extract()
                      .body()
                      .jsonPath()
                      .getList(".", this.clazz);
    }

    @Override
    public List<T> postByIdsAsList(final List<K> ids) {
        return given().contentType(ContentType.JSON)
                      .body(ids)
                      .when()
                      .accept(ContentType.JSON)
                      .post(this.path + "/byIds/asList")
                      .then()
                      .statusCode(200)
                      .extract()
                      .body()
                      .jsonPath()
                      .getList(".", this.clazz);
    }

    @Override
    public List<T> getFilterStringFieldEqualsValueAsList(final String stringField,
                                                         final String value,
                                                         final Integer firstResult,
                                                         final Integer maxResults) {
        return given().contentType(ContentType.JSON)
                      .when()
                      .accept(ContentType.JSON)
                      .get(this.path + "/filter/{stringField}/equals/{value}/asList", stringField, value)
                      .then()
                      .statusCode(200)
                      .extract()
                      .body()
                      .jsonPath()
                      .getList(".", this.clazz);
    }

    @Override
    public List<T> getFilterStringFieldLikeValueAsList(final String stringField,
                                                       final String value,
                                                       final Integer firstResult,
                                                       final Integer maxResults) {
        return given().contentType(ContentType.JSON)
                      .when()
                      .accept(ContentType.JSON)
                      .get(this.path + "/filter/{stringField}/like/{value}/asList", stringField, value)
                      .then()
                      .statusCode(200)
                      .extract()
                      .body()
                      .jsonPath()
                      .getList(".", this.clazz);
    }

    @Override
    public List<T> getFilterStringFieldInValuesAsList(final String stringField,
                                                      final List<String> values,
                                                      final Integer firstResult,
                                                      final Integer maxResults) {
        return given().contentType(ContentType.JSON)
                      .when()
                      .accept(ContentType.JSON)
                      .get(this.path + "/filter/{stringField}/in/{values}/asList", stringField, values)
                      .then()
                      .statusCode(200)
                      .extract()
                      .body()
                      .jsonPath()
                      .getList(".", this.clazz);
    }

    @Override
    public List<String> getAutocompleteStringFieldLikeValueAsSortedSet(final String stringField,
                                                                       final String value,
                                                                       final Integer cut,
                                                                       final Integer maxResults) {
        return given().contentType(ContentType.JSON)
                      .when()
                      .accept(ContentType.JSON)
                      .get(this.path + "/autocomplete/{stringField}/like/{value}/asSortedSet", stringField, value)
                      .then()
                      .statusCode(200)
                      .extract()
                      .body()
                      .jsonPath()
                      .getList(".", String.class);
    }

    @Override
    public List<IdGroup<K>> getAutocompleteIdsStringFieldLikeValueAsList(final String stringField,
                                                                         final String value,
                                                                         final Integer cut,
                                                                         final Integer maxResults) {
        return given().contentType(ContentType.JSON)
                      .when()
                      .accept(ContentType.JSON)
                      .get(this.path + "/autocompleteIds/{stringField}/like/{value}/asList", stringField, value)
                      .then()
                      .statusCode(200)
                      .extract()
                      .body()
                      .jsonPath()
                      .getList(".");
    }

    @Override
    public List<T> postFilterContentEqualsAsList(final T value,
                                                 final Integer firstResult,
                                                 final Integer maxResults) {
        return given().contentType(ContentType.JSON)
                      .body(value)
                      .when()
                      .accept(ContentType.JSON)
                      .post(this.path + "/filter/content/equals/value/asList")
                      .then()
                      .statusCode(200)
                      .extract()
                      .body()
                      .jsonPath()
                      .getList(".", this.clazz);
    }

    @Override
    public List<T> postFilterContentInAsList(final List<T> values,
                                             final Integer firstResult,
                                             final Integer maxResults) {
        return given().contentType(ContentType.JSON)
                      .body(values)
                      .when()
                      .accept(ContentType.JSON)
                      .post(this.path + "/filter/content/in/values/asList")
                      .then()
                      .statusCode(200)
                      .extract()
                      .body()
                      .jsonPath()
                      .getList(".", this.clazz);
    }

    @Override
    public T post(final T source) {
        return given().contentType(ContentType.JSON)
                      .body(source)
                      .when()
                      .accept(ContentType.JSON)
                      .post(this.path)
                      .then()
                      .statusCode(200)
                      .extract()
                      .body()
                      .as(this.clazz);
    }

    @Override
    public List<T> postListAsList(final List<T> sources) {
        return given().contentType(ContentType.JSON)
                      .body(sources)
                      .when()
                      .accept(ContentType.JSON)
                      .post(this.path + "/list/asList")
                      .then()
                      .statusCode(200)
                      .extract()
                      .jsonPath()
                      .getList(".", this.clazz);
    }

    @Override
    public T put(final T source) {
        return given().contentType(ContentType.JSON)
                      .body(source)
                      .when()
                      .accept(ContentType.JSON)
                      .put(this.path)
                      .then()
                      .statusCode(200)
                      .extract()
                      .body()
                      .as(this.clazz);
    }

    @Override
    public List<T> putListAsList(final List<T> sources) {
        return given().contentType(ContentType.JSON)
                      .body(sources)
                      .when()
                      .accept(ContentType.JSON)
                      .put(this.path + "/list/asList")
                      .then()
                      .statusCode(200)
                      .extract()
                      .jsonPath()
                      .getList(".", this.clazz);
    }

    @Override
    public void delete(final K id) {
        given().contentType(ContentType.JSON)
               .when()
               .accept(ContentType.JSON)
               .delete(this.path + "/{id}", id)
               .then()
               .statusCode(204);
    }

    @Override
    public void deleteByIds(final List<K> ids) {
        given().contentType(ContentType.JSON)
               .body(ids)
               .when()
               .accept(ContentType.JSON)
               .delete(this.path + "/byIds")
               .then()
               .statusCode(204);
    }

    @Override
    public void deleteByIdsInPath(final List<K> ids) {
        given().contentType(ContentType.JSON)
               .body(ids)
               .when()
               .accept(ContentType.JSON)
               .delete(this.path + "/byIds/{ids}", ids)
               .then()
               .statusCode(204);
    }
}
