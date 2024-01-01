package org.structured.api.quarkus.converters.paramConverter;

import org.jboss.logging.Logger;

public class IntegerListParamConverter extends AbstractListParamConvertor<Integer> {
    public IntegerListParamConverter(Logger log) {
        super(Integer::parseInt,log);
    }
}