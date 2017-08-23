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

import java.util.List;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolverHolder;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPA 2.0, 9.3 Determining the Available Persistence Providers
 */
public class DiscoveryTest {

    public static Logger LOG = LoggerFactory.getLogger(DiscoveryTest.class);

    @Test
    public void discovery() {
        // each API JAR may provide special version of PersistenceProviderResolverHolder
        //  - org.hibernate.javax.persistence:hibernate-jpa-2.0-api:1.0.1.Final
        //  - org.apache.geronimo.specs:geronimo-jpa_2.0_spec:1.1
        LOG.info("Location of javax.persistence.spi.PersistenceProviderResolverHolder class: {}",
                PersistenceProviderResolverHolder.class.getProtectionDomain().getCodeSource().getLocation());

        List<PersistenceProvider> persistenceProviders = PersistenceProviderResolverHolder.getPersistenceProviderResolver().getPersistenceProviders();
        LOG.info("Available persistence providers: {}", persistenceProviders);
    }

}
