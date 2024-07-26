
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


import io.github.agache41.generic.rest.jpa.modell.entities.EmbeddedIdModell;
import io.github.agache41.generic.rest.jpa.modell.entities.EmbeddedIdSubModell2;
import io.github.agache41.generic.rest.jpa.modell.entities.EmbeddedKeys;
import io.github.agache41.generic.rest.jpa.producer.Producer;
import io.github.agache41.generic.rest.jpa.resourceService.AbstractResourceServiceImplTest;
import org.junit.jupiter.api.*;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;


@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EmbeddedIdModellResourceServiceTest extends AbstractResourceServiceImplTest<EmbeddedIdModell, EmbeddedKeys> {
    private static final String stringField = "stringVal";

    private static final Producer<EmbeddedIdModell> producer;
    private static final List<EmbeddedIdModell> insertData;
    private static final List<EmbeddedIdModell> updateData;

    private static final Consumer<EmbeddedIdModell> havingPK = embeddedIdModell -> {
        EmbeddedKeys id = embeddedIdModell.getId();
        embeddedIdModell.getEmbeddedIdSubModells1()
                        .stream()
                        .forEach(embeddedIdSubModell1 -> {
                            embeddedIdSubModell1.setKey1(id.getKey1());
                            embeddedIdSubModell1.setKey2(id.getKey2());
                            embeddedIdSubModell1.setKey3(id.getKey3());
                        });
        embeddedIdModell.getEmbeddedIdSubModells2()
                        .stream()
                        .map(EmbeddedIdSubModell2::getId)
                        .forEach(id1 -> {
                            id1.setKey1(id.getKey1());
                            id1.setKey2(id.getKey2());
                            id1.setKey3(id.getKey3());
                        });
        if (embeddedIdModell.getEmbeddedIdSubModell3() == null) {
            return;
        }
        EmbeddedKeys id1 = embeddedIdModell.getEmbeddedIdSubModell3()
                                           .getId();
        id1.setKey1(id.getKey1());
        id1.setKey2(id.getKey2());
        id1.setKey3(id.getKey3());
    };

    static {
        producer = Producer.ofClass(EmbeddedIdModell.class)
                           .withPostProduce(havingPK)
                           .withPostChange(havingPK)
                           .withList(LinkedList::new)
                           .withMap(LinkedHashMap::new)
                           .withSize(Config.collectionSize);
        insertData = producer.produceList();
        updateData = producer.changeList(insertData);
    }

    public EmbeddedIdModellResourceServiceTest() {
        super(new EmbeddedIdModellResourceService(), EmbeddedIdModell.class, //
              insertData,   //
              updateData,   //
              stringField); //
    }

    @BeforeEach
    void beforeEach() {
        ((EmbeddedIdModellResourceService) this.getClient()).getDataAccess()
                                                            .beginTransaction();
    }

    @AfterEach
    void afterEach() {
        ((EmbeddedIdModellResourceService) this.getClient()).getDataAccess()
                                                            .commitTransaction();
    }
}
