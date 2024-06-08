
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
 * The interface describing the configuration for a resource service.
 * Contains default values.
 */
public interface ResourceServiceConfig {

    /**
     * The default offset used in filter queries.
     * It should be always 0 in the config.
     * Can be overwritten in the request using the query parameter firstResult.
     *
     * @return the firstResult
     */
    default int getFirstResult() {
        return 0;
    }

    /**
     * Gets first result or configured value
     *
     * @param firstResult the input
     * @return the firstResult
     */
    default int getFirstResult(final Integer firstResult) {
        if (firstResult == null) {
            return this.getFirstResult();
        }
        return firstResult;
    }

    /**
     * The default maxResults (or limit or fetch first maxResults only) used in filter queries.
     * Default value ist 256.
     * Can be overwritten in the request using the query parameter maxResults.
     *
     * @return the maxResults
     */
    default int getMaxResults() {
        return 256;
    }

    /**
     * Gets maxResults or configured value
     *
     * @param maxResults the input
     * @return the maxResults
     */
    default int getMaxResults(final Integer maxResults) {
        if (maxResults == null) {
            return this.getMaxResults();
        }
        return maxResults;
    }

    /**
     * Gets autocomplete minimum string length or autocompleteCut.
     * Default value is 3
     * Can be overwritten in the request using the query parameter cut.
     *
     * @return the autocompleteCut
     */
    default int getAutocompleteCut() {
        return 3;
    }

    /**
     * Gets autocompleteCut or configured value.
     *
     * @param autocompleteCut the input
     * @return the autocomplete cut
     */
    default int getAutocompleteCut(final Integer autocompleteCut) {
        if (autocompleteCut == null) {
            return this.getAutocompleteCut();
        }
        return autocompleteCut;
    }

    /**
     * The default maxResults (or limit or fetch first maxResults only) used in autocomplete queries.
     * Default value ist 16.
     * Can be overwritten in the request using the query parameter maxResults.
     *
     * @return the autocompleteMaxResults
     */
    default int getAutocompleteMaxResults() {
        return 16;
    }

    /**
     * Gets autocompleteMaxResults or configured value
     *
     * @param autocompleteMaxResults the input
     * @return the autocompleteMaxResults
     */
    default int getAutocompleteMaxResults(final Integer autocompleteMaxResults) {
        if (autocompleteMaxResults == null) {
            return this.getAutocompleteMaxResults();
        }
        return autocompleteMaxResults;
    }

    default boolean getVerify() {
        return false;
    }
}
