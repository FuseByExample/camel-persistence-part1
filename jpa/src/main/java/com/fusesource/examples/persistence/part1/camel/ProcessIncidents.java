package com.fusesource.examples.persistence.part1.camel;

/**
 * Copyright 2011 FuseSource
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

import com.fusesource.examples.persistence.part1.model.Incident;
import org.apache.camel.Exchange;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class ProcessIncidents {

    public static int count = 0;

    public Incident extract(Exchange exchange) throws ParseException {

        Map<String, Object> model = (Map<String, Object>) exchange.getIn().getBody();
        String key = "com.fusesource.examples.persistence.part1.model.Incident";

        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String currentDate = format.format(new Date());
        Date creationDate = format.parse(currentDate);

        Incident incident = (Incident) model.get(key);
        incident.setCreationDate(creationDate);
        incident.setCreationUser("file");

        return incident;

    }

    public void generateError() throws Exception {
        System.out.println("+++++ We will generate an exception +++++, " + count + " redelivery !");
        count++;
        throw new Exception("Cannot connect to Database ....");
    }
}
