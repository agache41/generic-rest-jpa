
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
 * The type Id group.
 *
 * @param <PK> the type parameter
 */
public class IdGroup<PK> implements PrimaryKey<PK> {
    private static final long serialVersionUID = -6076116813129169704L;
    private PK id;
    private String token;
    private Long count;

    /**
     * Instantiates a new Id group.
     *
     * @param id    the id
     * @param token the token
     * @param count the count
     */
    public IdGroup(final PK id,
                   final String token,
                   final Long count) {
        this.id = id;
        this.token = token;
        this.count = count;
    }

    /**
     * Instantiates a new Id group.
     */
    public IdGroup() {
    }

    /**
     * Instantiates a new Id group.
     *
     * @param v the v
     */
    public IdGroup(final Object v) {
        this.id = (PK) ((Object[]) v)[0];
        this.token = (String) ((Object[]) v)[1];
        this.count = (Long) ((Object[]) v)[2];
    }

    @Override
    public PK getId() {
        return this.id;
    }

    @Override
    public void setId(final PK id) {
        this.id = id;
    }

    /**
     * Gets token.
     *
     * @return the token
     */
    public String getToken() {
        return this.token;
    }

    /**
     * Sets token.
     *
     * @param token the token
     */
    public void setToken(final String token) {
        this.token = token;
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
