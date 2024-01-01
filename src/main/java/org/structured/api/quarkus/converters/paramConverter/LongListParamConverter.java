package org.structured.api.quarkus.converters.paramConverter;

import org.jboss.logging.Logger;

public class LongListParamConverter extends AbstractListParamConvertor<Long> {
    public LongListParamConverter(Logger log) {
        super(Long::parseLong,log);
    }
}