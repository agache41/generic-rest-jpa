
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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Alexandru.Agache.Extern@atruvia.de
 */
public final class Adapt {
    private Adapt() {
    }

    public static <V> List<V> toList(Map<?, V> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        return source.values()
                     .stream()
                     .filter(Objects::nonNull)
                     .collect(Collectors.toList());
    }

    public static <T, V> List<V> toList(Map<?, T> source, Function<T, V> convertor) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        return source.values()
                     .stream()
                     .filter(Objects::nonNull)
                     .map(convertor)
                     .filter(Objects::nonNull)
                     .collect(Collectors.toList());
    }

    public static <T, V> List<V> toList(Collection<T> source, Function<T, V> convertor) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        return source.stream()
                     .filter(Objects::nonNull)
                     .map(convertor)
                     .filter(Objects::nonNull)
                     .collect(Collectors.toList());
    }

    public static <K, T> Map<K, T> toMap(Collection<T> source, Function<T, K> keyMapper) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyMap();
        }
        return source.stream()
                     .filter(Objects::nonNull)
                     .collect(Collectors.toMap(keyMapper, Function.identity()));
    }

    public static <K, V, T> Map<K, V> toMap(Collection<T> source, Function<T, V> convertor, Function<V, K> keyMapper) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyMap();
        }
        return source.stream()
                     .filter(Objects::nonNull)
                     .map(convertor)
                     .filter(Objects::nonNull)
                     .collect(Collectors.toMap(keyMapper, Function.identity()));
    }
}
