
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


import io.github.agache41.generic.rest.jpa.filler.Producer;
import io.github.agache41.generic.rest.jpa.modell2.entities.Modell2;
import io.github.agache41.generic.rest.jpa.resourceService.AbstractResourceServiceImplTest;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;


@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Modell2ResourceServiceTest extends AbstractResourceServiceImplTest<Modell2, Long> {
    private static final Producer<Modell2> supplier = Producer.ofClass(Modell2.class);
    private static final String stringField = "name";

    public Modell2ResourceServiceTest() {


        super(new Modell2ResourceService(), Modell2.class, //
              supplier.produceList(5),
              supplier.produceList(5),
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
