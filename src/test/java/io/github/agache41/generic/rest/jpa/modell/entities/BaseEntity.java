
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

import io.github.agache41.generic.rest.jpa.update.Update;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Data
@MappedSuperclass
public class BaseEntity {
    private static final long serialVersionUID = 7578664415534706949L;

    @Basic
    @Update(order = 2)
    private String stringVal;

    @Basic
    @Update(order = 3, length = 1)
    private String smallStringVal;

    @Basic
    @Update(order = 4, dynamic = false)
    private String stringValNotNull;

    @Basic
    @Update(order = 5)
    private Boolean booleanVal;

    @Basic
    @Update
    private Boolean isBoolean;

    @Basic
    @Update(dynamic = false)
    private boolean booVal;

    @Basic
    @Update(dynamic = false)
    private boolean isBool;

    @Basic
    @Update
    private Integer integerVal;

    @Basic
    @Update(dynamic = false)
    private int intVal;

    @Basic
    @Update
    private Long longVal;

    @Basic
    @Update(dynamic = false)
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

    @Update.excluded
    @Getter(value = AccessLevel.NONE)
    @Setter(value = AccessLevel.NONE)
    private String notUpdatable;

    @Update.excluded
    @Getter(value = AccessLevel.NONE)
    @Setter(value = AccessLevel.NONE)
    @Column(name = "vStringVal", length = 2, nullable = false)
    private String vStringVal;

    @Update(length = 2, nullable = false)
    public String getVirtualStringVal() {
        return this.vStringVal;
    }

    @Update(length = 2, nullable = false)
    public void setVirtualStringVal(final String virtualStringVal) {
        this.vStringVal = virtualStringVal;
    }
}
