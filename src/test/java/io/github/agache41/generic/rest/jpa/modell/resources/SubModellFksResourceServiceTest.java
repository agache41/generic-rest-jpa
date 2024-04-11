
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
import io.github.agache41.generic.rest.jpa.modell.entities.SubModellFks;
import io.github.agache41.generic.rest.jpa.resourceService.AbstractResourceServiceImplTest;
import org.junit.jupiter.api.*;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SubModellFksResourceServiceTest extends AbstractResourceServiceImplTest<SubModellFks, Long> {

    private static final String stringField = "subName";
    private static final int collectionSize = 16;
    private static final Producer<SubModellFks> producer;
    private static final List<SubModellFks> insertData;
    private static final List<SubModellFks> updateData;

    static {
        Producer.setDefaultSize(collectionSize);
        producer = Producer.ofClass(SubModellFks.class)
                           .withList(LinkedList::new)
                           .withMap(LinkedHashMap::new)
                           .withSize(16);
        insertData = producer.produceList();
        updateData = producer.changeList(insertData);
    }

    public SubModellFksResourceServiceTest() {
        super(new SubModellFksResourceService(),
              SubModellFks.class, //
              insertData, //
              updateData,
              stringField); //
    }

    @BeforeEach
    void beforeEach() {
        ((SubModellFksResourceService) this.getClient()).getDataAccess()
                                                        .beginTransaction();
    }

    @AfterEach
    void afterEach() {
        ((SubModellFksResourceService) this.getClient()).getDataAccess()
                                                        .commitTransaction();

    }
}