
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

package io.github.agache41.generic.rest.jpa.resourceService;

/**
 * The interface Resource service config.
 */
public interface ResourceServiceConfig {

    /**
     * First result int.
     *
     * @return the int
     */
    default int getFirstResult() {
        return 0;
    }

    default int getFirstResult(final Integer input) {
        if (input == null) {
            return this.getFirstResult();
        }
        return input;
    }

    /**
     * Max results int.
     *
     * @return the int
     */
    default int getMaxResults() {
        return 256;
    }

    default int getMaxResults(final Integer input) {
        if (input == null) {
            return this.getMaxResults();
        }
        return input;
    }

    /**
     * Gets autocomplete cut.
     *
     * @return the autocomplete cut
     */
    default int getAutocompleteCut() {
        return 3;
    }

    default int getAutocompleteCut(final Integer input) {
        if (input == null) {
            return this.getAutocompleteCut();
        }
        return input;
    }

    /**
     * Gets autocomplete max results.
     *
     * @return the autocomplete max results
     */
    default int getAutocompleteMaxResults() {
        return 16;
    }

    default int getAutocompleteMaxResults(final Integer input) {
        if (input == null) {
            return this.getAutocompleteMaxResults();
        }
        return input;
    }
}
