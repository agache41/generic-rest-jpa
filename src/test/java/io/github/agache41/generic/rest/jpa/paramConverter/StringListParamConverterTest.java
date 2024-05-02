
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

package io.github.agache41.generic.rest.jpa.paramConverter;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class StringListParamConverterTest {
    private static final Logger log = Logger.getLogger(StringListParamConverterTest.class);
    private final StringListParamConverter slpc = new StringListParamConverter(log);

    @Test
    public void testNull() {
        assertNotNull(this.slpc.fromString(null));
        assertTrue(this.slpc.fromString(null)
                            .isEmpty());
        assertNotNull(this.slpc.toString(null));
        assertEquals("[]", this.slpc.toString(null));
    }

    @Test
    public void testEmpty() {
        assertNotNull(this.slpc.fromString(""));
        assertTrue(this.slpc.fromString("")
                            .isEmpty());
        assertNotNull(this.slpc.toString(Collections.emptyList()));
        assertEquals("[]", this.slpc.toString(Collections.emptyList()));
    }

    @Test
    public void testValues() {

        for (final String input : new String[]{"[\"12\",\"23\",\"34\"]", //
                                               "[\"aasasdcas\",\"fwrgrweg\",\"ergvregfreq\",\"regreqgfqergfq\"]",//
                                               "[\"asdasd fdd\",\"§!\"$!§\"$\",\"\",\"fq\"]",//
                                               "[\"\"]"//
        }) {

            assertNotNull(this.slpc.fromString(input));
            assertFalse(this.slpc.fromString(input)
                                 .isEmpty());
            assertEquals(input, this.slpc.toString(this.slpc.fromString(input)));
        }
    }
}