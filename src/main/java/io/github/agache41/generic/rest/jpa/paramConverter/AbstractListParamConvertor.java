
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

package io.github.agache41.generic.rest.jpa.paramConverter;

import jakarta.ws.rs.ext.ParamConverter;
import org.jboss.logging.Logger;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

/**
 * Base class for parameter conversion
 *
 * @param <T> the type parameter
 */
public abstract class AbstractListParamConvertor<T> implements ParamConverter<List<T>> {
    private final Function<String, T> parse;
    private final Function<T, String> format;
    private final Logger log;

    /**
     * Instantiates a new Abstract list param convertor.
     *
     * @param parse  the parse
     * @param format the format
     * @param log    the log
     */
    public AbstractListParamConvertor(final Function<String, T> parse,
                                      final Function<T, String> format,
                                      final Logger log) {
        this.parse = parse;
        this.format = format;
        this.log = log;
    }

    /**
     * Instantiates a new Abstract list param convertor.
     *
     * @param parse the parse
     * @param log   the log
     */
    public AbstractListParamConvertor(final Function<String, T> parse,
                                      final Logger log) {
        this(parse, Object::toString, log);
    }

    @Override
    public List<T> fromString(final String value) {
        if (value == null || value.length() < 2) {
            return Collections.emptyList();
        }
        try {
            final List<T> result = Stream.of(value.substring(1, value.length() - 1)
                                                  .split(","))
                                         .filter(not(String::isEmpty))
                                         .map(this.parse)
                                         .collect(Collectors.toList());
            this.log.debugf("Parsed parameter %s into List%s", value, result);
            return result;
        } catch (final Exception e) {
            this.log.errorf(" When parsing parameter %s as a json list.", value, e);
        }
        return Collections.emptyList();
    }

    @Override
    public String toString(final List<T> value) {
        if (value == null || value.isEmpty()) {
            return "[]";
        }
        try {
            return value.stream()
                        .map(this.format)
                        .collect(Collectors.joining(","));
        } catch (final Exception e) {
            this.log.error(" When formatting list \"" + value + "\" as a json list.", e);
        }
        return "[]";
    }
}
