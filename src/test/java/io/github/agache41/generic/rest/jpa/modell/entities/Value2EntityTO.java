
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
import io.github.agache41.generic.rest.jpa.update.Updateable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Update
public class Value2EntityTO implements Updateable<Value2EntityTO> {

    private String subName;

    private String subStreet;

    private Integer subNumber;

    private long subAge;

    public Value2EntityTO(final Value2Entity source) {
        if (source == null) {
            return;
        }
        this.subName = source.getSubName();
        this.subAge = source.getSubAge();
        this.subNumber = source.getSubNumber();
        this.subStreet = source.getSubStreet();
    }

    public Value2Entity toValue2Entity() {
        return new Value2Entity(null, this.subName, this.subStreet, this.subNumber, this.subAge);
    }
}
