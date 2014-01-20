<!--
  ~ Copyright 2011 FuseSource
  ~
  ~    Licensed under the Apache License, Version 2.0 (the "License");
  ~    you may not use this file except in compliance with the License.
  ~    You may obtain a copy of the License at
  ~
  ~        http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~    Unless required by applicable law or agreed to in writing, software
  ~    distributed under the License is distributed on an "AS IS" BASIS,
  ~    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~    See the License for the specific language governing permissions and
  ~    limitations under the License.
  -->

DESCRIPTION
===========

This is the material accompanying the presentation of part I - Database persistence with Camel from simple to more elaborated.
It covers the different demo made during the talk and is organised like that :

database = project containing the scripts to create the database in HSQLDB and the jar file of the HSQLDB server
jdbc = maven project for camel-jdbc demo
sql = idem but for camel-sql component
jpa = mavemn project containing camel routes for JPA persistence
sql-spring-persistence = maven projetc with additional examples (not covered during the webinars) but could be used to test transaction with SQL component


H2 DATABASE
===========

    1) Open a DOS/UNIX console in the folder persistence/database

    2) Download H2 Database (http://www.h2database.com/html/download.html) and install it

    3) Start H2 Server using the bat or shell script

        ./h2.sh &

        The H2 server is started and to manage the databases from your web browser, simply click on the following url http://192.168.1.2:8082/

    4) Next create the report database

        In the login.jsp screen, select Generic (H2) - Server
        Add as settings name : Generic H2 (Server) - Webinar
        and modify the JDBC ur las such : jdbc:h2:tcp://localhost/~/reportdb

        Next click on "connect" and the screen to manage the reportdb appears

    5) Create Schema and Tables using the script located in the file persistence/database/src/config/hsqldb/reportdb-h2-scripts.sql

        Execute the scripts 1), 2) and 3) defined in this file

        Check that the records are well created using the command : SELECT * FROM REPORT.T_INCIDENT;


HSQL DATABASE
=============

    1) Open a DOS/UNIX console in the folder persistence/database

    2) Download HSQLDB Database (http://hsqldb.org) and install it

    3) Start HSQLDB Server using the command and lib jar provided in database directory

        java -cp lib/hsqldb.jar org.hsqldb.Server -database.0 file:db/reportdb -dbname.0 reportdb

    4) In a separate DOS/UNIX console, start the Swing DataBase Manager console using the following command

        java -cp lib/hsqldb.jar org.hsqldb.util.DatabaseManagerSwing --user sa --url jdbc:hsqldb:hsql://localhost/reportdb

    5) Create Schema and Tables using the script located in the file persistence/database/src/config/hsqldb/reportdb-hsqldb-scripts.sql

        Execute the scripts 1), 2) and 3) defined in this file

MAVEN
=====

Except for the database directory, the other directories are maven projects. The webinar
part1 can be build using the command

mvn clean install

in the camel-persistence-part1 directory.

To launch each project individually, simply execute the following command in a DOS/UNIX console

mvn camel:run

Depending in which project you are (jdbc, sql-spring or jpa), you will have to copy files
to allow the file:// endpoint of the camel routes to read the corresponding file (key.txt, keys.txt or csv.txtx) and insert data
into the database


1) jdbc folder
cd camel-persistence-part1/jdbc
cp data/keys.txt target/data/

2) sql-spring folder
cd camel-persistence-part1/sql-spring
cp data/key.txt target/datainsert/
cp data/keyParams.txt target/datainsertparams/

3) jpa folder
cd camel-persistence-part1/jpa
cp data/csv.txt target/datainsert/
cp data/csv-one-record.txt target/datainsert/
cp data/csv-notinserted.txt target/datainsertrollback/
more target/datainsertrollback/failed/csv-notinserted.txt


