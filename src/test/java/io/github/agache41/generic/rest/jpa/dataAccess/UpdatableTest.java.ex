package de.fiduciagad.vermoegensoptimierung.data.v1.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import de.fiduciagad.vermoegensoptimierung.data.v1.jpa.VertriebsproduktEntity;
import de.fiduciagad.vermoegensoptimierung.data.v1.jpa.enums.EnumJaNeinKeineAngabe;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

class UpdatableTest
{
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    private static class UpdatableBean implements Updateable<UpdatableBean>
    {
        private int number;
        private String name;

        @Builder.Default
        private Map<String,UpdatableInnerBean> map = new HashMap<>();

        @Builder.Default
        private Map<String,Integer> mapSimple = new HashMap<>();

        @Override
        public boolean update(UpdatableBean source)
        {
            boolean updated = update(UpdatableBean::getNumber, UpdatableBean::setNumber, this, source);

            updated |= update(UpdatableBean::getName, UpdatableBean::setName, this, source);

            updated |=update(this.map,source.map,UpdatableInnerBean::getKey);

            updated |=update(this.mapSimple,source.mapSimple);

            return updated;
        }
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    private static class UpdatableInnerBean implements Updateable<UpdatableInnerBean>{

        private String key;
        private String value;
        @Override
        public boolean update(UpdatableInnerBean source)
        {
            boolean updated = update(UpdatableInnerBean::getKey,
                                     UpdatableInnerBean::setKey,
                                     this,
                                     source);

            updated|= update(UpdatableInnerBean::getValue,
                          UpdatableInnerBean::setValue,
                          this,
                          source);
            return updated;
        }
    }

    @Test
    void testUpdateSimple()
    {
        UpdatableBean bean1 = UpdatableBean.builder().name("name1").number(1).build();

        UpdatableBean bean2 = UpdatableBean.builder().name("name2").number(2).build();

        assertFalse(bean1.update(bean1));
        assertTrue(bean1.update(bean2));

        assertEquals(bean1.number, bean2.number);
        assertEquals(bean1.name, bean2.name);
    }

    @Test
    void testDeleteInnerMap()
    {
        UpdatableBean bean1 = UpdatableBean.builder()
                                           .name("name1")
                                           .number(1)
                                           .map(new HashMap<>(Map.of("toDelete",UpdatableInnerBean.builder()
                                                                                                  .key("toDelete")
                                                                                                  .value("toDelete")
                                                                                                  .build(),
                                                                     "toUpdate1",UpdatableInnerBean.builder()
                                                                                                   .key("toUpdate1")
                                                                                                   .value("toUpdate1")
                                                                                                   .build(),
                                                                     "toUpdate2",UpdatableInnerBean.builder()
                                                                                                   .key("toUpdate2")
                                                                                                   .value("toUpdate2")
                                                                                                   .build(),
                                                                     "toLeaveUnchanged",UpdatableInnerBean.builder()
                                                                                                          .key("toLeaveUnchanged")
                                                                                                          .value("toLeaveUnchanged")
                                                                                                          .build())))
                                           .mapSimple(new HashMap<>(Map.of("v1",1,
                                                                           "v2",2,
                                                                           "v3",3)))
                                           .build();

        UpdatableBean bean2 = UpdatableBean.builder()
                                           .name("name1")
                                           .number(1)
                                           .map(new HashMap<>())
                                           .build();

        assertFalse(bean1.update(bean1));
        assertTrue(bean1.update(bean2));
        assertEquals(bean1,bean2);
        assertTrue(bean1.map.isEmpty());
    }

    @Test
    void testAddAllInnerMap()
    {
        UpdatableBean bean1 = UpdatableBean.builder()
                                           .name("name1")
                                           .number(1)
                                           .map(new HashMap<>())
                                           .build();

        UpdatableBean bean2 = UpdatableBean.builder()
                                           .name("name1")
                                           .number(1)
                                           .map(new HashMap<>(Map.of("toDelete",UpdatableInnerBean.builder()
                                                                                                  .key("toDelete")
                                                                                                  .value("toDelete")
                                                                                                  .build(),
                                                                     "toUpdate1",UpdatableInnerBean.builder()
                                                                                                   .key("toUpdate1")
                                                                                                   .value("toUpdate1")
                                                                                                   .build(),
                                                                     "toUpdate2",UpdatableInnerBean.builder()
                                                                                                   .key("toUpdate2")
                                                                                                   .value("toUpdate2")
                                                                                                   .build(),
                                                                     "toLeaveUnchanged",UpdatableInnerBean.builder()
                                                                                                          .key("toLeaveUnchanged")
                                                                                                          .value("toLeaveUnchanged")
                                                                                                          .build())))
                                           .mapSimple(new HashMap<>(Map.of("v1",1,
                                                                           "v2",2,
                                                                           "v3",3)))
                                           .build();



        assertFalse(bean1.update(bean1));
        assertTrue(bean1.update(bean2));
        assertEquals(bean1,bean2);
    }

    @Test
    void testUpdateInnerMap()
    {
        UpdatableBean bean1 = UpdatableBean.builder()
                                           .name("name1")
                                           .number(1)
                                           .map(new HashMap<>(Map.of("toDelete",UpdatableInnerBean.builder()
                                                                                    .key("toDelete")
                                                                                    .value("toDelete")
                                                                                    .build(),
                                                       "toUpdate1",UpdatableInnerBean.builder()
                                                                                     .key("toUpdate1")
                                                                                     .value("toUpdate1")
                                                                                     .build(),
                                                       "toUpdate2",UpdatableInnerBean.builder()
                                                                                     .key("toUpdate2")
                                                                                     .value("toUpdate2")
                                                                                     .build(),
                                                       "toLeaveUnchanged",UpdatableInnerBean.builder()
                                                                                .key("toLeaveUnchanged")
                                                                                .value("toLeaveUnchanged")
                                                                                .build())))
                                           .mapSimple(new HashMap<>(Map.of("v1",1,
                                                                           "v2",2,
                                                                           "v3",3)))
                                           .build();

        UpdatableBean bean2 = UpdatableBean.builder()
                                           .name("name1")
                                           .number(1)
                                           .map(new HashMap<>(Map.of("toUpdate1",UpdatableInnerBean.builder()
                                                                                                   .key("toUpdate1")
                                                                                                   .value("updated")
                                                                                                   .build(),
                                                                     "toUpdate2",UpdatableInnerBean.builder()
                                                                                                   .key("toUpdate2")
                                                                                                   .value("updated")
                                                                                                   .build(),
                                                                     "toLeaveUnchanged",UpdatableInnerBean.builder()
                                                                                                          .key("toLeaveUnchanged")
                                                                                                          .value("toLeaveUnchanged")
                                                                                                          .build(),
                                                                     "toAddNew",UpdatableInnerBean.builder()
                                                                                                          .key("toAddNew")
                                                                                                          .value("toAddNew")
                                                                                                          .build())))
                                           .mapSimple(new HashMap<>(Map.of("v1",4,
                                                                           "v2",5,
                                                                           "v3",6)))
                                           .build();

        assertFalse(bean1.update(bean1));
        assertTrue(bean1.update(bean2));
        assertEquals(bean1,bean2);
    }

    @Test
    void testUpdateUnchangedInnerMap()
    {
        UpdatableBean bean1 = UpdatableBean.builder()
                                           .name("name1")
                                           .number(1)
                                           .map(new HashMap<>(Map.of("toDelete",UpdatableInnerBean.builder()
                                                                                                  .key("toDelete")
                                                                                                  .value("toDelete")
                                                                                                  .build(),
                                                                     "toUpdate1",UpdatableInnerBean.builder()
                                                                                                   .key("toUpdate1")
                                                                                                   .value("toUpdate1")
                                                                                                   .build(),
                                                                     "toUpdate2",UpdatableInnerBean.builder()
                                                                                                   .key("toUpdate2")
                                                                                                   .value("toUpdate2")
                                                                                                   .build(),
                                                                     "toLeaveUnchanged",UpdatableInnerBean.builder()
                                                                                                          .key("toLeaveUnchanged")
                                                                                                          .value("toLeaveUnchanged")
                                                                                                          .build())))
                                           .mapSimple(new HashMap<>(Map.of("v1",1,
                                                                           "v2",2,
                                                                           "v3",3)))
                                           .build();

        UpdatableBean bean2 = UpdatableBean.builder()
                                           .name("name1")
                                           .number(1)
                                           .map(new HashMap<>(Map.of("toDelete",UpdatableInnerBean.builder()
                                                                                                  .key("toDelete")
                                                                                                  .value("toDelete")
                                                                                                  .build(),
                                                                     "toUpdate1",UpdatableInnerBean.builder()
                                                                                                   .key("toUpdate1")
                                                                                                   .value("toUpdate1")
                                                                                                   .build(),
                                                                     "toUpdate2",UpdatableInnerBean.builder()
                                                                                                   .key("toUpdate2")
                                                                                                   .value("toUpdate2")
                                                                                                   .build(),
                                                                     "toLeaveUnchanged",UpdatableInnerBean.builder()
                                                                                                          .key("toLeaveUnchanged")
                                                                                                          .value("toLeaveUnchanged")
                                                                                                          .build())))
                                           .mapSimple(new HashMap<>(Map.of("v1",1,
                                                                           "v2",2,
                                                                           "v3",3)))
                                           .build();
        assertEquals(bean1,bean2);
        assertFalse(bean1.update(bean1));
        assertFalse(bean1.update(bean2));
        assertEquals(bean1,bean2);
    }

    @Test
    void testUpdateVertriebsproduktEntity()
    {
        VertriebsproduktEntity vertriebsproduktEntity1 = VertriebsproduktEntity.builder().paiKriteriumErfuellt(
            null).beitragNachhaltig(null).beitragOeko(null).build();


        VertriebsproduktEntity vertriebsproduktEntity2 = VertriebsproduktEntity.builder().paiKriteriumErfuellt(
            EnumJaNeinKeineAngabe.JA).beitragNachhaltig(EnumJaNeinKeineAngabe.JA).beitragOeko(
                EnumJaNeinKeineAngabe.JA).build();

        assertFalse(vertriebsproduktEntity1.update(vertriebsproduktEntity1));
        assertTrue(vertriebsproduktEntity1.update(vertriebsproduktEntity2));
        assertEquals(EnumJaNeinKeineAngabe.JA, vertriebsproduktEntity1.getPaiKriteriumErfuellt());
        assertEquals(EnumJaNeinKeineAngabe.JA, vertriebsproduktEntity1.getBeitragNachhaltig());
        assertEquals(EnumJaNeinKeineAngabe.JA, vertriebsproduktEntity1.getBeitragOeko());
    }
}
