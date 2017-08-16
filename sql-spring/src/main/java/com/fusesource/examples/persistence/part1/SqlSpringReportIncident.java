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
package com.fusesource.examples.persistence.part1;

import org.apache.camel.Body;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.sql.SqlEndpoint;

public class SqlSpringReportIncident {

    private ProducerTemplate template;

    @EndpointInject(ref="sqlEndpoint")
    protected SqlEndpoint endpoint;

    public void insertRecord(@Body String ref) {

        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append(
           "INSERT INTO REPORT.T_INCIDENT (INCIDENT_REF,INCIDENT_DATE," +
           "GIVEN_NAME,FAMILY_NAME,SUMMARY,DETAILS,EMAIL,PHONE) VALUES (");
        queryBuilder.append("'" + ref + "',");
        queryBuilder.append("'2011-03-22','Charles','Moulliard','Incident Webinar'," +
                "'This is a report incident for webinar-" + ref + "'," +
                "'cmoulliard@fusesource.com','+111 10 20 300')");

        String query = queryBuilder.toString();
        System.out.println(">>> Query created : " + query );

        endpoint.setQuery(query);
        template.send(endpoint,(Exchange) null) ;


    }

    public void setTemplate(ProducerTemplate template) {
        this.template = template;
    }

    public Object[] convertStringIntoArray(@Body String params) {
        return params.split(",");
    }

}
