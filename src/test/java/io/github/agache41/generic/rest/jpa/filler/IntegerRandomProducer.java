
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

package io.github.agache41.generic.rest.jpa.filler;

public class IntegerRandomProducer extends Producer<Integer> {
    final long min = 1000;
    final int max = 100000;

    public IntegerRandomProducer() {
        super(Integer.class);
    }

    @Override
    public Integer produce() {
        return (int) (this.min + this.random.nextFloat() * this.max);
    }

    @Override
    public Integer change(final Integer result) {
        return this.produce();
    }


}
