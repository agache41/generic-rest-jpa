
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
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Modell implements PrimaryKey<Long>, Updateable<Modell> {

    @Id
    @NotNull
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
    @OneToOne(mappedBy = "modell")
    private ValueEntity valueEntity;

    @Update
    private List<Integer> collectionValues;

    @Update
    @OneToMany(mappedBy = "modell")
    private List<CollectionEntity> collectionEntities;

    @Update
    @MapKey(name = "id")
    @OneToMany(mappedBy = "modell")
    private Map<Long, CollectionEntity> mapEntities;

    @Update
    @ElementCollection
    private Map<Long, String> mapValues;

}