package org.structured.api.quarkus.converters.paramConverter;

import jakarta.ws.rs.ext.ParamConverter;
import org.jboss.logging.Logger;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.structured.api.quarkus.util.Utils.not;

/**
 * The type Abstract list param convertor.
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
    public AbstractListParamConvertor(Function<String, T> parse, Function<T, String> format, Logger log) {
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
    public AbstractListParamConvertor(Function<String, T> parse, Logger log) {
        this(parse, Object::toString, log);
    }

    /**
     * Instantiates a new Abstract list param convertor.
     *
     * @param log the log
     */
    public AbstractListParamConvertor(Logger log) {
        this(s -> (T) s, t -> (String) t, log);
    }

    @Override
    public List<T> fromString(String value) {
        if (value == null || value.length() < 2) return Collections.emptyList();
        try {
            List<T> result = Stream.of(value.substring(1, value.length() - 1).split(","))
                    .filter(not(String::isEmpty))
                    .map(this.parse)
                    .collect(Collectors.toList());
            log.debugf("Parsed parameter %s into List%s", value, result);
            return result;
        } catch (Exception e) {
            log.errorf(" When parsing parameter %s as a json list.",value, e);
        }
        return Collections.emptyList();
    }

    @Override
    public String toString(List<T> value) {
        if (value == null || value.isEmpty()) return "[]";
        try {
            return value.stream().map(this.format).collect(Collectors.joining(","));
        } catch (Exception e) {
            log.error(" When formatting list \"" + value + "\" as a json list.", e);
        }
        return "[]";
    }
}
