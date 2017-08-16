# Description

This is the material accompanying the presentation of part I - Database persistence with Camel from simple to more elaborated.
It covers the different demos made during the talk and is organised like that:

* `database`: directory containing the scripts to create the database for H2, HSQLDB and PostgreSQL RDBMSes
* `jdbc-spring`: Maven project using camel-jdbc component and Spring XML DSL
* `jdbc-blueprint`: Maven project using camel-jdbc component and Blueprint XML DSL

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

2. Create `reportdb` database by from the `fuse-postgresql-server` container:

        $ docker exec -ti fuse-postgresql-server /bin/bash
        root@3e37b4b579c7:/# psql -U fuse -d fuse
        psql (9.4.0)
        Type "help" for help.
        fuse=# create database reportdb owner fuse encoding 'utf8';
        CREATE DATABASE
        fuse=# \q

3. Initialize database `reportdb` by creating schema, table and populating the table with data.

    $ cd $PROJECT_HOME/database
    $ docker cp src/config/postgresql/reportdb-postgresql-script.sql fuse-postgresql-server:/tmp
    $ docker exec -ti fuse-postgresql-server /bin/bash
    $ root@58b0d9de9c5b:/# psql -U fuse -d reportdb -f /tmp/reportdb-postgresql-script.sql 
    ...
    DROP SCHEMA
    CREATE SCHEMA
    CREATE TABLE
    INSERT 0 1
    INSERT 0 1
    INSERT 0 1
    INSERT 0 1

# Running examples

## `jdbc-spring` and `jdbc-blueprint`

These examples can be run using camel-test-spring and camel-test-blueprint respectively. Examples will run outside
of Fuse server and require only running database server.

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

    $ cd $PROJECT_HOME/jdbc-spring
    $ mvn clean package camel:run

The invocation is slightly different - `package` goal has to be invoked, so we have proper OSGi bundle - or rather
proper OSGi bundle `MANIFEST.MF` file generated in `target/classes/META-INF` directory. This allows `camel-test-blueprint`
to _pick up_ `target/classes` directory as correct OSGi bundle.