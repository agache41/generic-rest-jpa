
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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Update
@Entity
public class EmbeddedIdSubModell1 extends BaseEntity implements PrimaryKey<Long>, Updateable<EmbeddedIdSubModell1> {

    private static final long serialVersionUID = 4145235006835414021L;

    @Id
    @EqualsAndHashCode.Exclude
    @SequenceGenerator(name = "idSequence", sequenceName = "idSequence")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idSequence")
    @Update.excluded
    @Column(name = "id")
    private Long id;

    @Update.excluded
    @EqualsAndHashCode.Exclude
    @Column(name = "key1")
    private String key1;

    @Update.excluded
    @EqualsAndHashCode.Exclude
    @Column(name = "key2")
    private String key2;

    @Update.excluded
    @EqualsAndHashCode.Exclude
    @Column(name = "key3")
    private String key3;

}