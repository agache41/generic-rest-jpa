
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
import io.github.agache41.generic.rest.jpa.modell.entities.SubModellBFks;
import io.github.agache41.generic.rest.jpa.resourceService.AbstractResourceServiceImplTest;
import org.junit.jupiter.api.*;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SubModellBFksResourceServiceTest extends AbstractResourceServiceImplTest<SubModellBFks, Long> {

    private static final String stringField = "subName";
    private static final int collectionSize = 16;
    private static final Producer<SubModellBFks> producer;
    private static final List<SubModellBFks> insertData;
    private static final List<SubModellBFks> updateData;

    static {
        Producer.setDefaultSize(collectionSize);
        producer = Producer.ofClass(SubModellBFks.class)
                           .withList(LinkedList::new)
                           .withMap(LinkedHashMap::new)
                           .withSize(16);
        insertData = producer.produceList();
        updateData = producer.changeList(insertData);
    }

    public SubModellBFksResourceServiceTest() {
        super(new SubModellBFksResourceService(),
              SubModellBFks.class, //
              insertData, //
              updateData,
              stringField); //
    }

    @BeforeEach
    void beforeEach() {
        ((SubModellBFksResourceService) this.getClient()).getDataAccess()
                                                         .beginTransaction();
    }

    @AfterEach
    void afterEach() {
        ((SubModellBFksResourceService) this.getClient()).getDataAccess()
                                                         .commitTransaction();

    }
}