
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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface Mapper<T, R> {
    R toRO(T entity);

    T toEntity(R ro);

    default <K> List<R> toROList(Map<K, T> source) {
        return Adapt.toList(source, this::toRO);
    }

    default List<R> toROList(Collection<T> source) {
        return Adapt.toList(source, this::toRO);
    }

    default <K> Map<K, R> toROMap(Collection<T> source, Function<R, K> keyMapper) {
        return Adapt.toMap(source, this::toRO, keyMapper);
    }

    default List<T> toEntityList(Map<?, R> source) {
        return Adapt.toList(source, this::toEntity);
    }

    default List<T> toEntityList(Collection<R> source) {
        return Adapt.toList(source, this::toEntity);
    }

    default <K> Map<K, T> toEntityMap(Collection<R> source, Function<T, K> keyMapper) {
        return Adapt.toMap(source, this::toEntity, keyMapper);
    }
}
