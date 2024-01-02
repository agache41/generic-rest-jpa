package org.structured.api.quarkus.converters.paramConverter;

import org.jboss.logging.Logger;
import org.structured.api.quarkus.utils.StringUtils;

/**
 * The type String list param converter.
 */
public class StringListParamConverter extends AbstractListParamConvertor<String> {

    /**
     * Instantiates a new String list param converter.
     *
     * @param log the log
     */
    public StringListParamConverter(Logger log) {
        super(StringUtils::unQuote,
                StringUtils::quote,
                log);
    }
}