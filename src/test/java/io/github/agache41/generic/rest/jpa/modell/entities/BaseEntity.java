
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
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigInteger;

@Data
@MappedSuperclass
public class BaseEntity implements PrimaryKey<Long> {
    private static final long serialVersionUID = 7578664415534706949L;
    @Id
    @EqualsAndHashCode.Exclude
    @SequenceGenerator(name = "idSequence", sequenceName = "idSequence")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idSequence")
    @Update.excluded
    @Column(name = "id", updatable = false, insertable = false)
    private Long id;

    @Basic
    @Update
    private String stringVal;

    @Basic
    @Update(notNull = false)
    private String stringValNotNull;

    @Basic
    @Update
    private Boolean booleanVal;

    @Basic
    @Update
    private Boolean isBoolean;

    @Basic
    @Update(notNull = false)
    private boolean booVal;

    @Basic
    @Update(notNull = false)
    private boolean isBool;

    @Basic
    @Update
    private Integer integerVal;

    @Basic
    @Update(notNull = false)
    private int intVal;

    @Basic
    @Update
    private Long longVal;

    @Basic
    @Update
    private long longpVal;

    // todo: fixparse
    //
    //    @Basic
    //    @Update
    //    private BigDecimal bigDecimalVal;
    @Basic
    @Update
    private BigInteger bigIntegerVal;

    @Basic
    @Column(name = "keyA", length = 4, updatable = false)
    private String keyA;

    @Basic
    @Column(name = "keyB", length = 4, updatable = false)
    private String keyB;

    @Basic
    @Column(name = "keyC", length = 4, updatable = false)
    private String keyC;
}
