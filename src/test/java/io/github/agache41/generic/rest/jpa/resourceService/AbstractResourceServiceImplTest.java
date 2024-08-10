
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


import io.github.agache41.generic.rest.jpa.dataAccess.PrimaryKey;
import io.github.agache41.generic.rest.jpa.producer.Producer;
import io.github.agache41.generic.rest.jpa.update.Updatable;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;


@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractResourceServiceImplTest<T extends PrimaryKey<K> & Updatable<T>, K> extends AbstractResourceServiceBaseImplTest<T, K, T> {
    public AbstractResourceServiceImplTest(final Class<T> clazz,
                                           final String path,
                                           final List<T> insertData,
                                           final List<T> updateData,
                                           final String stringField,
                                           final Producer<T> producer) {
        super(clazz, clazz, path, insertData, updateData, stringField, producer);
    }

    public AbstractResourceServiceImplTest(final ResourceService<T, K> client,
                                           final Class<T> clazz,
                                           final List<T> insertData,
                                           final List<T> updateData,
                                           final String stringField,
                                           final Producer<T> producer) {
        super(client, clazz, clazz, insertData, updateData, stringField, producer);
    }


    @Override
    @Test
    @Order(10)
    public void testPost() {
        super.testPost();
    }

    @Override
    @Test
    @Order(20)
    public void testGet() {
        super.testGet();
    }

    @Override
    @Test
    @Order(25)
    public void testPostById() {
        super.testPostById();
    }

    @Override
    @Test
    @Order(26)
    public void testGetAllAsList() {
        super.testGetAllAsList();
    }

    @Override
    @Test
    @Order(27)
    public void testGetByIdsAsList() {
        super.testGetByIdsAsList();
    }

    @Override
    @Test
    @Order(28)
    public void testPostByIdsAsList() {
        super.testPostByIdsAsList();
    }


    @Override
    @Test
    @Order(30)
    public void testPut() {
        super.testPut();
    }

    @Override
    @Test
    @Order(33)
    public void testPutEachField() {
        super.testPutEachField();
    }

    @Override
    @Test
    @Order(34)
    public void testPostEachField() {
        super.testPostEachField();
    }

    @Override
    @Test
    @Order(36)
    public void testPostMinimalPutEachField() {
        super.testPostMinimalPutEachField();
    }

    @Override
    @Test
    @Order(40)
    public void testDelete() {
        super.testDelete();
    }


    @Override
    @Test
    @Order(50)
    public void testPostListAsList() {
        super.testPostListAsList();
    }

    @Override
    @Test
    @Order(60)
    public void testPutListAsList() {
        super.testPutListAsList();
    }

    @Override
    @Test
    @Order(70)
    public void testDeleteByIds() {
        super.testDeleteByIds();
    }

    @Override
    @Test
    @Order(80)
    public void testDeleteByIdsInPath() {
        super.testDeleteByIdsInPath();
    }


    @Override
    @Test
    @Order(90)
    public void testGetFilterStringFieldEqualsValueAsList() {
        super.testGetFilterStringFieldEqualsValueAsList();
    }

    @Override
    @Test
    @Order(100)
    public void testGetFilterStringFieldLikeValueAsList() {
        super.testGetFilterStringFieldLikeValueAsList();
    }

    @Override
    @Test
    @Order(110)
    public void testGetFilterStringFieldInValuesAsList() {
        super.testGetFilterStringFieldInValuesAsList();
    }


    @Override
    @Test
    @Order(120)
    public void testGetAutocompleteStringFieldLikeValueAsSortedSet() {
        super.testGetAutocompleteStringFieldLikeValueAsSortedSet();
    }


    @Override
    @Test
    @Order(121)
    public void testGetAutocompleteIdsStringFieldLikeValueAsSortedSet() {
        super.testGetAutocompleteIdsStringFieldLikeValueAsSortedSet();
    }

    @Override
    @Test
    @Order(130)
    public void testPostFilterContentEqualsAsList() {
        super.testPostFilterContentEqualsAsList();
    }


    @Override
    @Test
    @Order(140)
    public void postFilterContentInAsList() {
        super.postFilterContentInAsList();
    }

}
