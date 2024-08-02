
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

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;
import jakarta.persistence.spi.ClassTransformer;
import jakarta.persistence.spi.PersistenceUnitInfo;
import jakarta.persistence.spi.PersistenceUnitTransactionType;
import org.hibernate.jpa.HibernatePersistenceProvider;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;

import static org.hibernate.cfg.BatchSettings.STATEMENT_BATCH_SIZE;
import static org.hibernate.cfg.CacheSettings.*;
import static org.hibernate.cfg.JdbcSettings.*;
import static org.hibernate.cfg.QuerySettings.QUERY_STARTUP_CHECKING;
import static org.hibernate.cfg.SchemaToolingSettings.HBM2DDL_AUTO;
import static org.hibernate.cfg.StatisticsSettings.GENERATE_STATISTICS;


public class H2HEntityManagerFactory {
    private static final H2HEntityManagerFactory instance = new H2HEntityManagerFactory();
    private final EntityManagerFactory entityManagerFactory;

    public H2HEntityManagerFactory() {
        this.entityManagerFactory = new HibernatePersistenceProvider().createContainerEntityManagerFactory(this.archiverPersistenceUnitInfo(), this.config());
    }

    public static H2HEntityManagerFactory getInstance() {
        return instance;
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return this.entityManagerFactory;
    }

    private Map<String, Object> config() {
        final Map<String, Object> map = new HashMap<>();

        map.put(JAKARTA_JDBC_DRIVER, "org.h2.Driver");
        map.put(JAKARTA_JDBC_URL, "jdbc:h2:mem:test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        map.put(JAKARTA_JDBC_USER, "sa");
        map.put(JAKARTA_JDBC_PASSWORD, "");

//      map.put(JAKARTA_JDBC_DRIVER, "org.postgresql.Driver");
//      map.put(JAKARTA_JDBC_URL, "jdbc:postgresql://localhost:5432/modell_quarkus");
//      map.put(JAKARTA_JDBC_USER, "postgres");
//      map.put(JAKARTA_JDBC_PASSWORD, "postgres");

        map.put(HBM2DDL_AUTO, "create-drop");
        // comment this to reduce unnecessary logging
        // map.put(SHOW_SQL, "true");
        map.put(FORMAT_SQL, "true");
        map.put(HIGHLIGHT_SQL, "true");
        map.put(QUERY_STARTUP_CHECKING, "false");
        map.put(GENERATE_STATISTICS, "false");
        map.put(USE_SECOND_LEVEL_CACHE, "false");
        map.put(USE_QUERY_CACHE, "false");
        map.put(USE_STRUCTURED_CACHE, "false");
        map.put(STATEMENT_BATCH_SIZE, "20");
        map.put(AUTOCOMMIT, "true");

        map.put("hibernate.hikari.minimumIdle", "5");
        map.put("hibernate.hikari.maximumPoolSize", "15");
        map.put("hibernate.hikari.idleTimeout", "30000");

        return map;
    }

    private PersistenceUnitInfo archiverPersistenceUnitInfo() {
        return new PersistenceUnitInfo() {
            @Override
            public String getPersistenceUnitName() {
                return "test";
            }

            @Override
            public String getPersistenceProviderClassName() {
                return "com.zaxxer.hikari.hibernate.HikariConnectionProvider";
            }

            @Override
            public PersistenceUnitTransactionType getTransactionType() {
                return PersistenceUnitTransactionType.RESOURCE_LOCAL;
            }

            @Override
            public DataSource getJtaDataSource() {
                return null;
            }

            @Override
            public DataSource getNonJtaDataSource() {
                return null;
            }

            @Override
            public List<String> getMappingFileNames() {
                return Collections.emptyList();
            }

            @Override
            public List<java.net.URL> getJarFileUrls() {
                try {
                    return Collections.list(this.getClass()
                                                .getClassLoader()
                                                .getResources(""));
                } catch (final IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public java.net.URL getPersistenceUnitRootUrl() {
                return null;
            }

            @Override
            public List<String> getManagedClassNames() {
                return Collections.emptyList();
            }

            @Override
            public boolean excludeUnlistedClasses() {
                return true;
            }

            @Override
            public SharedCacheMode getSharedCacheMode() {
                return null;
            }

            @Override
            public ValidationMode getValidationMode() {
                return null;
            }

            @Override
            public Properties getProperties() {
                return new Properties();
            }

            @Override
            public String getPersistenceXMLSchemaVersion() {
                return null;
            }

            @Override
            public ClassLoader getClassLoader() {
                return null;
            }

            @Override
            public void addTransformer(final ClassTransformer transformer) {

            }

            @Override
            public ClassLoader getNewTempClassLoader() {
                return null;
            }
        };
    }

}
