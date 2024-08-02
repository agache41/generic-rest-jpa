
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

import java.util.List;

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
    /**
     * The Key.
     */
    protected final String key;
    /**
     * The Id.
     */
    protected final PK id;
    /**
     * The Value.
     */
    protected final String value;
    /**
     * The Count.
     */
    protected final int count;

    /**
     * Instantiates a new Id group.
     *
     * @param idEntries the id entries
     */
    public IdGroup(final List<IdEntry<PK>> idEntries) {
        this.count = idEntries.size();
        if (idEntries.isEmpty()) {
            throw new IllegalArgumentException(" received empty list in IdGroup aggregation ");
        }
        final IdEntry<PK> idEntry = idEntries.get(0);
        this.value = idEntry.getValue();
        this.id = idEntry.getId();
        this.key = this.id.toString();
    }

    /**
     * Gets key.
     *
     * @return the key
     */
    public String getKey() {
        return this.key;
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
        // not used
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
     * Gets count.
     *
     * @return the count
     */
    public int getCount() {
        return this.count;
    }


}
