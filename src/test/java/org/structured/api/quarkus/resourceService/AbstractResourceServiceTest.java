package org.structured.api.quarkus.resourceService;

import io.restassured.http.ContentType;
import org.structured.api.quarkus.dataAccess.PrimaryKey;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AbstractResourceServiceTest<T extends PrimaryKey<K>, K> implements ResourceService<T, K> {

    protected final Class<T> clazz;
    protected final String path;

    public AbstractResourceServiceTest(Class<T> clazz, String path) {
        assertNotNull(clazz, " Please provide a class !");
        this.clazz = clazz;
        assertFalse(path == null || path.isEmpty(), " Please provide a ResourceService Path !");
        this.path = path;
    }

    @Override
    public T get(K id) {
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
    public List<T> getAllAsList() {
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
    public List<T> getByIdsAsList(List<K> ids) {
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
    public List<T> postByIdsAsList(List<K> ids) {
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
    public List<T> getFilterStringFieldEqualsValueAsList(String stringField, String value) {
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
    public List<T> getFilterStringFieldLikeValueAsList(String stringField, String value) {
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
    public List<T> getFilterStringFieldInValuesAsList(String stringField, List<String> values) {
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
    public List<String> getAutocompleteStringFieldLikeValueAsSortedSet(String stringField, String value) {
        return given().contentType(ContentType.JSON)
                      .when()
                      .accept(ContentType.JSON)
                      .get(this.path + "/autocomplete/{stringField}/like/{value}/asSortedSet", stringField, value + "%")
                      .then()
                      .statusCode(200)
                      .extract()
                      .body()
                      .jsonPath()
                      .getList(".", String.class);
    }

    @Override
    public List<T> postFilterContentEqualsAsList(T value) {
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
    public List<T> postFilterContentInAsList(List<T> values) {
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
    public T post(T source) {
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
    public List<T> postListAsList(List<T> sources) {
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
    public T put(T source) {
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
    public List<T> putListAsList(List<T> sources) {
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
    public void delete(K id) {
        given().contentType(ContentType.JSON)
               .when()
               .accept(ContentType.JSON)
               .delete(this.path + "/{id}", id)
               .then()
               .statusCode(204);
    }

    @Override
    public void deleteByIds(List<K> ids) {
        given().contentType(ContentType.JSON)
               .body(ids)
               .when()
               .accept(ContentType.JSON)
               .delete(this.path + "/byIds")
               .then()
               .statusCode(204);
    }

    @Override
    public void deleteByIdsInPath(List<K> ids) {
        given().contentType(ContentType.JSON)
               .body(ids)
               .when()
               .accept(ContentType.JSON)
               .delete(this.path + "/byIds/{ids}", ids)
               .then()
               .statusCode(204);
    }
}
