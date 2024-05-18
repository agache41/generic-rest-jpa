
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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Update
@Entity
public class EmbeddedIdModell extends BaseEntity implements PrimaryKey<EmbeddedKeys>, Updateable<EmbeddedIdModell> {

    private static final long serialVersionUID = 4981653210124872352L;

    @EmbeddedId
    private EmbeddedKeys id;

    @Update.excluded
    @EqualsAndHashCode.Exclude
    @Column(name = "key1", insertable = false, updatable = false)
    private String key1;

    @Update.excluded
    @EqualsAndHashCode.Exclude
    @Column(name = "key2", insertable = false, updatable = false)
    private String key2;

    @Update.excluded
    @EqualsAndHashCode.Exclude
    @Column(name = "key3", insertable = false, updatable = false)
    private String key3;

    @OrderColumn(name = "orderId")
    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumns({@JoinColumn(name = "key1", referencedColumnName = "key1"), @JoinColumn(name = "key2", referencedColumnName = "key2"), @JoinColumn(name = "key3", referencedColumnName = "key3")})
    private List<EmbeddedIdSubModell1> embeddedIdSubModells1;

    @OrderColumn(name = "orderId")
    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmbeddedIdSubModell2> embeddedIdSubModells2;


    @Fetch(FetchMode.JOIN)
    @OneToOne(fetch = FetchType.EAGER, mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumns({@JoinColumn(name = "key1", referencedColumnName = "key1"), @JoinColumn(name = "key2", referencedColumnName = "key2"), @JoinColumn(name = "key3", referencedColumnName = "key3"),})
    private EmbeddedIdSubModell3 embeddedIdSubModell3;
}