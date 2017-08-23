# Description

This is the material accompanying the presentation of part I - Database persistence with Camel from simple to more elaborated.
It covers the different demos made during the talk and is organised like that:

* `database`: directory containing the scripts to create the database for H2, HSQLDB and PostgreSQL RDBMSes
* `jdbc-spring`: Maven project using camel-jdbc component and Spring XML DSL
* `jdbc-blueprint`: Maven project using camel-jdbc component and Blueprint XML DSL
* `sql-spring`: Maven project using camel-sql component and Spring XML DSL
* `sql-blueprint`: Maven project using camel-sql component and Blueprint XML DSL
* `jpa-javase`: Maven project with JUnit tests showing JPA API usage with local and JTA transactions
* `jpa-ds`: a bundle defining JDBC data sources to be installed in JBoss Fuse container
* `jpa-model`: a bundle defining JPA Persistence Units to be installed in JBoss Fuse container
* `jpa-ds-and-model`: data source + persistence units (however see comments below)
* `jpa-spring`: a bundle with Spring XML based Camel routes using `jpa:` endpoints (both consumer and producer)
* `jpa-blueprint`: a bundle with Blueprint XML based Camel routes using `jpa:` endpoints (both consumer and producer)
and JTA-bound `EntityManager` injected directly to Camel bean

# Initial setup

We will refer to the root directory of `camel-persistence-part1` project as `$PROJECT_HOME`.

## Setting up docker-based databases

To perform tests in more realistic environments, we can leverage the power of docker to run more advanced database servers.
Of course you can use existing database instances. The below examples are just here for completeness.

### PostgreSQL database

We can use *official* PostgreSQL docker image available at [docker hub](https://registry.hub.docker.com/_/postgres/).
You can use any of available methods to access PostgreSQL server (e.g., by mapping ports or connecting to containers IP address directly).

1. Start PostgreSQL server docker container:

        $ docker run -d --name fuse-postgresql-server -e POSTGRES_USER=fuse -e POSTGRES_PASSWORD=fuse -p 5432:5432 postgres:9.6.4

2. Create `reportdb` database from the `fuse-postgresql-server` container:

        $ docker exec -ti fuse-postgresql-server /bin/bash
        root@58b0d9de9c5b:/# psql -U fuse -d fuse
        psql (9.6.4)
        Type "help" for help.
        fuse=# create database reportdb owner fuse encoding 'utf8';
        CREATE DATABASE
        fuse=# \c reportdb
        You are now connected to database "reportdb" as user "fuse".
        reportdb=# create schema report;
        CREATE SCHEMA
        reportdb=# \q

3. Create `reportdb2` database from the `fuse-postgresql-server` container (for XA purposes):

        $ docker exec -ti fuse-postgresql-server /bin/bash
        root@58b0d9de9c5b:/# psql -U fuse -d fuse
        psql (9.6.4)
        Type "help" for help.
        fuse=# create database reportdb2 owner fuse encoding 'utf8';
        CREATE DATABASE
        fuse=# \c reportdb2
        You are now connected to database "reportdb2" as user "fuse".
        reportdb2=# create schema report;
        CREATE SCHEMA
        reportdb2=# \q

4. Initialize database `reportdb` by creating schema, table and populating the table with data.

        $ cd $PROJECT_HOME/database
        $ docker cp src/config/postgresql/reportdb-postgresql-script.sql fuse-postgresql-server:/tmp
        $ docker exec -ti fuse-postgresql-server /bin/bash
        root@58b0d9de9c5b:/# psql -U fuse -d reportdb -f /tmp/reportdb-postgresql-script.sql 
        ...
        DROP SCHEMA
        CREATE SCHEMA
        CREATE TABLE
        INSERT 0 1
        INSERT 0 1
        INSERT 0 1
        INSERT 0 1

5. Configure PostgreSQL database to allow XA transactions by setting `max_prepared_transactions` to the value equal
or greater than `max_connections` setting (`100` in the case of `postgres:9.6.4` image).

        root@58b0d9de9c5b:/# sed -i 's/^#max_prepared_transactions = 0/max_prepared_transactions = 200/' /var/lib/postgresql/data/postgresql.conf

5. Restart `fuse-postgresql-server` container. Your PostgreSQL database is ready to use.

# Running examples

## `jdbc-spring` and `jdbc-blueprint`

These examples can be run using camel-test-spring and camel-test-blueprint respectively. Examples will run outside
of JBoss Fuse server and require only running database server.

For `jdbc-spring` example, run:

    $ cd $PROJECT_HOME/jdbc-spring
    $ mvn clean compile camel:run

We will see Camel context being started and after initial delay of 4 secods, every 20 seconds we'll see list of
incidents being printed in console.

Additionally we can trigger `key-from-file` route simply by dropping comma-separated list of incident references
to a file inside `$PROJECT_HOME/jdbc-spring/target/data` directory. Here's example:

    $ cd $PROJECT_HOME/jdbc-spring
    $ echo -n 002,004 > target/data/keys
    
We can also run 2nd Camel context using:

    $ cd $PROJECT_HOME/jdbc-spring
    $ mvn clean compile camel:run -Pcontext2

For `jdbc-blueprint` we use Blueprint XML DSL and because of more _discovery_ nature of `camel-test-blueprint`, we
use only `camelContext1.xml` example. We can run this example using:

    $ cd $PROJECT_HOME/jdbc-blueprint
    $ mvn clean package camel:run

The invocation is slightly different - `package` goal has to be invoked, so we have proper OSGi bundle - or rather
proper OSGi bundle `MANIFEST.MF` file generated in `target/classes/META-INF` directory. This allows `camel-test-blueprint`
to _pick up_ `target/classes` directory as correct OSGi bundle.

## `sql-spring` and `sql-blueprint`

These examples also can be run outside JBoss Fuse server using camel-test-spring and camel-test-blueprint respectively.
This time we can also create new records in database by sending content of files through Camel route to the database
using `camel-spring` component.

For `sql-spring` example, run:

    $ cd $PROJECT_HOME/sql-spring
    $ mvn clean compile camel:run

This will start the route and print the content of `t_incident` table every 20 seconds. We can create new incidents
in two ways:

1. by invoking `insert-from-file-using-bean` route that prepares new record in bean method:

        $ cd $PROJECT_HOME/sql-spring
        $ cp data/key.txt target/datainsert

2. by invoking `insert-from-file-using-split` route that prepares new record simply by splitting comma-separated values
from given file

        $ cd $PROJECT_HOME/sql-spring
        $ cp data/keyParams.txt target/datainsertparams

For `sql-blueprint` the examples are run almost like the ones in `sql-spring`, except that `package` phase has
to be invoked.

        $ cd $PROJECT_HOME/sql-blueprint
        $ mvn clean package camel:run

## `jpa-javase`

This project should be examined before running `jpa-spring` and `jpa-blueprint` (and supporting `jpa-ds` and `jpa-model`).
`jpa-javase` shows few _canonical_ ways of using JPA API with Hibernate JPA provider.
According to JPA 2.0 specification (JSR 317), JPA can successfully be used outside application server - in pure JavaSE
environment. Using JPA this way is described as _application-managed Entity Manager_.

In this mode, it's the role of application developer to create `EntityManagerFactory` and obtain `EntityManager`. (In
application server, i.e., when using _container-managed Entity Manager_, `EntityManager` instance is injected to application
code using `@javax.persistence.PersistenceContext` annotation).

