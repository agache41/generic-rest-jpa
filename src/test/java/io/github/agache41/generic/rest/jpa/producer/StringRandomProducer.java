
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

public class StringRandomProducer extends Producer<String> {

    final int leftLimit = 'a'; // letter 'a '
    final int rightLimit = 'z'; // letter 'z'
    final float max = this.rightLimit - this.leftLimit;
    final int targetStringLength = 8;

    public StringRandomProducer() {
        super(String.class);
    }

    @Override
    public String produce() {
        final StringBuilder buffer = new StringBuilder(this.targetStringLength);
        for (int i = 0; i < this.targetStringLength; i++) {
            final int randomLimitedInt = this.leftLimit + (int) (this.random.nextFloat() * this.max);
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }

    @Override
    public String change(final String result) {
        return this.produce();
    }
}
