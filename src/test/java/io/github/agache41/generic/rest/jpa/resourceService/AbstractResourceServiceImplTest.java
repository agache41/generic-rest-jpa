
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
import io.github.agache41.generic.rest.jpa.producer.Producer;
import io.github.agache41.generic.rest.jpa.update.Updatable;
import io.github.agache41.generic.rest.jpa.update.reflector.ClassReflector;
import io.github.agache41.generic.rest.jpa.update.reflector.FieldReflector;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;


@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractResourceServiceImplTest<T extends PrimaryKey<K> & Updatable<T>, K> {


    private static final Logger LOG = Logger.getLogger(AbstractResourceServiceImplTest.class);
    protected final Class<T> clazz;
    protected final List<T> insertData;
    protected final List<T> updateData;
    protected final ClassReflector<T> classReflector;

    protected final Producer<T> producer;
    protected final FieldReflector<T, String> fieldReflector;
    protected final String stringField;
    protected final ResourceService<T, K> client;
    protected final ResourceServiceConfig config = new ResourceServiceConfig() {
    };

    public AbstractResourceServiceImplTest(final Class<T> clazz,
                                           final String path,
                                           final List<T> insertData,
                                           final List<T> updateData,
                                           final String stringField) {
        this(new ResourceServiceTestClient<>(clazz, path), clazz, insertData, updateData, stringField);
    }

    public AbstractResourceServiceImplTest(final ResourceService<T, K> client,
                                           final Class<T> clazz,
                                           final List<T> insertData,
                                           final List<T> updateData,
                                           final String stringField) {
        this.clazz = clazz;
        this.client = client;
        assertEquals(insertData.size(), updateData.size(), " Please use two data lists of equal size!");
        this.insertData = insertData;
        this.updateData = updateData;
        this.classReflector = ClassReflector.ofClass(clazz);
        this.producer = Producer.ofClass(clazz);
        if (stringField != null) {
            this.fieldReflector = ClassReflector.ofClass(clazz)
                                                .getReflector(stringField, String.class);
            this.stringField = stringField;
        } else {
            this.fieldReflector = null;
            this.stringField = null;
        }
    }


    public ResourceService<T, K> getClient() {
        return this.client;
    }

    @Test
    @Order(10)
    public void testPost() {
        for (int index = 0; index < this.insertData.size(); index++) {

            //given
            final T req = this.insertData.get(index);
            assertNotNull(req);
            //only to be checked when id is not updateable
            //assertNull(req.getId());
            //when
            LOG.debugf("POST: Request: %s", req);
            final T res = this.getClient()
                              .post(req);
            LOG.debugf("POST: Response: %s", res);
            //then
            assertNotNull(res);
            final K id = res.getId();
            assertNotNull(id);
            //req.setId(id);
            assertEquals(req, res);
            this.insertData.set(index, res);
            final T upd = this.updateData.get(index);
            assertNotNull(upd);
            upd.setId(id);
        }
    }

    @Test
    @Order(20)
    public void testGet() {
        for (final T req : this.insertData) {

            //given
            assertNotNull(req);
            final K id = req.getId();
            assertNotNull(id);

            //when
            LOG.debugf("GET: Request: %s", id);
            final T res = this.getClient()
                              .get(id);
            LOG.debugf("GET: Response: %s", res);
            //then
            assertNotNull(res);
            assertNotNull(res.getId());
            assertEquals(req.getId(), res.getId());
            assertEquals(req, res);
        }
    }

    @Test
    @Order(25)
    public void testPostById() {
        for (final T req : this.insertData) {

            //given
            assertNotNull(req);
            final K id = req.getId();
            assertNotNull(id);

            //when
            LOG.debugf("GET: Request: %s", id);
            final T res = this.getClient()
                              .postById(id);
            LOG.debugf("GET: Response: %s", res);
            //then
            assertNotNull(res);
            assertNotNull(res.getId());
            assertEquals(req.getId(), res.getId());
            assertEquals(req, res);
        }
    }

    @Test
    @Order(26)
    public void testGetAllAsList() {
        //when
        final List<T> res = this.getAll();

        //then
        assertNotNull(res);
        assertEquals(this.insertData.size(), res.size());
        assertThat(this.insertData).hasSameElementsAs(res);
    }

    @Test
    @Order(27)
    public void testGetByIdsAsList() {
        //given
        final List<K> ids = this.insertData.stream()
                                           .map(PrimaryKey::getId)
                                           .collect(Collectors.toList());

        //when
        final List<T> res = this.getClient()
                                .getByIdsAsList(ids);


        //then
        assertNotNull(res);
        assertEquals(this.insertData.size(), res.size());
        assertThat(this.insertData).hasSameElementsAs(res);
    }

    @Test
    @Order(28)
    public void testPostByIdsAsList() {
        //given
        final List<K> ids = this.insertData.stream()
                                           .map(PrimaryKey::getId)
                                           .collect(Collectors.toList());

        //when
        final List<T> res = this.getClient()
                                .postByIdsAsList(ids);

        //then
        assertNotNull(res);
        assertEquals(this.insertData.size(), res.size());
        assertThat(this.insertData).hasSameElementsAs(res);
    }


    @Test
    @Order(30)
    public void testPut() {
        for (final T req : this.updateData) {

            //given
            assertNotNull(req);
            final K id = req.getId();
            assertNotNull(id);

            //when
            final T res = this.getClient()
                              .put(req);

            //then
            assertNotNull(res);
            assertNotNull(res.getId());
            assertEquals(id, res.getId());
            assertEquals(req, res);

            final T getres = this.getClient()
                                 .get(id);
            //then
            assertNotNull(getres);
            assertNotNull(getres.getId());
            assertEquals(id, getres.getId());
            assertEquals(req, getres);
        }
    }

    @Test
    @Order(33)
    public void testPutEachField() {
        for (final T req : this.updateData) {

            //given
            assertNotNull(req);
            final K id = req.getId();
            assertNotNull(id);

            //iterate over the values in the object
            for (final FieldReflector reflector : this.classReflector.getValueReflectorsArray()) {
                if (!reflector.isUpdatable() || reflector.isId()) {
                    continue;
                }
                // create new empty object
                final T feldReq = this.classReflector.newInstance();
                // set the id
                feldReq.setId(id);

                for (int repeatField = 2; repeatField > 0; repeatField--) {
                    // set only this field
                    final Object value = this.producer.produceField(feldReq, reflector);
                    //when
                    final T feldRes = this.getClient()
                                          .put(feldReq);
                    //then
                    assertNotNull(feldRes);
                    assertNotNull(feldRes.getId());
                    assertEquals(id, feldRes.getId());
                    assertEquals(reflector.get(feldReq), reflector.get(feldRes), " Checking field " + reflector.getName());
                    assertEquals(value, reflector.get(feldRes), " Checking field " + reflector.getName());

                    final T getRes = this.getClient()
                                         .get(id);
                    //then
                    assertNotNull(getRes);
                    assertNotNull(getRes.getId());
                    assertEquals(id, getRes.getId());
                    assertEquals(reflector.get(feldReq), reflector.get(getRes), " Checking field " + reflector.getName());
                    assertEquals(value, reflector.get(getRes), " Checking field " + reflector.getName());
                }
            }
        }
        this.deleteAll();
    }

    @Test
    @Order(34)
    public void testPostEachField() {

        //iterate over the values in the object
        for (final FieldReflector reflector : this.classReflector.getValueReflectorsArray()) {
            if (!reflector.isUpdatable() || reflector.isId()) {
                continue;
            }
            // create new empty object
            final T feldReq = this.producer.produceMinimal();
            //then
            assertNotNull(feldReq);
            // set only this field
            final Object value = this.producer.produceField(feldReq, reflector);
            //when
            final T feldRes = this.getClient()
                                  .post(feldReq);
            //then
            assertNotNull(feldRes);
            final K id = feldRes.getId();
            assertNotNull(id);

            assertEquals(reflector.get(feldReq), reflector.get(feldRes), " Checking field " + reflector.getName());
            assertEquals(value, reflector.get(feldRes), " Checking field " + reflector.getName());

            final T getRes = this.getClient()
                                 .get(id);
            //then
            assertNotNull(getRes);
            assertNotNull(getRes.getId());
            assertEquals(id, getRes.getId());
            assertEquals(reflector.get(feldReq), reflector.get(getRes), " Checking field " + reflector.getName());
            assertEquals(value, reflector.get(getRes), " Checking field " + reflector.getName());

        }
        this.deleteAll();
    }

    @Test
    @Order(36)
    public void testPostMinimalPutEachField() {

        //iterate over the values in the object
        for (final FieldReflector reflector : this.classReflector.getValueReflectorsArray()) {
            if (!reflector.isUpdatable() || reflector.isId()) {
                continue;
            }
            // create new empty object
            final T baseReq = this.producer.produceMinimal();
            //then
            assertNotNull(baseReq);
            //when
            final T baseRes = this.getClient()
                                  .post(baseReq);
            //then
            assertNotNull(baseRes);
            final K id = baseRes.getId();
            assertNotNull(id);

            // set only this field
            final Object value = this.producer.produceField(baseRes, reflector);

            final T feldRes = this.getClient()
                                  .put(baseRes);


            assertEquals(reflector.get(baseRes), reflector.get(feldRes), " Checking field " + reflector.getName());
            assertEquals(value, reflector.get(feldRes), " Checking field " + reflector.getName());

            final T getRes = this.getClient()
                                 .get(id);
            //then
            assertNotNull(getRes);
            assertNotNull(getRes.getId());
            assertEquals(id, getRes.getId());
            assertEquals(reflector.get(baseRes), reflector.get(getRes), " Checking field " + reflector.getName());
            assertEquals(value, reflector.get(getRes), " Checking field " + reflector.getName());

        }
        this.deleteAll();
    }

    @Test
    @Order(40)
    public void testDelete() {
        for (final T req : this.updateData) {

            //given
            assertNotNull(req);
            final K id = req.getId();
            assertNotNull(id);

            //when
            this.getClient()
                .delete(id);
        }

        //then
        assertTrue(this.getAll()
                       .isEmpty());
    }


    @Test
    @Order(50)
    public void testPostListAsList() {

        //when
        final List<T> res = this.getClient()
                                .postListAsList(this.insertData);

        //then
        assertNotNull(res);
        assertEquals(this.insertData.size(), res.size());
        for (int index = 0; index < this.updateData.size(); index++) {
            this.insertData.get(index)
                           .setId(res.get(index)
                                     .getId());
            this.updateData.get(index)
                           .setId(res.get(index)
                                     .getId());
        }
        assertThat(this.insertData).hasSameElementsAs(res);
        assertThat(this.insertData).hasSameElementsAs(this.getAll());
    }

    @Test
    @Order(60)
    public void testPutListAsList() {

        //when
        final List<T> res = this.getClient()
                                .putListAsList(this.updateData);

        //then
        assertNotNull(res);
        assertEquals(this.updateData.size(), res.size());
        assertThat(this.updateData).hasSameElementsAs(res);
        assertThat(this.updateData).hasSameElementsAs(this.getAll());
    }

    @Test
    @Order(70)
    public void testDeleteByIds() {
        //given
        final List<K> ids = this.getAll()
                                .stream()
                                .map(PrimaryKey::getId)
                                .collect(Collectors.toList());
        assertFalse(ids.isEmpty());

        //when
        this.getClient()
            .deleteByIds(ids);

        //then
        assertTrue(this.getAll()
                       .isEmpty());
    }

    @Test
    @Order(80)
    public void testDeleteByIdsInPath() {
        //given
        this.testPostListAsList();
        final List<K> ids = this.getAll()
                                .stream()
                                .map(PrimaryKey::getId)
                                .collect(Collectors.toList());
        assertFalse(ids.isEmpty());
        //when
        this.getClient()
            .deleteByIdsInPath(ids);
        //then
        assertTrue(this.getAll()
                       .isEmpty());
    }


    @Test
    @Order(90)
    public void testGetFilterStringFieldEqualsValueAsList() {
        //given
        if (this.stringField == null) {
            return;
        }
        this.deleteAll();

        final List<T> insertedData = this.getClient()
                                         .postListAsList(this.insertData);
        assertEquals(this.insertData.size(), this.getAll()
                                                 .size());

        for (final T source : insertedData) {
            final String value = this.fieldReflector.get(source);

            //when
            final List<T> res = this.getClient()
                                    .getFilterStringFieldEqualsValueAsList(this.stringField, value, this.config.getFirstResult(), this.config.getMaxResults());

            //then
            assertNotNull(res);
            assertFalse(res.isEmpty());
            for (final T rest : res) {
                assertEquals(value, this.fieldReflector.get(rest));
            }

        }
        this.getClient()
            .deleteByIds(insertedData.stream()
                                     .map(PrimaryKey::getId)
                                     .collect(Collectors.toList()));
    }

    @Test
    @Order(100)
    public void testGetFilterStringFieldLikeValueAsList() {
        if (this.stringField == null) {
            return;
        }
        this.deleteAll();
        //given
        final List<T> insertedData = this.getClient()
                                         .postListAsList(this.insertData);
        assertEquals(this.insertData.size(), this.getAll()
                                                 .size());

        for (final T source : insertedData) {
            final String value = this.fieldReflector.get(source);

            //when
            final List<T> res = this.getClient()
                                    .getFilterStringFieldLikeValueAsList(this.stringField, value, this.config.getFirstResult(), this.config.getMaxResults());

            //then
            assertNotNull(res);
            assertFalse(res.isEmpty());
            for (final T rest : res) {
                assertEquals(value, this.fieldReflector.get(rest));
            }
        }
        this.deleteAll();
    }

    @Test
    @Order(110)
    public void testGetFilterStringFieldInValuesAsList() {
        if (this.stringField == null) {
            return;
        }
        this.deleteAll();
        //given
        final List<T> insertedData = this.getClient()
                                         .postListAsList(this.insertData);
        assertEquals(this.insertData.size(), this.getAll()
                                                 .size());

        final List<String> values = insertedData.stream()
                                                .map(this.fieldReflector::get)
                                                .collect(Collectors.toList());
        //when
        final List<T> res = this.getClient()
                                .getFilterStringFieldInValuesAsList(this.stringField, values, this.config.getFirstResult(), this.config.getMaxResults());

        //then
        assertNotNull(res);
        assertEquals(insertedData.size(), res.size());
        this.deleteAll();
    }


    @Test
    @Order(120)
    public void testGetAutocompleteStringFieldLikeValueAsSortedSet() {
        if (this.stringField == null) {
            return;
        }
        this.deleteAll();
        //given
        final List<T> insertedData = this.getClient()
                                         .postListAsList(this.insertData);
        final List<T> data = this.getAll();
        assertEquals(this.insertData.size(), data.size());
        assertThat(this.insertData).hasSameElementsAs(data);

        final List<String> values = data.stream()
                                        .map(this.fieldReflector::get)
                                        .collect(Collectors.toList());
        System.out.println(values);
        for (final String value : values) {
            final String likeValue = value + "%";
            final Set<String> expected = values.stream()
                                               .filter(v -> v.startsWith(value))
                                               .collect(Collectors.toSet());
            //when
            final Set<String> res = new TreeSet<>(this.getClient()
                                                      .getAutocompleteStringFieldLikeValueAsSortedSet(this.stringField, likeValue, null, null, null));

            //then
            assertNotNull(res);
            if (likeValue.length() < this.client.getConfig()
                                                .getAutocompleteCut()) {
                assertTrue(res.isEmpty());
            } else {
                assertEquals(expected, res);
            }
        }

        this.deleteAll();
    }


    @Test
    @Order(121)
    public void testGetAutocompleteIdsStringFieldLikeValueAsSortedSet() {
        if (this.stringField == null) {
            return;
        }
        this.deleteAll();
        //given
        final List<T> insertedData = this.getClient()
                                         .postListAsList(this.insertData);
        final List<T> data = this.getAll();
        assertEquals(this.insertData.size(), data.size());
        assertEquals(this.insertData, data);

        final List<String> values = data.stream()
                                        .map(this.fieldReflector::get)
                                        .collect(Collectors.toList());
        System.out.println(values);
        for (final String value : values) {
            final String likeValue = value + "%";
            final Set<String> expected = values.stream()
                                               .filter(v -> v.startsWith(value))
                                               .collect(Collectors.toSet());
            //when
            final List<IdGroup<K>> res = this.getClient()
                                             .getAutocompleteIdsStringFieldLikeValueAsList(this.stringField, likeValue, null, null, null);
            //then
            assertNotNull(res);
            if (likeValue.length() < this.client.getConfig()
                                                .getAutocompleteCut()) {
                assertTrue(res.isEmpty());
            } else {
                assertEquals(expected.size(), res.size());
            }
        }
        this.deleteAll();
    }

    @Test
    @Order(130)
    public void testPostFilterContentEqualsAsList() {
        //given
        final List<T> insertedData = this.getClient()
                                         .postListAsList(this.insertData);
        assertEquals(this.insertData.size(), this.getAll()
                                                 .size());
        for (final T value : insertedData) {
            // K id = value.getId();
            // value.setId(null);

            //when
            final List<T> res = this.getClient()
                                    .postFilterContentEqualsAsList(this.classReflector.mapValues(value, true), this.config.getFirstResult(), this.config.getMaxResults());

            //then
            assertNotNull(res);
            assertEquals(1, res.size());
            // value.setId(id);
            assertEquals(value, res.get(0));
        }
        this.deleteAll();
    }


    @Test
    @Order(140)
    public void postFilterContentInAsList() {
        //given
        final List<T> insertedData = this.getClient()
                                         .postListAsList(this.insertData);
        assertEquals(this.insertData.size(), this.getAll()
                                                 .size());
        for (final T value : insertedData) {
            // K id = value.getId();
            // value.setId(null);
            final List<T> values = List.of(value);

            //when
            final List<T> res = this.getClient()
                                    .postFilterContentInAsList(this.classReflector.mapValues(values, true), this.config.getFirstResult(), this.config.getMaxResults());

            //then
            assertNotNull(res);
            assertEquals(1, res.size());
            // value.setId(id);
            assertEquals(value, res.get(0));
        }
        this.deleteAll();
    }

    protected void deleteAll() {
        final List<K> ids = this.getAll()
                                .stream()
                                .map(PrimaryKey::getId)
                                .collect(Collectors.toList());
        this.getClient()
            .deleteByIds(ids);
        final List<T> all = this.getAll();
        assertTrue(all.isEmpty());
    }

    protected List<T> getAll() {
        return this.getClient()
                   .getAllAsList(this.client.getConfig()
                                            .getFirstResult(), this.getClient()
                                                                   .getConfig()
                                                                   .getMaxResults(), null);
    }
}
