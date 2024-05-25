
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

package io.github.agache41.generic.rest.jpa.modell.entities;

import io.github.agache41.generic.rest.jpa.dataAccess.PrimaryKey;
import io.github.agache41.generic.rest.jpa.update.Update;
import io.github.agache41.generic.rest.jpa.update.Updateable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Update
@Entity
public class Modell extends BaseEntity implements PrimaryKey<Long>, Updateable<Modell> {

    private static final long serialVersionUID = 4981653210124872352L;

    @Id
    @EqualsAndHashCode.Exclude
    @SequenceGenerator(name = "idSequence", sequenceName = "idSequence")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idSequence")
    @Update.excluded
    @Column(name = "id", updatable = false, insertable = false)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "subModell_id")
    private SubModell subModell;

    @Fetch(FetchMode.SELECT)
    @ElementCollection(fetch = FetchType.EAGER)
    @OrderColumn
    @Column(name = "collectionValues")
    private List<Integer> collectionValues;

    @Fetch(FetchMode.SELECT)
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "mapValues")
    private Map<Long, String> mapValues;

    @Fetch(FetchMode.JOIN)
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "valueEntity_id", referencedColumnName = "id")
    private ValueEntity valueEntity;

    @Fetch(FetchMode.SELECT)
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    // add this to prevent Hibernate from using PersistentBag
    @OrderColumn(name = "id")
    private List<CollectionEntity> collectionEntities;

    @Fetch(FetchMode.SELECT)
    @MapKey(name = "id")
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    //add this to prevent failure at post when inserting new keys in the map, keys that will be overwritten.
    @EqualsAndHashCode.Exclude
    private Map<Long, MapEntity> mapEntities;

    @Update.excluded
    @Fetch(FetchMode.SELECT)
    @ElementCollection(fetch = FetchType.EAGER)
    @OrderColumn
    @Column(name = "vCollectionValues")
    private List<String> vCollectionValues = new LinkedList<>();


    @Update.excluded
    @Fetch(FetchMode.SELECT)
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "mapValues")
    private Map<Long, String> vMapValues = new HashMap<>();


    @Update.excluded
    @Fetch(FetchMode.SELECT)
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    // add this to prevent Hibernate from using PersistentBag
    @OrderColumn(name = "id")
    private List<Collection2Entity> vCollectionEntities = new LinkedList<>();

    @Update.excluded
    @Fetch(FetchMode.SELECT)
    @MapKey(name = "id")
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    //add this to prevent failure at post when inserting new keys in the map, keys that will be overwritten.
    @EqualsAndHashCode.Exclude
    private Map<Long, Map2Entity> vMapEntities = new HashMap<>();


    @Update.excluded
    @Fetch(FetchMode.JOIN)
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "value2Entity_id", referencedColumnName = "id")
    private Value2Entity vValue2Entity = new Value2Entity();

    @Update
    public List<String> getVirtualCollectionValues() {
        final List<String> virtualCollectionValues = new LinkedList<>();
        virtualCollectionValues.addAll(this.vCollectionValues);
        return virtualCollectionValues;
    }

    @Update
    public void setVirtualCollectionValues(final List<String> virtualCollectionValues) {
        this.vCollectionValues.clear();
        this.vCollectionValues.addAll(virtualCollectionValues);
    }

    @Update
    public Map<Long, String> getVirtualMapValues() {
        final Map<Long, String> virtualMapValues = new HashMap<>();
        virtualMapValues.putAll(this.vMapValues);
        return virtualMapValues;
    }

    @Update
    public void setVirtualMapValues(final Map<Long, String> virtualMapValues) {
        this.vMapValues.clear();
        this.vMapValues.putAll(virtualMapValues);
    }

    @Update(length = 2)
    public List<Collection2Entity> getVirtualCollectionEntities() {
        final List<Collection2Entity> virtualCollectionEntities = new LinkedList<>();
        virtualCollectionEntities.addAll(this.vCollectionEntities);
        return virtualCollectionEntities;
    }

    @Update(length = 2, notNull = false)
    public void setVirtualCollectionEntities(final List<Collection2Entity> virtualCollectionEntities) {
        this.vCollectionEntities.clear();
        this.vCollectionEntities.addAll(virtualCollectionEntities);
    }

    @Update
    public Map<Long, Map2Entity> getVirtualMapEntities() {
        final Map<Long, Map2Entity> virtualMapEntities = new HashMap<>();
        virtualMapEntities.putAll(this.vMapEntities);
        return virtualMapEntities;
    }

    @Update
    public void setVirtualMapEntities(final Map<Long, Map2Entity> virtualMapEntities) {
        this.vMapEntities.clear();
        this.vMapEntities.putAll(virtualMapEntities);
    }

    @Update
    public Value2EntityTO getValue2EntityTO() {
        return new Value2EntityTO(this.vValue2Entity);
    }

    @Update
    public void setValue2EntityTO(final Value2EntityTO value2EntityTO) {
        this.vValue2Entity.setSubName(value2EntityTO.getSubName());
        this.vValue2Entity.setSubStreet(value2EntityTO.getSubStreet());
        this.vValue2Entity.setSubAge(value2EntityTO.getSubAge());
        this.vValue2Entity.setSubNumber(value2EntityTO.getSubNumber());
    }
}