`jpa-javase` provides 3 unit tests that illustrate few uses cases related to JPA:

* `com.fusesource.examples.persistence.part1.DiscoveryTest.discovery()`: a tiny example of using JPA discover API,
where we can check what is the configured (discovered) `javax.persistence.spi.PersistenceProvider` instance.
* `com.fusesource.examples.persistence.part1.JavaSETest.bootstrapAndUseApplicationManagedResourceLocalEntityManager()`:
Full example using `transaction-type="RESOURCE_LOCAL"` that shows how to:
  * create dbcp2 `javax.sql.DataSource`
  * use `javax.persistence.Persistence.createEntityManagerFactory()` to _create_ application-managed EMF using provided properties
  * use `javax.persistence.EntityManagerFactory.createEntityManager()` to _obtain_ EntityManager
  * use `javax.persistence.EntityTransaction` API to manually demarcate JPA transactions according to declared `transaction-type="RESOURCE_LOCAL"`
  * use `javax.persistence.EntityManager.persist()` to simply use JPA API
  * use plain JDBC code to verify that record was indeed persisted to database
* `com.fusesource.examples.persistence.part1.JavaSETest.bootstrapAndUseApplicationManagedJTAEntityManager()`:
Full example using `transaction-type="JTA"` that shows how to:
  * create fully functional `javax.transaction.TransactionManager`/`javax.transaction.UserTransaction` instances using
    aries.transaction.manager
  * create 2 `org.postgresql.xa.PGXADataSource` instances for XA-aware access to PostgreSQL database
  * create 2 `javax.sql.DataSource` instances that are using JCA API to interact with aries.jdbc and provide
    XA-aware, JTA-enlisting, pooling data sources
  * use `javax.persistence.Persistence.createEntityManagerFactory()` to _create_ application-managed EMF using provided properties
  * use `javax.persistence.EntityManagerFactory.createEntityManager()` to _obtain_ EntityManager
  * use `javax.transaction.UserTransaction` API to manually demarcate JPA transactions according to declared `transaction-type="JTA"`
  * use `javax.persistence.EntityManager.joinTransaction()` to tie JPA to JTA
  * use `javax.persistence.EntityManager.persist()` to simply use JPA API
  * use plain JDBC to insert row to 2nd database within the same global JTA transaction
  * use plain JDBC code to verify that record was indeed persisted to both databases

