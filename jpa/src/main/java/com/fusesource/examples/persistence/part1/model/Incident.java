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
package com.fusesource.examples.persistence.part1.model;

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

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

@CsvRecord(separator = ",")
@Entity
@Table(schema = "REPORT", name = "T_INCIDENT")
@NamedQuery(name = "reportSummaryQuery",
            query = "SELECT rep.incidentRef, rep.familyName, rep.summary FROM Incident rep")
public class Incident extends Abstract implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "INCIDENT_REF", length = 55)
    @DataField(pos = 1)
    private String incidentRef;

    @Column(name = "INCIDENT_DATE")
    @DataField(pos = 2, pattern = "dd-mm-yyyy")
    private Date incidentDate;

    @Column(name = "GIVEN_NAME", length = 35)
    @DataField(pos = 3)
    private String givenName;

    @Column(name = "FAMILY_NAME", length = 35)
    @DataField(pos = 4)
    private String familyName;

    @Column(name = "SUMMARY", length = 35)
    @DataField(pos = 5)
    private String summary;

    @Column(name = "DETAILS")
    @DataField(pos = 6)
    private String details;

    @Column(name = "EMAIL", length = 60)
    @DataField(pos = 7)
    private String email;

    @Column(name = "PHONE", length = 35)
    @DataField(pos = 8)
    private String phone;

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name = "INCIDENT_ID")
    private long incidentId;

    @Column(name = "CREATION_USER")
    private String creationUser;

    @Column(name = "CREATION_DATE")
    private Date creationDate;

    public long getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(long incidentId) {
        this.incidentId = incidentId;
    }

    public String getIncidentRef() {
        return incidentRef;
    }

    public void setIncidentRef(String incidentRef) {
        this.incidentRef = incidentRef;
    }

    public Date getIncidentDate() {
        return incidentDate;
    }

    public void setIncidentDate(Date incidentDate) {
        this.incidentDate = incidentDate;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCreationUser() {
        return creationUser;
    }

    public void setCreationUser(String creationUser) {
        this.creationUser = creationUser;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
