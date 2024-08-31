
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

package io.github.agache41.generic.rest.jpa.dataAccess;

import io.github.agache41.generic.rest.jpa.utils.ReflectionUtils;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;

/**
 * The Creator class is used to create instances of a class. It is meant to be injected in template classes, providing access to the underlining type.
 *
 * @param <T> the type parameter
 */
public class Creator<T> {
    /**
     * <pre>
     * The type of the Object
     * </pre>
     */
    protected final Class<T> type;
    /**
     * <pre>
     * The no arguments constructor associated for the type.
     * </pre>
     */
    protected final Constructor<T> noArgsConstructor;

    /**
     * Instantiates a new Creator.
     *
     * @param ip the ip
     */
    @Inject
    public Creator(final InjectionPoint ip) {
        this(((Class<T>) (((ParameterizedType) ip.getType()).getActualTypeArguments()[0])));//
    }

    /**
     * Instantiates a new Creator.
     *
     * @param type the type
     */
    public Creator(final Class<T> type) {
        this.type = type;
        this.noArgsConstructor = ReflectionUtils.getNoArgsConstructor(type);
    }

    /**
     * Create t.
     *
     * @return the t
     */
    public T create() {
        try {
            return this.noArgsConstructor.newInstance();
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
