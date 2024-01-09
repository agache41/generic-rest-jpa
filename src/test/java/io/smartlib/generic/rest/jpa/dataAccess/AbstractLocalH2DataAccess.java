
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

package io.smartlib.generic.rest.jpa.dataAccess;

import jakarta.persistence.EntityTransaction;


public class AbstractLocalH2DataAccess<ENTITY extends PrimaryKey<PK>, PK> extends DataAccess<ENTITY, PK> {

    public AbstractLocalH2DataAccess(Class<ENTITY> type, Class<PK> keyType) {
        super(type, keyType);
        this.em = H2HEntityManagerFactory.getInstance()
                                         .getEntityManagerFactory()
                                         .createEntityManager();
    }

    public void beginTransaction() {
        EntityTransaction transaction = this.em.getTransaction();
        transaction.begin();
    }

    public void commitTransaction() {
        this.em.flush();
        EntityTransaction transaction = this.em.getTransaction();
        transaction.commit();
        this.em.clear();
    }
}
