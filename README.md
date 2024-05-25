# Generic REST JPA

Generic REST Resources Service API and Data Access over JPA for JEE / Quarkus

**Generic REST JPA** is a [JEE](https://en.wikipedia.org/wiki/Jakarta_EE) library targeted on
creating generic Resource Services and Data Access Layers on top
of [JPA](https://en.wikipedia.org/wiki/Jakarta_Persistence)
and [CDI](https://docs.oracle.com/javaee/6/tutorial/doc/giwhl.html).
It develops [REST](https://en.wikipedia.org/wiki/REST) Services starting from
the [CRUD](https://en.wikipedia.org/wiki/Create,_read,_update_and_delete) pattern and extends it by adding frequently
needed methods to each domain entity.

The idea behind it is that just by adding these generic services on each entity of your database you get a general 70 -
90% implementation of the services needed by the application, thus allowing the developer to focus just on the complex
cases, in other words removing as much as "boilerplate code" as possible.

And that comes with already developed [test units](https://junit.org/junit5/) that bring 100% coverage to
all provided service methods.

The library can be very easily extended to add more functionality by reusing and extending the provided components.

Initially designed for [Quarkus](https://quarkus.io/) it can also be used in any JEE / CDI / JPA compliant environment.

- [Quick start](#quick-start)
    - [Entity](#entity)
    - [Resource Service](#resource-service)
    - [Updating](#updating)
    - [Data Access](#data-access)
    - [Resource Service again](#resource-service-again)
    - [Testing](#testing)
    - [Testing my own methods](#testing-my-own-methods)
- [Demo](#demo)
- [Requirements](#requirements)
- [Installation](#installation)
- [Features](#features)
- [Structure](#structure)

## Quick start

Let's start with a database table named Modell and the associated JPA Entity.

### Entity

Let the **entity** implement
the [PrimaryKey](src/main/java/io/github/agache41/generic/rest/jpa/dataAccess/PrimaryKey.java) and
the [Updatable](src/main/java/io/github/agache41/generic/rest/jpa/update/Updatable.java)

interface :

```java

@Data
@NoArgsConstructor
@Entity
public class Modell implements PrimaryKey<Long>, Updatable<Modell> {

    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Update
    private String name;

    @Update(notNull = false)
    private String street;

    @Update(notNull = false)
    private Integer number;

    @EqualsAndHashCode.Exclude
    private long age;

}
```

Notice the used @Data annotation from [Lombok](https://projectlombok.org/).

### Resource Service

Extend your **resource service**
from [AbstractResourceServiceImpl](src/main/java/io/github/agache41/generic/rest/jpa/resourceService/AbstractResourceServiceImpl.java):

```java

@Path("/modell")
@Transactional
public class ModellResourceService extends AbstractResourceServiceImpl<Modell, Long> {
}
```

and ... you're pretty much done.

For the **Modell** entity the following REST services are available :

- GET /modell/{id} - finds and returns the corresponding entity for the given path id.
- POST /modell/byId - finds and returns the corresponding entity for the given body id.
- GET /modell/all/asList - returns all the entities for the given table.
- GET /modell/byIds/{ids}/asList - finds and returns the corresponding entity for the given path list of id's.
- POST /modell/byIds/asList - finds and returns the corresponding entity for the given list of body id's.
- GET /modell/filter/{stringField}/equals/{value}/asList - finds all entities whose value in a specified string field is
  equal to the given value.
- GET /modell/filter/{stringField}/like/{value}/asList - finds all entities whose value in a specified field is like the
  given path value.
- GET /modell/filter/{stringField}/in/{values}/asList - finds all entities whose value in a specified field is in the
  given path values list.
- GET /modell/autocomplete/{stringField}/like/{value}/asSortedSet - finds all values in a field whose value is like the
  given value.
- GET /modell/autocompleteIds/{stringField}/like/{value}/asList - finds all entities whose value in a field is like the
  given value, groups them, and returns for each the corresponding id.
- POST /modell/filter/content/equals/value/asList - finds all entities that equals a given body content object.
- POST /modell/filter/content/in/values/asList - finds all entities that are in a given body content list of given
  values.
- POST /modell/ - inserts a new entity in the database or updates an existing one.
- POST /modell/list/asList - inserts a list of new entities in the database or updates the existing ones.
- PUT /modell/ - updates an existing entity by id.
- PUT /modell/list/asList - updates existing entities by id.
- DELETE /modell/{id}/ - deletes the entity for the given id.
- DELETE /modell/byIds - deletes all the entities for the given ids in the request body
- DELETE /modell/byIds/{ids} - deletes all the entities for the given ids.

### Updating

What does the [@Update](src/main/java/io/github/agache41/generic/rest/jpa/update/Update.java) annotation do ?

The Resource Service uses the entity as both [DAO](https://en.wikipedia.org/wiki/Data_access_object)
and [DTO](https://en.wikipedia.org/wiki/Data_transfer_object). Upon update though it is important to be able to
configure which fields participate in the update process and how null values impact that.

When a field is annotated, it will be updated from the provided source during a PUT or POST operation.

When used on the class, all fields will be updated, except the ones annotated
with [@Update.excluded](src/main/java/io/github/agache41/generic/rest/jpa/update/Update.java) annotation.

If a field is not annotated, it will not participate in the update process. That is general the case for the id field
and for our last field in the example (age).

By default, during the update the value can not be set to null, so if a null value is received, it will be skipped.
Exception can be enforced with @Update(notNull = false) but only when @NotNull is not used on the field.
This is only recommended to be used when the update source transfer object is always complete.

Every entity participating in this update process must implement
the [Updatable](src/main/java/io/github/agache41/generic/rest/jpa/update/Updatable.java) interface.
The root entity must also implement
the [PrimaryKey](src/main/java/io/github/agache41/generic/rest/jpa/dataAccess/PrimaryKey.java) interface and provide a
unique id field.
If the primary key of the table is composed of several database
columns, [@EmbeddedId](https://jakarta.ee/specifications/persistence/3.2/apidocs/jakarta.persistence/jakarta/persistence/embeddedid)
must be used.

### Data Access

Extending the [DAO](https://en.wikipedia.org/wiki/Data_access_object) layer

In complex cases the **Data Access** of the entity must be extended, by adding the new data methods.
Let's start by extending [DataAccess](src/main/java/io/klebrit/generic/api/dataAccess/DataAccess.java).

```java

@Dependent
public class ModellDataAccess extends DataAccess<Modell, Long> {
    @Inject
    public ModellDataAccess() {
        super(Modell.class, Long.class);
    }

    public List<Modell> getAllModellsOver100() {
        CriteriaBuilder criteriaBuilder = em().getCriteriaBuilder();
        CriteriaQuery<Modell> query = criteriaBuilder.createQuery(type);
        Root<Modell> entity = query.from(type);
        return em().createQuery(query.select(entity)
                                     .where(criteriaBuilder.greaterThan(entity.get("age"), 100L)))
                   .getResultList();
    }

}

```

The method getAllModellsOver100() uses the underlining em() method to access
the available [EntityManager](https://docs.oracle.com/javaee%2F7%2Fapi%2F%2F/javax/persistence/EntityManager.html) and
builds the query using
the [CriteriaBuilder](https://docs.jboss.org/hibernate/stable/entitymanager/reference/en/html/querycriteria.html).

Or if a [JPQL](https://en.wikibooks.org/wiki/Java_Persistence/JPQL) approach is to be considered :

```java

@Dependent
public class ModellDataAccess extends DataAccess<Modell, Long> {
    @Inject
    public ModellDataAccess() {
        super(Modell.class, Long.class);
    }

    public List<Modell> getAllModellsOver100() {
        return em().createQuery(" select t from Modell where t.age > 100")
                   .getResultList();
    }
}

```

### Resource Service again

Now let's use this newly ModellDataAccess in our Resource Service:

```java

@Getter
@Path("/modell")
@Transactional

public class ModellResourceService extends AbstractResourceServiceImpl<Modell, Long> {

    @Inject
    ModellDataAccess dataAccess;

    /**
     * Finds and returns all the models over 100
     *
     * @return the models list.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/over/100")
    public List<Modell> getAllModellsOver100() {
        return this.getDataAccess()
                   .getAllModellsOver100();
    }
}

```

and we're ready to go:

- [GET] /modell/over/100 - Finds and returns all the models over 100

Please do notice the this.getDataAccess() method that gets overridden behind the scenes
with [Lombok](https://projectlombok.org/)

### Testing

So far so good. But how can I be sure that the generated services do really work on my platform or with my entities ?
Not to mention that there are already 17 methods in the service, and that goes for each entity.

Let's start by creating the **TestUnit** by
extending  [AbstractResourceServiceImplTest](src/main/java/io/github/agache41/generic/rest/jpa/resourceService/AbstractResourceServiceImpl.java).

```java

@QuarkusTest
@Transactional
public class ModellResourceServiceTest extends AbstractResourceServiceImplTest<Modell, Long> {

    static final String path = "/modell";
    private static final String stringField = "stringVal";
    private static final Producer<Modell> producer;
    private static final List<Modell> insertData;
    private static final List<Modell> updateData;

    static {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    static {
        producer = Producer.ofClass(Modell.class)
                           .withList(LinkedList::new)
                           .withMap(LinkedHashMap::new)
                           .withSize(Config.collectionSize);
        insertData = producer.produceList();
        updateData = producer.changeList(insertData);
    }

    public ModellResourceServiceTest() {
        super(Modell.class, //
              path, //
              insertData, //
              updateData,
              stringField); //
    }
}

```

Notice the use of the [Producer](src/test/java/io/github/agache41/generic/rest/jpa/producer/Producer.java) class that
generates automatically complete lists with instance objects for tests.

The test goes through all the provided methods :

![ModelResourcesServiceTest](/readme.res/ModelResourcesServiceTest.png)

Notice that the actual entities used in the test are omitted for simplicity from the example.

### Testing my own methods

The ModellResourceServiceTest is a UnitTest where test methods can be further added :

```java

@QuarkusTest
public class ModellResourceServiceTest extends AbstractResourceServiceImplTest<Modell, Long> {
    public ModellResourceServiceTest() {
        /// .....
    }

    @Test
    @Order(1000)
    void testGetAllModellsOver100() {

        /// your favorite method gets tested here 
    }

}

```

Notice the use of the @Order(1000) annotation, this will ensure the correct order of running.

### Generating Front End model classes and services.

My application grows steadily and every day I add new entities. It's time to present the resource services to my clients
in a
ready to code manner.
The smallrye-openapi open API setting ensures the generation of the open API yaml file.

```properties
quarkus.smallrye-openapi.store-schema-directory=openapi/api
quarkus.smallrye-openapi.open-api-version=3.0.3
```

Then the `org.openapitools:openapi-generator-maven-plugin:7.0.1` plugin will generate the classes for the front end.
Here is an example for [Angular](https://angular.io/) using [Typescript](https://www.typescriptlang.org/).

```xml

<profile>
    <id>generate</id>
    <activation>
        <property>
            <name>generate</name>
        </property>
    </activation>
    <build>
        <plugins>
            <plugin>
                <groupId>org.openapitools</groupId>
                <artifactId>openapi-generator-maven-plugin</artifactId>
                <!-- RELEASE_VERSION -->
                <version>7.0.1</version>
                <!-- /RELEASE_VERSION -->
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>generate</goal>
                        </goals>
                        <configuration>
                            <inputSpec>${project.basedir}/openapi/api/openapi.yaml</inputSpec>
                            <generatorName>typescript-angular</generatorName>
                            <configOptions>
                                <sourceFolder>src/gen/java/main</sourceFolder>
                            </configOptions>
                            <output>${project.basedir}/openapi/generated</output>
                            <verbose>true</verbose>
                            <cleanupOutput>true</cleanupOutput>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</profile>
```

Here ist an example of the generated files:

![OpenapiGeneration](/readme.res/OpenapiGeneration.png)

## Demo

А comprehensive example of using the library with JPA database you can find in the **[demo](/demo)** module.

## Requirements

The library works with Java 11+, Quarkus 3.4.3+, JPA 2+

## Installation

Simply add  `io.github.agache41:generic-rest-jpa.version` dependency to your project.

The current version today at sunset:

```xml

<generic-rest-jpa.version>0.2.7</generic-rest-jpa.version>
```

The dependency for the main jar:

```xml

<dependency>
    <groupId>io.github.agache41</groupId>
    <artifactId>generic-rest-jpa</artifactId>
    <version>${generic-rest-jpa.version}</version>
</dependency>
```

For the test context the tests-classified jar is needed:

```xml

<dependency>
    <groupId>io.github.agache41</groupId>
    <artifactId>generic-rest-jpa</artifactId>
    <version>${generic-rest-jpa.version}</version>
    <classifier>tests</classifier>
    <type>test-jar</type>
    <scope>test</scope>
</dependency>
```

## Features

- Easy to install, use and extend.
- Test coverage provided on the fly.
- Works with both [Jackson](https://github.com/FasterXML/jackson) and [JSONB](https://javaee.github.io/jsonb-spec/). No
  support yet for reactive mode.
- Tested with Quarkus 3.6.7.

## Structure

The library is packaged as a single jar. An auxiliary test classifier jar is provided for test context.

## Dependencies

Execution dependencies consist in the needed packages from [Jakarta](https://jakarta.ee/)
and [JbossLogging](https://github.com/jboss-logging/jboss-logging) and are not transitive.

```xml

<dependencies>
    <dependency>
        <groupId>jakarta.enterprise</groupId>
        <artifactId>jakarta.enterprise.cdi-api</artifactId>
        <version>4.0.1</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>jakarta.persistence</groupId>
        <artifactId>jakarta.persistence-api</artifactId>
        <version>3.1.0</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>jakarta.ws.rs</groupId>
        <artifactId>jakarta.ws.rs-api</artifactId>
        <version>3.1.0</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>jakarta.validation</groupId>
        <artifactId>jakarta.validation-api</artifactId>
        <version>3.0.2</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>org.jboss.logging</groupId>
        <artifactId>jboss-logging</artifactId>
        <version>3.5.3.Final</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

Testing dependencies are listed here. Please note the Lombok is used only in the test context.

```xml

<dependencies>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-engine</artifactId>
        <version>5.10.1</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>io.rest-assured</groupId>
        <artifactId>rest-assured</artifactId>
        <version>5.4.0</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.30</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.hibernate.orm</groupId>
        <artifactId>hibernate-core</artifactId>
        <version>6.4.1.Final</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <version>2.2.224</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```