## `jpa-spring` and `jpa-blueprint`

These examples run inside JBoss Fuse container, because `mvn camel:run`, which underneath uses trimmed-down OSGi registry
(Felix Connect, formerly known as PojoSR) doesn't work well with aries.jpa.

Before installing any _logic_ bundles, we'll create/use 2 supporting bundles:

* `jpa-ds` which exposes `javax.sql.(XA)DataSource` OSGi services for use by other bundles
* `jpa-model` which exposes `javax.persistence.EntityManagerFactory` OSGi service to be used by camel routes, components
and any bundle that requires access to EMF and EntityManager

The separation of _data source_ and _JPA model_ bundles is important in OSGi environment, so we won't get into
situation when none of the services will be published and will be blocked waiting for each other.
More precisely - common mistake is to create a Blueprint bundle that both:
* declare a `<bean>` for `javax.sql.DataSource` and
* declare a `<bean>` which needs `javax.persistence.EntityManagerFactory` service, even if `META-INF/persistence.xml` is
in another bundle

The reason is that `EntityManagerFactory` will never get published when `DataSource` is not available, and Blueprint container
will never publish `javax.sql.DataSource` while waiting for `javax.persistence.EntityManagerFactory`.

It is (almost) fine to have single bundle publishing both DataSource OSGi services (either from Spring XML or Blueprint XML)
and at the same time containing `Meta-Persistence: META-INF/persistence.xml` Manifest header - this bundle will be
`extended` by two `extenders`:
* aries.blueprint.core that'll run BlueprintContainer or org.springframework.osgi.extender that'll run Spring context
* org.apache.aries.jpa.container that'll instantiate `EntityManagerFactory` from `META-INF/persistence.xml` descriptor

An example of such bundle is shown in `jpa-ds-and-model` project.

The problem may arise when `hibernate.hbm2ddl.auto` property is used in `META-INF/persistence.xml` - because data
source is immediately needed during construction of `EntityManagerFactory`. That's why it's preferred to separate
data source and entity manager factory bundles.

Here's a list of steps required to install `jpa-ds` bundle in fresh Fuse installation:

    install -s mvn:org.postgresql/postgresql/42.1.4
    install -s mvn:org.apache.commons/commons-pool2/2.4.2
    install -s mvn:org.apache.commons/commons-dbcp2/2.1.1
    features:install jndi transaction connector
    install -s mvn:com.fusesource.examples.camel-persistence-part1/jpa-ds/1.0

After installing and starting `jpa-ds`, we can check the the services exposed by it:

    JBossFuse:karaf@root> ls com.fusesource.examples.camel-persistence-part1.jpa-ds
    
    FuseSource :: Examples :: Camel Persistence :: JPA Data Source (320) provides:
    ------------------------------------------------------------------------------
    objectClass = [javax.sql.DataSource]
    osgi.jndi.service.name = jdbc/reportdb
    service.id = 705
    ...
    ----
    aries.managed = true
    objectClass = [javax.sql.DataSource]
    osgi.jndi.service.name = jdbc/reportdb
    service.id = 706
    service.ranking = 1000
    ...
    ----
    objectClass = [javax.sql.XADataSource]
    osgi.jndi.service.name = jdbc/reportdbxa
    service.id = 707
    ...
    ----
    aries.managed = true
    objectClass = [javax.sql.DataSource]
    osgi.jndi.service.name = jdbc/reportdbxa
    service.id = 708
    service.ranking = 1000
    ...
    ----
    objectClass = [..., org.springframework.context.ApplicationContext, ...]
    service.id = 709
    ...

What can be seen in the above output is that we have two pairs of data sources - for both `jdbc/reportdb` and
`jdbc/reportdbxa` we have original service and a service with `aries.managed=true` property and higher ranking.
This is why, when looking up a service with given name, we'll get _managed_ version that can perform JTA enlistment
(if needed).

Here's how `jpa-model` should be installed:

    features:install jpa hibernate
    install -s mvn:com.fusesource.examples.camel-persistence-part1/jpa-model/1.0

