/**
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
package org.apache.camel.component.atlasmap;

import java.io.ByteArrayInputStream;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringRunner;
import org.apache.camel.test.spring.CamelTestContextBootstrapper;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

@RunWith(CamelSpringRunner.class)
@BootstrapWith(CamelTestContextBootstrapper.class)
@ContextConfiguration
public class AtlasMapComponentXmlToXmlTest {

    private static final String XML_EXPECTED = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><tns:Patient xmlns:tns=\"http://hl7.org/fhir\"><tns:id value=\"101138\"/></tns:Patient>";

    @Autowired
    protected CamelContext camelContext;

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint result;

    @Test
    @DirtiesContext
    public void testMocksAreValid() throws Exception {
        result.setExpectedCount(1);

        final ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
        producerTemplate.sendBody("direct:start", new ByteArrayInputStream("<tns:Patient xmlns:tns=\"http://hl7.org/fhir\"><tns:id value=\"101138\"></tns:id></tns:Patient>".getBytes()));

        MockEndpoint.assertIsSatisfied(camelContext);
        final String body = result.getExchanges().get(0).getIn().getBody(String.class);

        Assert.assertNotNull(body);

        Diff d = DiffBuilder.compare(Input.fromString(XML_EXPECTED).build())
                .withTest(Input.fromString(body).build())
                .ignoreWhitespace().build();
        Assert.assertFalse(d.toString() + ": " + body, d.hasDifferences());
    }

}
