/*
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.atlasmap.issue;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringRunner;
import org.apache.camel.test.spring.CamelTestContextBootstrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;

import io.atlasmap.java.test.SourceContact;
import io.atlasmap.java.test.TargetContact;

@RunWith(CamelSpringRunner.class)
@BootstrapWith(CamelTestContextBootstrapper.class)
@ContextConfiguration
public class AtlasMapIssue729Test {

    @Autowired
    protected CamelContext camelContext;

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint result;


    @Test
    public void test() throws Exception {
        SourceContact javaSource = new SourceContact();
        javaSource.setFirstName("JavaFirstName");
        javaSource.setLastName("JavaLastName");
        javaSource.setPhoneNumber("JavaPhoneNumber");
        javaSource.setZipCode("JavaZipCode");
        ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
        producerTemplate.sendBody("direct:start", javaSource);

        MockEndpoint.assertIsSatisfied(camelContext);
        Exchange exchange = result.getExchanges().get(0);
        Map<?, ?> targetMap = exchange.getIn().getBody(Map.class);
        TargetContact javaTarget = (TargetContact) targetMap.get("DOCID:JAVA:CONTACT:T");
        assertEquals("JavaFirstName", javaTarget.getFirstName());

    }
}
