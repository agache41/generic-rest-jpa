
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

package io.github.agache41.generic.rest.jpa.modell2.resources;


import io.github.agache41.generic.rest.jpa.modell2.entities.Modell2;
import io.github.agache41.generic.rest.jpa.resourceService.AbstractResourceServiceImplTest;
import org.junit.jupiter.api.*;

import java.util.Arrays;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;


@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Modell2ResourceServiceTest extends AbstractResourceServiceImplTest<Modell2, Long> {
    static final Modell2 modell1 = Modell2.builder()
                                          .name("aaaa")
                                          .street("one")
                                          .number(1)
                                          .age(100)
                                          .build();
    static final Modell2 modell2 = Modell2.builder()
                                          .name("aaaab")
                                          .street("two")
                                          .number(2)
                                          .age(200)
                                          .build();
    static final Modell2 modell3 = Modell2.builder()
                                          .name("aaaabc")
                                          .street("three")
                                          .number(3)
                                          .age(300)
                                          .build();
    static final Modell2 modell4 = Modell2.builder()
                                          .name("aaaabcd")
                                          .street("four")
                                          .number(null)
                                          .age(400)
                                          .build();
    static final Modell2 modell5 = Modell2.builder()
                                          .name("aaaabcde")
                                          .street(null)
                                          .number(5)
                                          .age(500)
                                          .build();
    static final Modell2 modell1updated = Modell2.builder()
                                                 .name("aaaa updated")
                                                 .street(null)
                                                 .number(11)
                                                 .age(101)
                                                 .build();
    static final Modell2 modell2updated = Modell2.builder()
                                                 .name("aaaab updated")
                                                 .street("two updated")
                                                 .number(12)
                                                 .age(102)
                                                 .build();
    static final Modell2 modell3updated = Modell2.builder()
                                                 .name("aaaabc updated")
                                                 .street("three updated")
                                                 .number(null)
                                                 .age(103)
                                                 .build();
    static final Modell2 modell4updated = Modell2.builder()
                                                 .name("aaaaabcd updated")
                                                 .street("four updated")
                                                 .number(14)
                                                 .age(104)
                                                 .build();
    static final Modell2 modell5updated = Modell2.builder()
                                                 .name("aaaaabcdd updated")
                                                 .street(null)
                                                 .number(15)
                                                 .age(105)
                                                 .build();

    static final String stringField = "name";

    public Modell2ResourceServiceTest() {
        super(new Modell2ResourceService(), Modell2.class, //
                Arrays.asList(modell1, modell2, modell3, modell4, modell5), //
                Arrays.asList(modell1updated, modell2updated, modell3updated, modell4updated, modell5updated),
                stringField); //
    }

    @BeforeEach
    void beforeEach() {
        ((Modell2ResourceService) this.getClient()).getDataAccess()
                                                   .beginTransaction();
    }

    @AfterEach
    void afterEach() {
        ((Modell2ResourceService) this.getClient()).getDataAccess()
                                                   .commitTransaction();
    }
}
