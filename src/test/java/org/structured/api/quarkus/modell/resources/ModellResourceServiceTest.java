package org.structured.api.quarkus.modell.resources;


import org.junit.jupiter.api.*;
import org.structured.api.quarkus.modell.entities.Modell;
import org.structured.api.quarkus.resourceService.AbstractResourceServiceImplTest;

import java.util.Arrays;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;


@TestInstance(PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ModellResourceServiceTest extends AbstractResourceServiceImplTest<Modell, Long> {
    static final Modell modell1 = Modell.builder()
                                        .name("aaaa")
                                        .street("one")
                                        .number(1)
                                        .age(100)
                                        .build();
    static final Modell modell2 = Modell.builder()
                                        .name("aaaab")
                                        .street("two")
                                        .number(2)
                                        .age(200)
                                        .build();
    static final Modell modell3 = Modell.builder()
                                        .name("aaaabc")
                                        .street("three")
                                        .number(3)
                                        .age(300)
                                        .build();
    static final Modell modell4 = Modell.builder()
                                        .name("aaaabcd")
                                        .street("four")
                                        .number(null)
                                        .age(400)
                                        .build();
    static final Modell modell5 = Modell.builder()
                                        .name("aaaabcde")
                                        .street(null)
                                        .number(5)
                                        .age(500)
                                        .build();
    static final Modell modell1updated = Modell.builder()
                                               .name("aaaa updated")
                                               .street(null)
                                               .number(11)
                                               .age(101)
                                               .build();
    static final Modell modell2updated = Modell.builder()
                                               .name("aaaab updated")
                                               .street("two updated")
                                               .number(12)
                                               .age(102)
                                               .build();
    static final Modell modell3updated = Modell.builder()
                                               .name("aaaabc updated")
                                               .street("three updated")
                                               .number(null)
                                               .age(103)
                                               .build();
    static final Modell modell4updated = Modell.builder()
                                               .name("aaaaabcd updated")
                                               .street("four updated")
                                               .number(14)
                                               .age(104)
                                               .build();
    static final Modell modell5updated = Modell.builder()
                                               .name("aaaaabcdd updated")
                                               .street(null)
                                               .number(15)
                                               .age(105)
                                               .build();

    static final String stringField = "name";

    public ModellResourceServiceTest() {
        super(new ModellResourceService(), Modell.class, //
                Arrays.asList(modell1, modell2, modell3, modell4, modell5), //
                Arrays.asList(modell1updated, modell2updated, modell3updated, modell4updated, modell5updated),
                stringField); //
    }

    @BeforeEach
    void beforeEach() {
        ((ModellResourceService) this.getClient()).getDataAccess()
                                                  .beginTransaction();
    }

    @AfterEach
    void afterEach() {
        ((ModellResourceService) this.getClient()).getDataAccess()
                                                  .commitTransaction();
    }
}
