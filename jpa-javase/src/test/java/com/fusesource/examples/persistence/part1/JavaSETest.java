/**
 *  Copyright 2005-2017 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package com.fusesource.examples.persistence.part1;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import com.fusesource.examples.persistence.part1.model.Incident;
import org.apache.aries.transaction.AriesTransactionManager;
import org.apache.aries.transaction.internal.AriesTransactionManagerImpl;
import org.apache.aries.transaction.jdbc.internal.ConnectionManagerFactory;
import org.apache.aries.transaction.jdbc.internal.XADataSourceMCFFactory;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.geronimo.transaction.log.HOWLLog;
import org.hibernate.cfg.Environment;
import org.hibernate.service.jta.platform.internal.AbstractJtaPlatform;
import org.junit.Test;
import org.postgresql.xa.PGXADataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tranql.connector.jdbc.KnownSQLStateExceptionSorter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * <p>JPA 2.0, 9.2 Bootstrapping in Java SE Environments</p>
 * <p>Test shows how JPA should be bootstrapped and used in pure Java SE environment - assuming Hibernate 4.2 as
 * JPA provider</p>
 */
public class JavaSETest {

    public static Logger LOG = LoggerFactory.getLogger(JavaSETest.class);

    /**
     * Using <em>application-managed, resource-local entity manager</em> with underlying transactions controlled by
     * the application through the {@link javax.persistence.EntityTransaction} API.
     */
    @Test
    public void bootstrapAndUseApplicationManagedResourceLocalEntityManager() throws SQLException {

        // Data source, which we'll inject to Hibernate provider
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://localhost:5432/reportdb");
        dataSource.setUsername("fuse");
        dataSource.setPassword("fuse");
        dataSource.setDefaultAutoCommit(false);
        dataSource.setMaxTotal(3);
        dataSource.setInitialSize(0);

        // properties passed to javax.persistence.Persistence.createEntityManagerFactory()
        // These may be properties from org.hibernate.ejb.AvailableSettings or any other used by the provider
        // The properties may also be configured inside /META-INF/persistence.xml descriptor
        // (see /persistence/persistence-unit/properties/property elements)
        Map<String, Object> emfProperties = new HashMap<>();

        // Hibernate provides these important implementations of org.hibernate.service.jdbc.connections.spi.ConnectionProvider:
        // (org.hibernate.service.jdbc.connections.internal.ConnectionProviderInitiator.initiateService())
        //  - org.hibernate.service.jdbc.connections.internal.DriverManagerConnectionProviderImpl
        //    requires usual url/driver/... properties
        //  - org.hibernate.service.jdbc.connections.internal.DatasourceConnectionProviderImpl
        //    obtains data source from JNDI or uses passed in javax.sql.DataSource instance
        //  - org.hibernate.ejb.connection.InjectedDataSourceConnectionProvider
        //    allows to use any given data source - this is used with container-managed entity managers
        //
        // the provider implementation is chosen:
        //  - check "hibernate.connection.provider_class" property
        //  - check "hibernate.connection.datasource" property - it may either be JNDI name or javax.sql.DataSource
        //    instance - DatasourceConnectionProviderImpl
        //  - check "hibernate.c3p0*" properties - c3p0 data source
        //  - check "hibernate.proxool*" properties - proxool data source
        //  - check "hibernate.connection.url" - DriverManagerConnectionProviderImpl
        //  - fallback to UserSuppliedConnectionProviderImpl which throws UnsupportedOperationException

        // setting "hibernate.connection.datasource" selects DatasourceConnectionProviderImpl
        emfProperties.put(Environment.DATASOURCE, dataSource);

        // INJECTION_DATA may further configure selected org.hibernate.service.jdbc.connections.spi.ConnectionProvider
        // this code works as well, but it's cleaner simply to use Environment.DATASOURCE in main emf properties
        /*
        Map<String, Object> connectionProviderProperties = new HashMap<>();
        connectionProviderProperties.put("dataSource", dataSource);
        emfProperties.put(ConnectionProviderInitiator.INJECTION_DATA, connectionProviderProperties);
        */

        // Hibernate provides these important implementations of org.hibernate.engine.transaction.spi.TransactionFactory:
        // (org.hibernate.engine.transaction.internal.TransactionFactoryInitiator.initiateService())
        //  - org.hibernate.engine.transaction.internal.jdbc.JdbcTransactionFactory
        //  - org.hibernate.engine.transaction.internal.jta.JtaTransactionFactory
        //  - org.hibernate.engine.transaction.internal.jta.CMTTransactionFactory
        //
        // the factory implementation is chosen:
        //  - check "hibernate.transaction.factory_class" property
        //  - if persistence-unit/@transaction-type="RESOURCE_LOCAL" - JdbcTransactionFactory
        //  - if persistence-unit/@transaction-type="JTA" - CMTTransactionFactory

        // Canonical way of obtaining EMF using persistence-unit name and properties
        // 7.3.2 Obtaining an Entity Manager Factory in a Java SE Environment
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("reportincident-local", emfProperties);
        LOG.info("EntityManagerFactory: {}", emf);

        // 7.2.2 Obtaining an Application-managed Entity Manager
        EntityManager em = emf.createEntityManager();
        LOG.info("EntityManager: {}", em);

        // 7.5.3: The EntityTransaction interface is used to control resource transactions on resource-local
        // entity managers.
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        Incident incident = new Incident();
        incident.setGivenName("John");
        incident.setFamilyName("Doe");
        incident.setCreationDate(new Date());
        incident.setIncidentRef("042");
        em.persist(incident);

        tx.commit();

        // close EM
        em.close();

        // close EMF
        emf.close();

        // verify the record is persisted
        boolean foundJohnDoe = false;
        try (Connection con = dataSource.getConnection()) {
            try (Statement st = con.createStatement()) {
                try (ResultSet rs = st.executeQuery("SELECT i.incident_id, i.family_name, i.incident_ref FROM report.t_incident i")) {
                    while (rs.next()) {
                        LOG.info("{} | {} | {}", rs.getInt(1), rs.getString(2), rs.getString(3));
                        foundJohnDoe |= rs.getString(2).equals("Doe");
                    }
                }
            }
        }
        assertTrue(foundJohnDoe);
    }

