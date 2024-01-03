
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


import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.structured.api.quarkus.dataAccess.PrimaryKey;
import org.structured.api.quarkus.reflection.ClassReflector;
import org.structured.api.quarkus.reflection.FieldReflector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractResourceServiceImplTest<T extends PrimaryKey<K>, K> extends AbstractResourceServiceTest<T, K> {

    private final List<T> insertData;
    private final List<T> updateData;
    private final FieldReflector<T, String> fieldReflector;
    private final String stringField;

    public AbstractResourceServiceImplTest(Class<T> clazz, String path, List<T> insertData, List<T> updateData, String stringField) {
        super(clazz, path);
        assertEquals(insertData.size(), updateData.size(), " Please use two data lists of equal size!");
        this.insertData = insertData;
        this.updateData = updateData;
        if (stringField != null) {
            this.fieldReflector = ClassReflector.ofClass(this.clazz)
                                                .getReflector(stringField, String.class);
            this.stringField = stringField;
        } else {
            this.fieldReflector = null;
            this.stringField = null;
        }
    }

    @BeforeAll
    public static void beforeAll() {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    @Test
    @Order(10)
    public void testPost() {
        for (int index = 0; index < insertData.size(); index++) {

            //given
            T req = this.insertData.get(index);
            assertNotNull(req);
            assertNull(req.getId());

            //when
            T res = this.post(req);

            //then
            assertNotNull(res);
            K id = res.getId();
            assertNotNull(id);
            req.setId(id);
            assertEquals(req, res);
            T upd = this.updateData.get(index);
            assertNotNull(upd);
            upd.setId(id);
        }
    }


    @Test
    @Order(20)
    public void testGet() {
        for (int index = 0; index < insertData.size(); index++) {

            //given
            T req = this.insertData.get(index);
            assertNotNull(req);
            K id = req.getId();
            assertNotNull(id);

            //when
            T res = get(id);

            //then
            assertNotNull(res);
            assertNotNull(res.getId());
            assertEquals(req.getId(), res.getId());
            assertEquals(req, res);
        }
    }

    @Test
    @Order(21)
    public void testGetAllAsList() {
        //when
        List<T> res = this.getAllAsList();

        //then
        assertNotNull(res);
        assertEquals(this.insertData.size(), res.size());
        assertEquals(this.insertData, res);
    }

    @Test
    @Order(22)
    public void testGetByIdsAsList() {
        //given
        List<K> ids = this.insertData.stream()
                                     .map(PrimaryKey::getId)
                                     .collect(Collectors.toList());

        //when
        List<T> res = this.getByIdsAsList(ids);

        //then
        assertNotNull(res);
        assertEquals(this.insertData.size(), res.size());
        assertEquals(this.insertData, res);
    }

    @Test
    @Order(23)
    public void testPostByIdsAsList() {
        //given
        List<K> ids = this.insertData.stream()
                                     .map(PrimaryKey::getId)
                                     .collect(Collectors.toList());

        //when
        List<T> res = this.postByIdsAsList(ids);

        //then
        assertNotNull(res);
        assertEquals(this.insertData.size(), res.size());
        assertEquals(this.insertData, res);
    }


    @Test
    @Order(30)
    public void testPut() {
        for (int index = 0; index < updateData.size(); index++) {

            //given
            T req = this.updateData.get(index);
            assertNotNull(req);
            K id = req.getId();
            assertNotNull(id);

            //when
            T res = put(req);

            //then
            assertNotNull(res);
            assertNotNull(res.getId());
            assertEquals(req, res);
        }
    }

    @Test
    @Order(40)
    public void testDelete() {
        for (int index = 0; index < updateData.size(); index++) {

            //given
            T req = this.updateData.get(index);
            assertNotNull(req);
            K id = req.getId();
            assertNotNull(id);

            //when
            this.delete(id);
        }

        //then
        Assertions.assertTrue(this.getAllAsList()
                                  .isEmpty());
    }


    @Test
    @Order(50)
    public void testPostListAsList() {

        //when
        List<T> res = postListAsList(this.insertData);

        //then
        assertNotNull(res);
        assertEquals(this.insertData.size(), res.size());
        for (int index = 0; index < updateData.size(); index++) {
            this.insertData.get(index)
                           .setId(res.get(index)
                                     .getId());
            this.updateData.get(index)
                           .setId(res.get(index)
                                     .getId());
        }
        assertEquals(this.insertData, res);
        assertEquals(this.insertData, this.getAllAsList());
    }

    @Test
    @Order(60)
    public void testPutListAsList() {

        //when
        List<T> res = this.putListAsList(this.updateData);

        //then
        assertNotNull(res);
        assertEquals(this.updateData.size(), res.size());
        assertEquals(this.updateData, res);
        assertEquals(this.updateData, this.getAllAsList());
    }

    @Test
    @Order(70)
    public void testDeleteByIds() {
        //given
        List<K> ids = this.getAllAsList()
                          .stream()
                          .map(PrimaryKey::getId)
                          .collect(Collectors.toList());
        assertFalse(ids.isEmpty());

        //when
        this.deleteByIds(ids);

        //then
        Assertions.assertTrue(this.getAllAsList()
                                  .isEmpty());
    }

    @Test
    @Order(80)
    public void testDeleteByIdsInPath() {
        //given
        this.testPostListAsList();
        List<K> ids = this.getAllAsList()
                          .stream()
                          .map(PrimaryKey::getId)
                          .collect(Collectors.toList());
        assertFalse(ids.isEmpty());
        //when
        this.deleteByIdsInPath(ids);
        //then
        Assertions.assertTrue(this.getAllAsList()
                                  .isEmpty());
    }


    @Test
    @Order(90)
    public void testGetFilterStringFieldEqualsValueAsList() {
        //given
        if (this.stringField == null) return;
        List<T> insertedData = this.postListAsList(this.insertData);
        assertEquals(this.insertData.size(), this.getAllAsList()
                                                 .size());

        for (int index = 0; index < insertedData.size(); index++) {
            T source = insertedData.get(index);
            String value = this.fieldReflector.get(source);

            //when
            List<T> res = this.getFilterStringFieldEqualsValueAsList(this.stringField, value);

            //then
            assertNotNull(res);
            assertEquals(1, res.size());
            assertEquals(source, res.get(0));
        }
        this.deleteAll(insertedData.stream()
                                   .map(PrimaryKey::getId)
                                   .collect(Collectors.toList()));
    }

    @Test
    @Order(100)
    public void testGetFilterStringFieldLikeValueAsList() {
        if (this.stringField == null) return;

        //given
        List<T> insertedData = this.postListAsList(this.insertData);
        assertEquals(this.insertData.size(), this.getAllAsList()
                                                 .size());

        for (int index = 0; index < insertedData.size(); index++) {
            T source = insertedData.get(index);
            String value = this.fieldReflector.get(source);

            //when
            List<T> res = this.getFilterStringFieldLikeValueAsList(stringField, value);

            //then
            assertNotNull(res);
            assertEquals(1, res.size());
            assertEquals(source, res.get(0));
        }
        this.deleteAll(insertedData.stream()
                                   .map(PrimaryKey::getId)
                                   .collect(Collectors.toList()));
    }

    @Test
    @Order(110)
    public void testGetFilterStringFieldInValuesAsList() {
        if (this.stringField == null) return;
        //given
        List<T> insertedData = this.postListAsList(this.insertData);
        assertEquals(this.insertData.size(), this.getAllAsList()
                                                 .size());

        List<String> values = insertedData.stream()
                                          .map(this.fieldReflector::get)
                                          .collect(Collectors.toList());
        //when
        List<T> res = this.getFilterStringFieldInValuesAsList(stringField, values);

        //then
        assertNotNull(res);
        assertEquals(insertedData.size(), res.size());
        this.deleteAll(insertedData.stream()
                                   .map(PrimaryKey::getId)
                                   .collect(Collectors.toList()));
    }


    @Test
    @Order(120)
    public void testGetAutocompleteStringFieldLikeValueAsSortedSet() {
        if (this.stringField == null) return;
        //given
        List<T> insertedData = this.postListAsList(this.insertData);
        assertEquals(this.insertData.size(), this.getAllAsList()
                                                 .size());

        List<String> values = insertedData.stream()
                                          .map(this.fieldReflector::get)
                                          .collect(Collectors.toList());
        for (String value : values) {
            Set<String> expected = values.stream()
                                         .filter(v -> v.startsWith(value))
                                         .collect(Collectors.toSet());
            //when
            Set<String> res = new HashSet<>(this.getAutocompleteStringFieldLikeValueAsSortedSet(stringField, value));

            //then
            assertNotNull(res);
            assertEquals(expected.size(), res.size());
            assertEquals(expected, res);
        }

        this.deleteAll(insertedData.stream()
                                   .map(PrimaryKey::getId)
                                   .collect(Collectors.toList()));
    }

    @Test
    @Order(130)
    public void testPostFilterContentEqualsAsList() {
        //given
        List<T> insertedData = this.postListAsList(this.insertData);
        assertEquals(this.insertData.size(), this.getAllAsList()
                                                 .size());
        for (T value : insertedData) {
            K id = value.getId();
            value.setId(null);

            //when
            List<T> res = postFilterContentEqualsAsList(value);

            //then
            assertNotNull(res);
            assertEquals(1, res.size());
            value.setId(id);
            assertEquals(value, res.get(0));
        }

        this.deleteAll(insertedData.stream()
                                   .map(PrimaryKey::getId)
                                   .collect(Collectors.toList()));
    }


    @Test
    @Order(140)
    public void postFilterContentInAsList() {
        //given
        List<T> insertedData = this.postListAsList(this.insertData);
        assertEquals(this.insertData.size(), this.getAllAsList()
                                                 .size());
        for (T value : insertedData) {
            K id = value.getId();
            value.setId(null);
            List<T> values = List.of(value);

            //when
            List<T> res = this.postFilterContentInAsList(values);

            //then
            assertNotNull(res);
            assertEquals(1, res.size());
            value.setId(id);
            assertEquals(value, res.get(0));
        }


        this.deleteAll(insertedData.stream()
                                   .map(PrimaryKey::getId)
                                   .collect(Collectors.toList()));
    }

    protected void deleteAll(List<K> ids) {
        given().contentType(ContentType.JSON)
               .body(ids)
               .when()
               .accept(ContentType.JSON)
               .delete(this.path + "/byIds")
               .then()
               .statusCode(204);
    }
}
