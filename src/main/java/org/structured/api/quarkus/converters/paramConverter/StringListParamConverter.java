
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