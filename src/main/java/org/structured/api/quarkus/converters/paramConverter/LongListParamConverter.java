package org.structured.api.quarkus.converters.paramConverter;

import org.jboss.logging.Logger;

/**
 * The type Long list param converter.
 */
public class LongListParamConverter extends AbstractListParamConvertor<Long> {
    /**
     * Instantiates a new Long list param converter.
     *
     * @param log the log
     */
    public LongListParamConverter(Logger log) {
        super(Long::parseLong,log);
    }
}