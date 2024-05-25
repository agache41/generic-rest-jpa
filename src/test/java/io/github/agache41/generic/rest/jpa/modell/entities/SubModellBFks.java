
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
import io.github.agache41.generic.rest.jpa.update.Updatable;
import io.github.agache41.generic.rest.jpa.update.Update;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SubModellBFks implements PrimaryKey<Long>, Updatable<SubModellBFks> {

    private static final long serialVersionUID = 4145235006835414021L;
    @Id
    @EqualsAndHashCode.Exclude
    @SequenceGenerator(name = "idSubModellFks", sequenceName = "sequenceidSubModellFks")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idSubModellFks")
    @Column(name = "id", updatable = false, insertable = false)
    private Long id;

    @EqualsAndHashCode.Exclude
    private Integer index;

    @Update
    @Basic
    @Column(name = "keyA", length = 4, updatable = false, insertable = false)
    @EqualsAndHashCode.Exclude
    private String keyA;

    @Update
    @Basic
    @Column(name = "keyB", length = 4, updatable = false, insertable = false)
    @EqualsAndHashCode.Exclude
    private String keyB;


    @Update
    @Basic
    @Column(name = "keyC", length = 4, updatable = false, insertable = false)
    @EqualsAndHashCode.Exclude
    private String keyC;


    @Update
    private String subName;

    @Update(notNull = false)
    private String subStreet;

    @Update(notNull = false)
    private Integer subNumber;

    @EqualsAndHashCode.Exclude
    private Long age;
}