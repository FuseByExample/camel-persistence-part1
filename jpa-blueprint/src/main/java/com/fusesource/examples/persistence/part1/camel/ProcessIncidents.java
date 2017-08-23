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
package com.fusesource.examples.persistence.part1.camel;

import com.fusesource.examples.persistence.part1.model.Incident;
import org.apache.camel.Exchange;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import javax.persistence.EntityManager;

public class ProcessIncidents {

    public static int count = 0;

    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public Incident extract(Exchange exchange) throws ParseException {

        Incident incident = exchange.getIn().getBody(Incident.class);

        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String currentDate = format.format(new Date());
        Date creationDate = format.parse(currentDate);

        incident.setCreationDate(creationDate);
        incident.setCreationUser("file");

        return incident;

    }

    @SuppressWarnings("unchecked")
    public void createIncident(Exchange exchange) throws ParseException {

        String summary = exchange.getIn().getBody(String.class);

        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String currentDate = format.format(new Date());
        Date creationDate = format.parse(currentDate);

        Incident incident = new Incident();
        incident.setCreationDate(creationDate);
        incident.setCreationUser("file");
        incident.setSummary(summary);
        entityManager.persist(incident);
        System.out.println("#### Created incident: " + incident);
    }

    public void generateError() throws Exception {
        System.out.println("+++++ We will generate an exception +++++, " + count + " redelivery !");
        count++;
        throw new Exception("Cannot connect to Database ....");
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

}
