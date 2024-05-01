
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
}