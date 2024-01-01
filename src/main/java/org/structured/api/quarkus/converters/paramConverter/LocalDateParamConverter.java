package org.structured.api.quarkus.converters.paramConverter;

import org.jboss.logging.Logger;

import java.time.LocalDate;

public class LocalDateParamConverter extends AbstractListParamConvertor<LocalDate> {
    public LocalDateParamConverter(Logger log) {
        super(LocalDate::parse,log);
    }
}