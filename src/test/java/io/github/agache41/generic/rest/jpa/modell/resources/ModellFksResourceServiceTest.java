
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

package io.github.agache41.generic.rest.jpa.modell.resources;

import io.github.agache41.generic.rest.jpa.filler.Producer;
import io.github.agache41.generic.rest.jpa.modell.entities.ModellFks;
import io.github.agache41.generic.rest.jpa.modell.entities.SubModellAFks;
import io.github.agache41.generic.rest.jpa.resourceService.AbstractResourceServiceImplTest;
import org.junit.jupiter.api.*;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ModellFksResourceServiceTest extends AbstractResourceServiceImplTest<ModellFks, Long> {

    private static final String stringField = "name";
    private static final Producer<ModellFks> producer;
    private static final List<ModellFks> insertData;
    private static final List<ModellFks> updateData;

    static {

        producer = Producer.ofClass(ModellFks.class)
                           .withList(LinkedList::new)
                           .withMap(LinkedHashMap::new)
                           .withSize(Config.collectionSize);
        insertData = producer.produceList();
        updateData = producer.changeList(insertData);
    }

    public ModellFksResourceServiceTest() {
        super(new ModellFksResourceService(),
              ModellFks.class, //
              insertData, //
              updateData,
              stringField); //
    }

    @BeforeEach
    void beforeEach() {
        ((ModellFksResourceService) this.getClient()).getDataAccess()
                                                     .beginTransaction();
    }

    @AfterEach
    void afterEach() {
        ((ModellFksResourceService) this.getClient()).getDataAccess()
                                                     .commitTransaction();

    }

    @Test
    @Order(200)
    public void testCascade() {
        final List<ModellFks> res = this.getClient()
                                        .postListAsList(insertData);
        assertEquals(res.size(), insertData.size());
        super.deleteAll();
        final SubModellAFksResourceService subModellAFksResourceService = new SubModellAFksResourceService();
        final List<SubModellAFks> subModellAFksList = subModellAFksResourceService.getAllAsList(subModellAFksResourceService.getConfig()
                                                                                                                            .getFirstResult(), subModellAFksResourceService.getConfig()
                                                                                                                                                                           .getMaxResults());
        assertEquals(0, subModellAFksList.size());

    }
}