    /**
     * <p>Using <em>application-managed, JTA entity manager</em> with underlying transactions controlled
     * through JTA.</p>
     * <p>This test shows roughly how Aries-JPA implements interacts with JTA, not how Aries-JPA implements
     * <em>container-managed entity manager</em>.</p>
     */
    @Test
    public void bootstrapAndUseApplicationManagedJTAEntityManager() throws Exception {

        // Fully functional JTA transaction manager
        HOWLLog txLog = Helpers.createDefaultTransactionLog();
        txLog.doStart();
        AriesTransactionManager ariesTransactionManager = new AriesTransactionManagerImpl(1800, txLog);

        // JTA API
        final UserTransaction userTransaction = ariesTransactionManager;
        final TransactionManager transactionManager = ariesTransactionManager;

        // Data source configuration - similar to what aries.jdbc does using JCA, JDBC and JTA
        //
        // When Aries detects javax.sql.DataSource or javax.sql.XADataSource OSGi service it processes them
        // see: org.apache.aries.transaction.jdbc.internal.ManagedDataSourceFactory#register()

        // JDBC 4.2, 18 Relationship to Connectors
        // The "resource adapter's" functionality is similar to that provided by the JDBC interfaces
        // used in the Java EE platform to establish a connection with a data source:
        //  - javax.sql.DataSource
        //  - javax.sql.XADataSource
        //  - javax.sql.ConnectionPoolDataSource
        // EIS-specific adapter (i.e., JDBC driver) may interact with application server using
        // javax.resource.spi.ConnectionManager
        //
        // XA non-pooling, non-enlisting data sources - database specific.
        // Know how to interact with DB using XA protocol - this is what is usually declared in Blueprint or
        // Spring XML. They are not true javax.resource.spi.ManagedConnectionFactory objects
        XADataSource postgresql1 = newPGXADataSource("reportdb");
        XADataSource postgresql2 = newPGXADataSource("reportdb2");

        // JCA 1.6, 6.5.3 javax.resource.spi.ManagedConnectionFactory
        // This interface is implemented by Resource Adapter. Its createConnectionFactory() method
        // may return javax.resource.cci.ConnectionFactory for pure JCA access to EIS (DB) or any other
        // object that may be used for non-standard EIS access
        // When javax.resource.spi.ConnectionManager is passed to createConnectionFactory(), resource adapter
        // has chance to interact with application server during construction of connection factory
        //
        // non-XA, pooling, enlisting data sources
        ManagedConnectionFactory managedConnectionFactory1 = jcaManagedConnectionFactory(postgresql1);
        ManagedConnectionFactory managedConnectionFactory2 = jcaManagedConnectionFactory(postgresql2);

        // JCA 1.6, 6.5.2 javax.resource.spi.ConnectionManager
        // This interface is used by resource adapter to forward "get connection" request to app server
        // i.e., user gets connection from resource adapter, but resource adapter has a chance to
        // wrap/modify the connection with what application server provides
        // additional "services" provided by a connection effectively created by ConnectionManager include
        // pooling, XA transaction enlisting, etc.
        //
        // Aries uses org.apache.aries.transaction.jdbc.internal.ConnectionManagerFactory that wraps:
        //  - resource adapter specific javax.resource.spi.ManagedConnectionFactory
        //  - aplication specific javax.resource.spi.ConnectionManager
        ConnectionManagerFactory cmf1 = new ConnectionManagerFactory();
        cmf1.setManagedConnectionFactory(managedConnectionFactory1);
        cmf1.setTransactionManager(ariesTransactionManager); // will be used by chosen javax.resource.spi.ConnectionManager
        cmf1.setConnectionMaxIdleMinutes(10);
        cmf1.setConnectionMaxWaitMilliseconds(5000);
        cmf1.setPartitionStrategy("none"); // "by-connector-properties", "by-subject"
        cmf1.setPooling(true);
        cmf1.setPoolMaxSize(10);
        cmf1.setPoolMinSize(0);
        cmf1.setTransaction("xa"); // "none", "local"
        cmf1.init();
        ConnectionManager cm1 = cmf1.getConnectionManager();

        ConnectionManagerFactory cmf2 = new ConnectionManagerFactory();
        cmf2.setManagedConnectionFactory(managedConnectionFactory2);
        cmf2.setTransactionManager(ariesTransactionManager); // will be used by chosen javax.resource.spi.ConnectionManager
        cmf2.setConnectionMaxIdleMinutes(10);
        cmf2.setConnectionMaxWaitMilliseconds(5000);
        cmf2.setPartitionStrategy("none"); // "by-connector-properties", "by-subject"
        cmf2.setPooling(true);
        cmf2.setPoolMaxSize(10);
        cmf2.setPoolMinSize(0);
        cmf2.setTransaction("xa"); // "none", "local"
        cmf2.init();
        ConnectionManager cm2 = cmf2.getConnectionManager();

        // Aries will use tranql to create "connection factory" which is not pure javax.resource.cci.ConnectionFactory
        // In fact, this "connection factory" will be javax.sql.DataSource (not javax.sql.XADataSource)
        //
        // org.tranql.connector.jdbc.TranqlDataSource will interact with javax.resource.spi.ConnectionManager
        // from geronimo-connector which, when asked to allocateConnection() will:
        //  - use interceptors configured in org.apache.geronimo.connector.outbound.GenericConnectionManager
        //  - delegate to javax.resource.spi.ManagedConnectionFactory.createManagedConnection() to get
        //    javax.resource.spi.ManagedConnection which eventually wraps physical connection
        DataSource cf1 = (DataSource) managedConnectionFactory1.createConnectionFactory(cm1);
        DataSource cf2 = (DataSource) managedConnectionFactory2.createConnectionFactory(cm2);

        // Let's use JPA

        Map<String, Object> emfProperties = new HashMap<>();
        emfProperties.put(Environment.DATASOURCE, cf1);

        // In OSGi, org.hibernate.osgi.OsgiJtaPlatform is used to retrieve UserTransaction and TransactionManager
        // instances from OSGi
        // here we'll use class based on org.springframework.orm.hibernate4.ConfigurableJtaPlatform from spring-orm
        emfProperties.put(Environment.JTA_PLATFORM, new AbstractJtaPlatform() {
            @Override
            protected TransactionManager locateTransactionManager() {
                return transactionManager;
            }

            @Override
            protected UserTransaction locateUserTransaction() {
                return userTransaction;
            }
        });

        // 7.3.2 Obtaining an Entity Manager Factory in a Java SE Environment
        // https://hibernate.atlassian.net/browse/HHH-6651 - can't invoke SchemaExport (part of EMF creation)
        // while JTA TX is active
//        userTransaction.begin();
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("reportincident-jta", emfProperties);
        LOG.info("EntityManagerFactory: {}", emf);
//        userTransaction.commit();

        // now DDL should be executed and we should have access to report.t_incident table
        // Let's write some records using plain JDBC in the context of JTA transaction

        // with rollback - no effect
        userTransaction.begin();
        Connection jdbc = cf1.getConnection();
        int rowCount = jdbc.createStatement()
                .executeUpdate("insert into report.t_incident (incident_id, given_name, family_name) values (nextval('hibernate_sequence'), 'Johnny', 'Mnemonic')");
        assertThat(rowCount, equalTo(1));
        userTransaction.setRollbackOnly();
        jdbc.close();
        userTransaction.rollback();

        // commit - row inserted
        userTransaction.begin();
        jdbc = cf1.getConnection();
        rowCount = jdbc.createStatement()
                .executeUpdate("insert into report.t_incident (incident_id, given_name, family_name) values (nextval('hibernate_sequence'), 'Johnny', 'Mnemonic')");
        assertThat(rowCount, equalTo(1));
        jdbc.close();
        userTransaction.commit();

        // 7.2.2 Obtaining an Application-managed Entity Manager
        EntityManager em = emf.createEntityManager();
        LOG.info("EntityManager: {}", em);

        // 7.5.1 JTA EntityManagers

        // Starting global JTA transaction
        userTransaction.begin();

        // from Javadoc: Indicate to the entity manager that a JTA transaction is active. This method should be called
        // on a JTA application managed entity manager that was created outside the scope of the active transaction to
        // associate it with the current JTA transaction.
        em.joinTransaction();

        Incident incident = new Incident();
        incident.setGivenName("John");
        incident.setFamilyName("Doe");
        incident.setCreationDate(new Date());
        incident.setIncidentRef("042");
        em.persist(incident);

        // accessing 2nd XA Resource
        jdbc = cf2.getConnection();
        jdbc.createStatement().executeUpdate("drop table if exists report.users");
        jdbc.createStatement().executeUpdate("create table report.users (id integer primary key, name varchar(50))");
        rowCount = jdbc.createStatement()
                .executeUpdate("insert into report.users (id, name) values (1, 'Johnny Mnemonic')");
        assertThat(rowCount, equalTo(1));
        jdbc.close();

        userTransaction.commit();

        // close EM
        em.close();

        // close EMF
        emf.close();

        // verify the record is persisted in 1st database
        boolean foundJohnDoe = false;
        boolean foundJohnnyMnemonic = false;
        try (Connection con = postgresql1.getXAConnection().getConnection()) {
            try (Statement st = con.createStatement()) {
                try (ResultSet rs = st.executeQuery("SELECT i.incident_id, i.family_name, i.incident_ref FROM report.t_incident i")) {
                    while (rs.next()) {
                        LOG.info("{} | {} | {}", rs.getInt(1), rs.getString(2), rs.getString(3));
                        foundJohnDoe |= rs.getString(2).equals("Doe");
                        foundJohnnyMnemonic |= rs.getString(2).equals("Mnemonic");
                    }
                }
            }
        }
        assertTrue(foundJohnDoe && foundJohnnyMnemonic);

        // verify the record is persisted in 2nd database
        foundJohnnyMnemonic = false;
        try (Connection con = postgresql2.getXAConnection().getConnection()) {
            try (Statement st = con.createStatement()) {
                try (ResultSet rs = st.executeQuery("SELECT u.id, u.name FROM report.users u")) {
                    while (rs.next()) {
                        LOG.info("2nd DB: {} | {}", rs.getInt(1), rs.getString(2));
                        foundJohnnyMnemonic |= rs.getString(2).equals("Johnny Mnemonic");
                    }
                }
            }
        }
        assertTrue(foundJohnnyMnemonic);
    }

