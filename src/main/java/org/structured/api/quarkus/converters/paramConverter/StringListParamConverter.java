package org.structured.api.quarkus.converters.paramConverter;

import org.jboss.logging.Logger;

public class StringListParamConverter extends AbstractListParamConvertor<String> {

    public StringListParamConverter(Logger log) {
        super(log);
    }
}