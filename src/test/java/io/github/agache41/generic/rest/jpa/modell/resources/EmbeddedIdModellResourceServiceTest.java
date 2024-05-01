
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
import io.github.agache41.generic.rest.jpa.modell.entities.EmbeddedIdModell;
import io.github.agache41.generic.rest.jpa.modell.entities.EmbeddedKeys;
import io.github.agache41.generic.rest.jpa.resourceService.AbstractResourceServiceImplTest;
import org.junit.jupiter.api.*;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;


@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EmbeddedIdModellResourceServiceTest extends AbstractResourceServiceImplTest<EmbeddedIdModell, EmbeddedKeys> {
    private static final String stringField = null;

    private static final Producer<EmbeddedIdModell> producer;
    private static final List<EmbeddedIdModell> insertData;
    private static final List<EmbeddedIdModell> updateData;

    static {

        producer = Producer.ofClass(EmbeddedIdModell.class)
                           .withList(LinkedList::new)
                           .withMap(LinkedHashMap::new)
                           .withSize(Config.collectionSize);
        insertData = producer.produceList();
        updateData = producer.changeList(insertData);
        setPK(insertData);
        setPK(updateData);
    }

    public EmbeddedIdModellResourceServiceTest() {
        super(new EmbeddedIdModellResourceService(),
              EmbeddedIdModell.class, //
              insertData,   //
              updateData,   //
              stringField); //
    }

    private static void setPK(final List<EmbeddedIdModell> data) {
        data.forEach(embeddedIdModell -> {
            embeddedIdModell.getEmbeddedIdSubModells1()
                            .stream()
                            .forEach(embeddedIdSubModell1 -> {
                                embeddedIdSubModell1.setKey1(embeddedIdModell.getId()
                                                                             .getKey1());
                                embeddedIdSubModell1.setKey2(embeddedIdModell.getId()
                                                                             .getKey2());
                                embeddedIdSubModell1.setKey3(embeddedIdModell.getId()
                                                                             .getKey3());
                            });
            embeddedIdModell.getEmbeddedIdSubModells2()
                            .stream()
                            .forEach(embeddedIdSubModell2 -> {
                                embeddedIdSubModell2.getId()
                                                    .setKey1(embeddedIdModell.getId()
                                                                             .getKey1());
                                embeddedIdSubModell2.getId()
                                                    .setKey2(embeddedIdModell.getId()
                                                                             .getKey2());
                                embeddedIdSubModell2.getId()
                                                    .setKey3(embeddedIdModell.getId()
                                                                             .getKey3());
                            });
            embeddedIdModell.getEmbeddedIdSubModell3()
                            .getId()
                            .setKey1(embeddedIdModell.getId()
                                                     .getKey1());
            embeddedIdModell.getEmbeddedIdSubModell3()
                            .getId()
                            .setKey2(embeddedIdModell.getId()
                                                     .getKey2());
            embeddedIdModell.getEmbeddedIdSubModell3()
                            .getId()
                            .setKey3(embeddedIdModell.getId()
                                                     .getKey3());

        });
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
