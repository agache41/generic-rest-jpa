package org.structured.api.quarkus.converters.paramConverter;

import org.jboss.logging.Logger;

import java.time.LocalDate;

/**
 * The type Local date param converter.
 */
public class LocalDateParamConverter extends AbstractListParamConvertor<LocalDate> {
    /**
     * Instantiates a new Local date param converter.
     *
     * @param log the log
     */
    public LocalDateParamConverter(Logger log) {
        super(LocalDate::parse,log);
    }
}