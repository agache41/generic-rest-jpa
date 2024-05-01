
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

package io.github.agache41.generic.rest.jpa.dataAccess;

/**
 * The IdGroup object is used in providing an aggregated list of ids
 * and the corresponding column values for them (tokens)
 * together with a count for those record groups.
 * Its mainly role is in navigating while autocompleting search words.
 *
 * @param <PK> the type parameter
 */
public class IdGroup<PK> implements PrimaryKey<PK> {
    private static final long serialVersionUID = -6076116813129169704L;
    private PK id;
    private String value;
    private Long count;

    /**
     * Instantiates a new id group based on a resulting array of a query.
     *
     * @param v the v
     */
    public IdGroup(final Object v) {
        this.id = (PK) ((Object[]) v)[0];
        this.value = (String) ((Object[]) v)[1];
        this.count = (Long) ((Object[]) v)[2];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PK getId() {
        return this.id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setId(final PK id) {
        this.id = id;
    }

    /**
     * Gets token.
     *
     * @return the token
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Sets token.
     *
     * @param value the token
     */
    public void setValue(final String value) {
        this.value = value;
    }

    /**
     * Gets count.
     *
     * @return the count
     */
    public Long getCount() {
        return this.count;
    }

    /**
     * Sets count.
     *
     * @param count the count
     */
    public void setCount(final Long count) {
        this.count = count;
    }
}
