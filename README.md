# structuredAPI

Structured Resources Service and Data Access API for Quarkus

**structuredAPI** is a [JEE](https://en.wikipedia.org/wiki/Jakarta_EE) library targeted on
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

Let the **entity** implement the [PrimaryKey](src/main/java/org/structured/api/quarkus/dataAccess/PrimaryKey.java)
interface :

```java

@Data
@NoArgsConstructor
@Entity
public class Modell implements PrimaryKey<Long> {

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
from [AbstractResourceServiceImpl](src/main/java/org/structured/api/quarkus/resourceService/AbstractResourceServiceImpl.java):

```java

@Path("/modell")
@Transactional
public class ModellResourceService extends AbstractResourceServiceImpl<Modell, Long> {
}
```

and ... you're pretty much done.

For the **Modell** entity the following REST services are available :

- GET /modell/{id} - finds and returns the corresponding entity for the given id.
- GET /modell/all/asList - returns all the entities for the given table.
- GET /modell/byIds/{ids}/asList - finds and returns the corresponding entity for the given list of ids.
- POST /modell/byIds/asList - finds and returns the corresponding entity for the given list of ids using post.
- GET /modell/filter/{stringField}/equals/{value}/asList - finds all entities whose value in a specified string field is
  equal
  the given value.
- GET /modell/filter/{stringField}/like/{value}/asList - finds all entities whose value in a specified field is like the
  given value.
- GET /modell/filter/{stringField}/in/{values}/asList - finds all entities whose value in a specified field is in the
  given values list.
- GET /modell/autocomplete/{stringField}/like/{value}/asSortedSet - finds all values in a database column whose value is
  like the given value.
- POST /modell/filter/content/equals/value/asList - finds in Database the entities that equals a given content object.
- POST /modell/filter/content/in/values/asList - finds in Database the entities that are in a given content list of
  given values.
- POST /modell/ - inserts a new entity in the database or updates an existing one.
- POST /modell/list/asList - inserts a list of new entities in the database or updates the existing ones.
- PUT /modell/ - updates an existing entity by id.
- PUT /modell/list/asList - updates existing entities by id.
- DELETE /modell/{id}/ - deletes the entity for the given id.
- DELETE /modell/byIds - deletes all the entities for the given ids in the request body
- DELETE /modell/byIds/{ids} - deletes all the entities for the given ids.

### Updating

What does the [@Update](src/main/java/org/structured/api/quarkus/update/Update.java) annotation do ?

The Resource Service uses the entity as both [DAO](https://en.wikipedia.org/wiki/Data_access_object)
and [DTO](https://en.wikipedia.org/wiki/Data_transfer_object). Upon update though it is important to be able to
configure which fields participate in the update process and how null values impact that.

When a field is annotated, it will be updated from the provided source during a PUT or POST operation.

When used on the class, all fields will be updated, except the ones annotated with @Update.excluded annotation.

If a field is not annotated, it will not participate in the update process. That is general the case for the id field
and for our last field in the example (age).

By default, during the update the value can not be set to null, so if a null value is received, it will be skipped.
Exception can be enforced with @Update(notNull = false) but only when @NotNull is not used on the field.
This is only recommended to be used when the update source transfer object is always complete.

### Data Access

Extending the [DAO](https://en.wikipedia.org/wiki/Data_access_object) layer

In complex cases the **Data Access** of the entity must be extended, by adding the new data methods.
Let's start by extending [DataAccess](src/main/java/org/structured/api/quarkus/dataAccess/DataAccess.java).

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
extending  [AbstractResourceServiceImplTest](src/main/java/org/structured/api/quarkus/resourceService/AbstractResourceServiceImpl.java).

```java

@QuarkusTest
public class ModellResourceServiceTest extends AbstractResourceServiceImplTest<Modell, Long> {
    public ModellResourceServiceTest() {
        super(Modell.class, // - the entity class
                "/modell", //  - the controller path
                Arrays.asList(modell1, modell2, modell3, modell4, modell5), // - one list of entities to insert
                Arrays.asList(modell1updated, modell2updated, modell3updated, modell4updated, modell5updated), // - one list of entities to update 
                "name"); // - the name of field of type string to take part in filter methods
    }
}

```

The test goes through all the provided methods :

![ModelResourcesServiceTest](/readme.res/ModelResourcesServiceTest.png)

Notice that the entities used in the test are omitted for simplicity from the example.

### Testing my own methods

The ModellResourceServiceTest ist a UnitTest where test methods can be further added :

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

Simply add  `io.structured.api:quarkus-structured-api` dependency to your project.

```xml

<dependency>
    <groupId>io.structured.api</groupId>
    <artifactId>quarkus-structured-api</artifactId>
    <version>version</version>
</dependency>
```

For the test context the tests-classified jar is needed:

```xml

<dependency>
    <groupId>io.structured.api</groupId>
    <artifactId>quarkus-structured-api</artifactId>
    <version>version</version>
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
- Tested with Quarkus 3.4.3.

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