After installing and starting `jpa-model`, we can check the the services exposed by it:

    JBossFuse:karaf@root> ls com.fusesource.examples.camel-persistence-part1.jpa-model
    
    FuseSource :: Examples :: Camel Persistence :: JPA Model (321) provides:
    ------------------------------------------------------------------------
    objectClass = [javax.persistence.EntityManagerFactory]
    org.apache.aries.jpa.container.managed = true
    org.apache.aries.jpa.default.unit.name = false
    osgi.unit.name = reportincident-jta
    osgi.unit.provider = org.hibernate.ejb.HibernatePersistence
    osgi.unit.version = 1.0.0
    service.id = 677
    ----
    objectClass = [javax.persistence.EntityManagerFactory]
    org.apache.aries.jpa.container.managed = true
    org.apache.aries.jpa.default.unit.name = false
    osgi.unit.name = reportincident-local
    osgi.unit.provider = org.hibernate.ejb.HibernatePersistence
    osgi.unit.version = 1.0.0
    service.id = 678

We can see two `javax.persistence.EntityManagerFactory` services for two persistence units from `META-INF/persistence.xml`.

Let's install `jpa-spring` bundle that provides Spring XML based camel routes using JPA endpoints and RESOURCE_LOCAL transactions:

    features:install spring-orm
    features:install camel-jpa
    install -s mvn:com.fusesource.examples.camel-persistence-part1/jpa-spring/1.0

We can see contexts running:

    JBossFuse:karaf@root> context-list
     Context        Status              Total #       Failed #     Inflight #   Uptime        
     -------        ------              -------       --------     ----------   ------        
     camel          Started                  76              0              0   12 minutes    
    JBossFuse:karaf@root> route-list
     Context        Route                          Status              Total #       Failed #     Inflight #   Uptime        
     -------        -----                          ------              -------       --------     ----------   ------        
     camel          create-incident                Started                   0              0              0   12 minutes    
     camel          rollback-incident              Started                   0              0              0   12 minutes    
     camel          trigger-database               Started                  50              0              0   12 minutes    
     camel          trigger-database-named-query   Started                  26              0              0   12 minutes    

`trigger-database` and `trigger-database-named-query` routes poll from database and print the content of `report.t_incident` tables.

In order to test `create-incident` and `rollback-incident` routes, we can:

    $ cd $PROJECT_HOME/jpa-spring/data
    $ cp csv.txt $FUSE_HOME/data/camel/datainsert
    $ cp csv-one-record.txt $FUSE_HOME/data/camel/datainsert
    $ cp csv-notinserted.txt $FUSE_HOME/data/camel/datainsertrollback

Finally `jpa-blueprint` is the ultimate showcase of integrating JPA, Hibernate, JTA and Camel. It can be installed like this:

    features:install spring-orm
    features:install camel-jpa
    install -s mvn:com.fusesource.examples.camel-persistence-part1/jpa-blueprint/1.0

`jpa-blueprint` differs from `jpa-spring` in these:
* it references persistence unit with `transaction-type="JTA"`
* it doesn't create own `PlatformTransactionManager` and instead references global (JTA) transaction manager
* it injects `EntityManagerFactory` to `jpa` component using `<jpa:unit>` custom element
* it injects JTA-scoped `EntityManager` instance to `com.fusesource.examples.persistence.part1.camel.ProcessIncidents`
Camel bean using `<jpa:context>` custom element

In addition to 4 routes identical to the ones provided by `jpa-spring`, there's additional route `create-incident-using-jpa` which
invokes Camel bean which has JTA-scoped `EntityManager` instance injected. We can send incident messages to it using:

    $ echo -n 'panic!' > $FUSE_HOME/data/camel/datainsertjpa/incident

### Aries JPA internals

org.apache.aries.jpa.container bundle:

* `org.apache.aries.jpa.container.parsing.PersistenceDescriptorParser` service implemented by
`org.apache.aries.jpa.container.parsing.impl.PersistenceDescriptorParserImpl` - parses `META-INF/persistence.xml`
descriptors into `org.apache.aries.jpa.container.parsing.impl.PersistenceUnitImpl` objects using
`org.apache.aries.jpa.container.parsing.impl.JPAHandler`
* `org.apache.aries.jpa.container.impl.PersistenceBundleManager` - locates, parses and manages persistence units
defined in OSGi bundles. Contains `Map<Bundle, EntityManagerFactoryManager>`

Parsed `org.apache.aries.jpa.container.parsing.impl.PersistenceUnitImpl` is changed into
`org.apache.aries.jpa.container.unit.impl.ManagedPersistenceUnitInfoImpl` and `getPersistenceUnitInfo()` is called
to get `javax.persistence.spi.PersistenceUnitInfo` instance which is then finally used as argument to
`javax.persistence.spi.PersistenceProvider.createContainerEntityManagerFactory()`.

org.apache.aries.jpa.container.context bundle:

* provides contextual (proxied, thread-local) access to context-related `javax.persistence.EntityManager` instance
for Blueprint XMLs which use `<jpa:context>` custom element.
