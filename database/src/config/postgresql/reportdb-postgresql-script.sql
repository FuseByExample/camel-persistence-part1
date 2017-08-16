--
--  Copyright 2005-2017 Red Hat, Inc.
--
--  Red Hat licenses this file to you under the Apache License, version
--  2.0 (the "License"); you may not use this file except in compliance
--  with the License.  You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
--  Unless required by applicable law or agreed to in writing, software
--  distributed under the License is distributed on an "AS IS" BASIS,
--  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
--  implied.  See the License for the specific language governing
--  permissions and limitations under the License.
--

drop schema if exists report cascade;

create schema report;

create table report.t_incident (
  incident_id serial not null primary key,
  incident_ref varchar(55),
  incident_date timestamp,
  given_name varchar(35),
  family_name varchar(35),
  summary varchar(35),
  details varchar(255),
  email varchar(60),
  phone varchar(35),
  creation_date timestamp,
  creation_user varchar(255)
);

insert into report.t_incident (incident_ref, incident_date, given_name, family_name, summary, details, email, phone)
values ('001', '2017-01-23 00:00:00', 'Charles', 'Moulliard', 'incident webinar-001', 'This is a report incident for webinar-001', 'cmoulliard@redhat.com', '+111 10 20 300');
insert into report.t_incident (incident_ref, incident_date, given_name, family_name, summary, details, email, phone)
values ('002', '2017-01-24 00:00:00', 'Charles', 'Moulliard', 'incident webinar-002', 'This is a report incident for webinar-002', 'cmoulliard@redhat.com', '+111 10 20 300');
insert into report.t_incident (incident_ref, incident_date, given_name, family_name, summary, details, email, phone)
values ('003', '2017-01-25 00:00:00', 'Charles', 'Moulliard', 'incident webinar-003', 'This is a report incident for webinar-003', 'cmoulliard@redhat.com', '+111 10 20 300');
insert into report.t_incident (incident_ref, incident_date, given_name, family_name, summary, details, email, phone)
values ('004', '2017-01-26 00:00:00', 'Charles', 'Moulliard', 'incident webinar-004', 'This is a report incident for webinar-004', 'cmoulliard@redhat.com', '+111 10 20 300');
