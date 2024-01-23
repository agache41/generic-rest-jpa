
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

package io.github.agache41.generic.rest.jpa.utils;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Alexandru.Agache.Extern@atruvia.de
 */
public final class ValueMapper<T, R> implements Mapper<T, R> {
    private final Map<T, R> mapping;
    private final Map<R, T> reverseMapping;

    private ValueMapper(Map<T, R> mapping) {
        this.mapping = mapping;
        this.reverseMapping = this.mapping.entrySet()
                                          .stream()
                                          .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    public static <T, R> ValueMapper<T, R> from(Map<T, R> mapping) {
        return new ValueMapper<>(mapping);
    }

    @Override
    public R toRO(T ent) {
        if (ent == null) {
            return null;
        }
        return mapping.get(ent);
    }

    @Override
    public T toEntity(R ro) {
        if (ro == null) {
            return null;
        }
        return reverseMapping.get(ro);
    }
}
