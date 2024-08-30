
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
import io.github.agache41.generic.rest.jpa.update.reflector.ClassReflector;
import io.github.agache41.generic.rest.jpa.update.reflector.FieldReflector;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;


@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractResourceServiceBaseImplTest<T extends PrimaryKey<K>, K> {


    protected static final Logger LOG = Logger.getLogger(AbstractResourceServiceBaseImplTest.class);
    protected final Class<T> clazz;
    protected final List<T> insertData;
    protected final List<T> insertedData;
    protected final List<T> updateData;
    protected final ClassReflector<T, T> classReflector;

    protected final Producer<T> producer;
    protected final FieldReflector<T, T, String, String> fieldReflector;
    protected final String stringField;
    protected final ResourceService<T, K> client;
    protected final ResourceServiceConfig config = new ResourceServiceConfig() {
    };

    public AbstractResourceServiceBaseImplTest(final Class<T> clazz,
                                               final String path,
                                               final List<T> insertData,
                                               final List<T> updateData,
                                               final String stringField,
                                               final Producer<T> producer) {
        this(new ResourceServiceTestClient<>(clazz, path), clazz, insertData, updateData, stringField, producer);
    }

    public AbstractResourceServiceBaseImplTest(final ResourceService<T, K> client,
                                               final Class<T> clazz,
                                               final List<T> insertData,
                                               final List<T> updateData,
                                               final String stringField,
                                               final Producer<T> producer) {
        this.clazz = clazz;
        this.client = client;
        assertEquals(insertData.size(), updateData.size(), " Please use two data lists of equal size!");
        this.insertData = insertData;
        this.insertedData = new LinkedList<>();
        this.updateData = updateData;
        this.classReflector = ClassReflector.ofClass(clazz);
        this.producer = producer;
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

    public ClassReflector<T, T> getClassReflector() {
        return this.classReflector;
    }

    public Producer<T> getProducer() {
        return this.producer;
    }

    @BeforeEach
    public void beforeEach(final TestInfo testInfo) {
        final String testName = testInfo.getDisplayName();
        LOG.infof("Starting %s.%s", this.getClass()
                                        .getSimpleName(), testName);
    }

    @AfterEach
    public void afterEach(final TestInfo testInfo) {
        final String testName = testInfo.getDisplayName();
        LOG.infof("Finished %s.%s", this.getClass()
                                        .getSimpleName(), testName);
    }


    public void testPost() {
        for (int index = 0; index < this.insertData.size(); index++) {

            //given
            final T req = this.insertData.get(index);
            assertNotNull(req, "Provided object instance must not be null. Please check insertData.");

            //when
            final T res = this.getClient()
                              .post(req);
            //then
            assertNotNull(res, "Post response must be not null.");
            final K id = res.getId();
            assertNotNull(id, "Post response must have a not null id.");
            //req.setId(id);
            assertEquals(req, res, "Post response body ist not equal to the post request body.");
            //this.insertData.set(index, res);
            this.insertedData.add(res);
            final T upd = this.updateData.get(index);
            assertNotNull(upd);
            upd.setId(id);
        }
    }


    public void testGet() {
        for (final T req : this.insertedData) {

            //given
            assertNotNull(req, "Provided object instance must not be null. Please check insertData.");
            final K id = req.getId();
            assertNotNull(id, "Provided object id  must not be null. Please check insertData.");

            //when
            final T res = this.getClient()
                              .get(id);
            //then
            assertNotNull(res, "Get response must be not null.");
            assertNotNull(res.getId(), "Get response id must be not null.");
            assertEquals(req.getId(), res.getId(), "Get response id must be equal to response id.\"");
            assertEquals(req, res, "Get result is different than request!");
        }
    }


    public void testPostById() {
        for (final T req : this.insertedData) {

            //given
            assertNotNull(req, "Provided object instance must not be null. Please check insertData.");
            final K id = req.getId();
            assertNotNull(id, "Provided object id must not be null. Please check insertData.");

            //when
            final T res = this.getClient()
                              .postById(id);
            //then
            assertNotNull(res, "PostById response must be not null.");
            assertNotNull(res.getId(), "PostById response id must be not null.");
            assertEquals(req.getId(), res.getId(), "PostById response id must be equal to response id.\"");
            assertEquals(req, res, "PostById (get) result is different than expected!");
        }
    }


    public void testGetAllAsList() {
        //when
        final List<T> res = this.getAll();

        //then
        assertNotNull(res, "Get response must be not null.");
        assertEquals(this.insertedData.size(), res.size(), " Get all returned a different number of results");
        assertThat(this.insertedData).hasSameElementsAs(res);
    }


    public void testGetByIdsAsList() {
        //given
        final List<K> ids = this.insertedData.stream()
                                             .map(PrimaryKey::getId)
                                             .collect(Collectors.toList());

        //when
        final List<T> res = this.getClient()
                                .getByIdsAsList(ids);


        //then
        assertNotNull(res, "Get response must be not null.");
        assertEquals(this.insertedData.size(), res.size(), " GetByIdsAsList returned a different number of results");
        assertThat(this.insertedData).hasSameElementsAs(res);
    }


    public void testPostByIdsAsList() {
        //given
        final List<K> ids = this.insertedData.stream()
                                             .map(PrimaryKey::getId)
                                             .collect(Collectors.toList());

        //when
        final List<T> res = this.getClient()
                                .postByIdsAsList(ids);

        //then
        assertNotNull(res, "PostByIds response must be not null.");
        assertEquals(this.insertedData.size(), res.size(), "PostByIdsAsList returned a different number of results");
        assertThat(this.insertedData).hasSameElementsAs(res);
    }


    public void testPut() {
        for (final T req : this.updateData) {

            //given
            assertNotNull(req, "Provided object instance must not be null. Please check updateData.");
            final K id = req.getId();
            assertNotNull(id, "Provided object id must not be null. Please check updateData.");

            //when
            final T res = this.getClient()
                              .put(req);
            //then
            assertNotNull(res);
            assertNotNull(res.getId());
            assertEquals(id, res.getId());
            assertEquals(req, res, "Put returned a different response then request.");

            final T getres = this.getClient()
                                 .get(id);
            //then
            assertNotNull(getres);
            assertNotNull(getres.getId());
            assertEquals(id, getres.getId());
            assertEquals(req, getres, "Get after Put returned a different response then request.");
        }
    }

    public void testPutEachField() {
        for (final T req : this.updateData) {

            //given
            assertNotNull(req);
            final K id = req.getId();
            assertNotNull(id);

            //iterate over the values in the object
            for (final FieldReflector reflector : this.getClassReflector()
                                                      .getValueReflectorsArray()) {
                if (!reflector.isUpdatable() || reflector.isId()) {
                    continue;
                }
                LOG.infof("Starting Test Put Single for field %s.%s", this.clazz.getSimpleName(), reflector.getName());
                // create new empty object
                final T feldReq = this.getClassReflector()
                                      .newInstance();
                // set the id
                feldReq.setId(id);


                // set only this field
                final Object value = this.getProducer()
                                         .produceField(feldReq, reflector, true);
                //when
                final T feldRes = this.getClient()
                                      .put(feldReq);
                //then
                assertNotNull(feldRes);
                assertNotNull(feldRes.getId());
                assertEquals(id, feldRes.getId());
                assertEquals(value, reflector.get(feldReq));
                assertEquals(value, reflector.get(feldRes), "Field value returned from put is different from request for field " + reflector.getName());

                final T getRes = this.getClient()
                                     .get(id);
                //then
                assertNotNull(getRes);
                assertNotNull(getRes.getId());
                assertEquals(id, getRes.getId());
                assertEquals(value, reflector.get(getRes), " Field value returned from get is different after put request for field " + reflector.getName());

                LOG.infof("Finished Test Put Single for field %s.%s", this.clazz.getSimpleName(), reflector.getName());
                // if field is dynamic, then by put with null it should not change
                if (reflector.isDynamic() && reflector.isNullable()) {
                    LOG.infof("Starting Test null Put Single for field %s.%s", this.clazz.getSimpleName(), reflector.getName());
                    reflector.set(feldReq, null);
                    //when
                    final T feldRes2 = this.getClient()
                                           .put(feldReq);
                    //then
                    assertNotNull(feldRes2);
                    assertNotNull(feldRes2.getId());
                    assertEquals(id, feldRes2.getId());
                    assertEquals(value, reflector.get(feldRes2), "Field value returned from put with null (dynamic) is different than expected (value has changed)  for field " + reflector.getName());

                    final T getRes2 = this.getClient()
                                          .get(id);
                    //then
                    assertNotNull(getRes2);
                    assertNotNull(getRes2.getId());
                    assertEquals(id, getRes2.getId());
                    assertEquals(value, reflector.get(getRes2), " Field Value returned from get after put with null (dynamic) is different than previous value (value has changed!) for field " + reflector.getName());
                    LOG.infof("Finished Test null Put Single for field %s.%s", this.clazz.getSimpleName(), reflector.getName());
                }
            }
        }
        this.deleteAll();
    }

    public void testPostEachField() {

        //iterate over the values in the object
        for (final FieldReflector reflector : this.getClassReflector()
                                                  .getValueReflectorsArray()) {
            if (!reflector.isInsertable() || reflector.isId()) {
                continue;
            }
            LOG.infof("Starting Test Post Single for field %s.%s", this.clazz.getSimpleName(), reflector.getName());
            // create new empty object
            final T feldReq = this.getProducer()
                                  .produceMinimal();
            //then
            assertNotNull(feldReq);
            // set only this field
            final Object value = this.getProducer()
                                     .produceField(feldReq, reflector, true);
            //when
            final T feldRes = this.getClient()
                                  .post(feldReq);
            //then
            assertNotNull(feldRes);
            final K id = feldRes.getId();
            assertNotNull(id);
            assertEquals(value, reflector.get(feldReq));
            assertEquals(value, reflector.get(feldRes), " Wrong field Value returned from post for field " + reflector.getName());

            final T getRes = this.getClient()
                                 .get(id);
            //then
            assertNotNull(getRes);
            assertNotNull(getRes.getId());
            assertEquals(id, getRes.getId());
            assertEquals(value, reflector.get(getRes), "  Wrong field Value returned from get after post for field " + reflector.getName());

            LOG.infof("Finished Test Post Single for field %s.%s", this.clazz.getSimpleName(), reflector.getName());
            this.getClient()
                .delete(id);
        }
        this.deleteAll();
    }

    public void testPostMinimalPutEachField() {

        //iterate over the values in the object
        for (final FieldReflector reflector : this.getClassReflector()
                                                  .getValueReflectorsArray()) {
            if (!reflector.isUpdatable() || reflector.isId()) {
                continue;
            }
            LOG.infof("Starting Test Post Minimal Put Single for field %s.%s", this.clazz.getSimpleName(), reflector.getName());
            // create new empty object
            final T baseReq = this.getProducer()
                                  .produceMinimal();
            //then
            assertNotNull(baseReq);
            //when
            final T baseRes = this.getClient()
                                  .post(baseReq);
            //then
            assertNotNull(baseRes);
            final K id = baseRes.getId();
            assertNotNull(id);
            baseReq.setId(id);

            // set only this field
            final Object value = this.getProducer()
                                     .produceField(baseRes, reflector, true);

            final T feldRes = this.getClient()
                                  .put(baseRes);

            assertEquals(value, reflector.get(baseRes));
            assertEquals(value, reflector.get(feldRes), " Wrong field Value returned from put for field " + reflector.getName());

            final T getRes = this.getClient()
                                 .get(id);
            //then
            assertNotNull(getRes);
            assertNotNull(getRes.getId());
            assertEquals(id, getRes.getId());
            assertEquals(value, reflector.get(getRes), " Wrong field Value returned from get after put for field " + reflector.getName());

            LOG.infof("Finished Test Post Minimal Put Single for field %s.%s", this.clazz.getSimpleName(), reflector.getName());
            // if field is dynamic, then by put with null it should not change
            if (reflector.isDynamic() && reflector.isNullable()) {
                LOG.infof("Starting Test Post Minimal Put null Single for field %s.%s", this.clazz.getSimpleName(), reflector.getName());
                reflector.set(baseReq, null);
                //when
                final T feldRes2 = this.getClient()
                                       .put(baseReq);
                //then
                assertNotNull(feldRes2);
                assertNotNull(feldRes2.getId());
                assertEquals(id, feldRes2.getId());
                assertEquals(value, reflector.get(feldRes2), "Field value returned from put with null (dynamic) is different than expected (value has changed) for field " + reflector.getName());

                final T getRes2 = this.getClient()
                                      .get(id);
                //then
                assertNotNull(getRes2);
                assertNotNull(getRes2.getId());
                assertEquals(id, getRes2.getId());
                assertEquals(value, reflector.get(getRes2), " Field Value returned from get after put with null (dynamic) is different than previous value (value has changed!) for field " + reflector.getName());
                LOG.infof("Finished Test Post Minimal Put null Single for field %s.%s", this.clazz.getSimpleName(), reflector.getName());
            }
            this.getClient()
                .delete(id);
        }
        this.deleteAll();
    }

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


    public void testPostListAsList() {

        //when
        final List<T> res = this.getClient()
                                .postListAsList(this.insertData);

        //then
        assertNotNull(res);
        assertEquals(this.insertData.size(), res.size());
        this.insertedData.clear();
        for (int index = 0; index < this.updateData.size(); index++) {
            this.insertedData.add(res.get(index));
            this.updateData.get(index)
                           .setId(res.get(index)
                                     .getId());
        }
        assertThat(this.insertedData).hasSameElementsAs(res);
        assertThat(this.insertData).hasSameElementsAs(this.getAll());
    }

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
                                    .postFilterContentEqualsAsList(this.getClassReflector()
                                                                       .mapValues(value, true), this.config.getFirstResult(), this.config.getMaxResults());

            //then
            assertNotNull(res);
            assertEquals(1, res.size());
            // value.setId(id);
            assertEquals(value, res.get(0));
        }
        this.deleteAll();
    }


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
                                    .postFilterContentInAsList(this.getClassReflector()
                                                                   .mapValues(values, true), this.config.getFirstResult(), this.config.getMaxResults());

            //then
            assertNotNull(res);
            assertEquals(1, res.size());
            // value.setId(id);
            assertEquals(value, res.get(0));
        }
        this.deleteAll();
    }

    public void deleteAll() {
        final List<K> ids = this.getAll()
                                .stream()
                                .map(PrimaryKey::getId)
                                .collect(Collectors.toList());
        this.getClient()
            .deleteByIds(ids);
        final List<T> all = this.getAll();
        assertTrue(all.isEmpty());
    }

    public List<T> getAll() {
        return this.getClient()
                   .getAllAsList(this.client.getConfig()
                                            .getFirstResult(), this.getClient()
                                                                   .getConfig()
                                                                   .getMaxResults(), null);
    }
}
