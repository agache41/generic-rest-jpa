
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

package io.github.agache41.generic.rest.jpa.producer;

import java.util.Collections;
import java.util.LinkedList;

public class CharacterRandomProducer extends Producer<Character> {

    private final LinkedList<Character> charList = new LinkedList<>();

    public CharacterRandomProducer() {
        super(Character.class);
        this.initCharList();
    }

    @Override
    public Character produce() {
        if (this.charList.isEmpty()) {
            this.initCharList();
        }
        return this.charList.remove();
    }

    private void initCharList() {
        for (final char charVal : StringRandomProducer.charset)
            this.charList.add(charVal);
        Collections.shuffle(this.charList);
    }

    @Override
    public Character change(final Character result) {
        return this.produce();
    }
}
