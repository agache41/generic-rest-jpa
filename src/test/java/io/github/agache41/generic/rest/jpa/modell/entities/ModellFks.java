
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
import io.github.agache41.generic.rest.jpa.update.SelfTransferObject;
import io.github.agache41.generic.rest.jpa.update.Update;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Update
public class ModellFks implements PrimaryKey<Long>, SelfTransferObject<ModellFks> {

    private static final long serialVersionUID = 4187535114799837397L;
    @Id
    @EqualsAndHashCode.Exclude
    @SequenceGenerator(name = "idModellFks", sequenceName = "sequenceidModellFks")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idModellFks")
    @Column(name = "id", updatable = false, insertable = false)
    private Long id;


    @Update
    @Basic
    @Column(name = "keyA", length = 4, nullable = false, updatable = false)
    private String keyA;

    @Update
    @Basic
    @Column(name = "keyB", length = 4, nullable = false, updatable = false)
    private String keyB;


    @Update
    @Basic
    @Column(name = "keyC", length = 4, nullable = false, updatable = false)
    private String keyC;


    @Update
    @Column(name = "name", length = -1)
    private String name;

    @Update(dynamic = false)
    @Column(name = "street", length = -1)
    private String street;

    @Update(dynamic = false)
    private Integer number;

    @EqualsAndHashCode.Exclude
    private Long age;

    @Update
    @Fetch(FetchMode.JOIN)
    // add this to prevent Hibernate from using PersistentBag and defaulting equals to Object
    @OrderColumn(name = "index")
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumns({
            @JoinColumn(name = "keyA", referencedColumnName = "keyA"),
            @JoinColumn(name = "keyB", referencedColumnName = "keyB"),
            @JoinColumn(name = "keyC", referencedColumnName = "keyC")
    })
    private List<SubModellAFks> subModellAFks = new ArrayList<>();


    @Update
    @Fetch(FetchMode.JOIN)
    // add this to prevent Hibernate from using PersistentBag and defaulting equals to Object
    @OrderColumn(name = "index")
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumns({
            @JoinColumn(name = "keyA", referencedColumnName = "keyA"),
            @JoinColumn(name = "keyB", referencedColumnName = "keyB"),
            @JoinColumn(name = "keyC", referencedColumnName = "keyC")
    })
    private List<SubModellBFks> subModellBFks = new ArrayList<>();
}