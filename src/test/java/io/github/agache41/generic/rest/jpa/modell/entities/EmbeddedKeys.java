
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


import io.github.agache41.generic.rest.jpa.update.SelfTransferObject;
import io.github.agache41.generic.rest.jpa.update.Update;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Update
@Embeddable
public class EmbeddedKeys implements SelfTransferObject<EmbeddedKeys> {

    @Column(name = "key1")
    private String key1;

    @Column(name = "key2")
    private String key2;

    @Column(name = "key3")
    private String key3;

    public EmbeddedKeys(final String input) {
        if (input == null) {
            return;
        }
        final String[] inputs = input.split("#");
        if (inputs == null || inputs.length < 3) {
            return;
        }
        this.key1 = inputs[0];
        this.key2 = inputs[1];
        this.key3 = inputs[2];
    }

    @Override
    public String toString() {
        return this.key1 + '#' + this.key2 + '#' + this.key3;
    }
}
