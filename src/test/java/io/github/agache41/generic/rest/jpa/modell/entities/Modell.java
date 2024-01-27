
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

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Modell implements PrimaryKey<Long>, Updateable<Modell> {

    @Id
    @EqualsAndHashCode.Exclude
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Update
    private String name;

    @Update(notNull = false)
    private String street;

    @Update(notNull = false)
    private Integer number;

    @EqualsAndHashCode.Exclude
    private long age;

    @Update
    @Builder.Default
    private List<Integer> collectionValues = new ArrayList<>();

    @Update
    @ElementCollection
    @Builder.Default
    private Map<Long, String> mapValues = new HashMap<>();

    @Update
    @Fetch(FetchMode.JOIN)
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "valueEntity_id", referencedColumnName = "id")
    private ValueEntity valueEntity;

    @Update
    @Fetch(FetchMode.JOIN)
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @Builder.Default
    private Set<CollectionEntity> collectionEntities = new TreeSet<>();

//    @Update
//    @Fetch(FetchMode.JOIN)
//    @MapKey(name = "id")
//    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
//    @Builder.Default
//    private Map<Long, MapEntity> mapEntities = new HashMap<>();

}