    /**
     * Helper method to create PostgreSQL related data source
     * @param dbName
     * @return
     */
    private PGXADataSource newPGXADataSource(String dbName) {
        PGXADataSource ds = new PGXADataSource();
        ds.setApplicationName("Fuse Test");
        ds.setDatabaseName(dbName);
        ds.setUser("fuse");
        ds.setPassword("fuse");
        ds.setUrl("jdbc:postgresql://localhost:5432/" + dbName);
        return ds;
    }

    private ManagedConnectionFactory jcaManagedConnectionFactory(XADataSource xaDataSource) throws Exception {
        XADataSourceMCFFactory mcf = new XADataSourceMCFFactory();
        mcf.setDataSource(xaDataSource);
        mcf.setUserName("fuse");
        mcf.setPassword("fuse");
        mcf.setExceptionSorter(new KnownSQLStateExceptionSorter() {
            @Override
            protected boolean checkSQLState(String sqlState) {
//                if ("0A000".equals(sqlState)) {
//                    // java.sql.SQLFeatureNotSupportedException: Method org.postgresql.jdbc.PgConnection.createClob() is not yet implemented.
//                    return false;
//                }
                return super.checkSQLState(sqlState);
            }
        });
        mcf.init();
        return mcf.getConnectionFactory();
    }

}
