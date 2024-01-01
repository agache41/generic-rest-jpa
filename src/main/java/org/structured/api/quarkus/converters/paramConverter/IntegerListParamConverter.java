package org.structured.api.quarkus.converters.paramConverter;

import org.jboss.logging.Logger;

/**
 * The type Integer list param converter.
 */
public class IntegerListParamConverter extends AbstractListParamConvertor<Integer> {
    /**
     * Instantiates a new Integer list param converter.
     *
     * @param log the log
     */
    public IntegerListParamConverter(Logger log) {
        super(Integer::parseInt,log);
    }
}