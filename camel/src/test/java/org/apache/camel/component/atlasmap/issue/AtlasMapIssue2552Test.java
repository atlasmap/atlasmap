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
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RunWith(CamelSpringRunner.class)
@BootstrapWith(CamelTestContextBootstrapper.class)
@ContextConfiguration
public class AtlasMapIssue2552Test {

    @Autowired
    protected CamelContext camelContext;

    @EndpointInject(uri = "mock:result-old")
    protected MockEndpoint resultOld;

    @EndpointInject(uri = "mock:result-new")
    protected MockEndpoint resultNew;


    @Test
    public void testOld() throws Exception {
        String jsonSource = new String(Files.readAllBytes(Paths.get(
                this.getClass().getClassLoader().getResource(
                        "org/apache/camel/component/atlasmap/issue/2552-input.json").toURI())));
        ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
        producerTemplate.sendBody("direct:start-old", jsonSource);

        MockEndpoint.assertIsSatisfied(camelContext);
        ObjectMapper om = new ObjectMapper();
        Exchange exchange = resultOld.getExchanges().get(0);
        String target = exchange.getIn().getBody(String.class);
        JsonNode targetJsonOld = om.readTree(target);
        assertTarget(targetJsonOld);
        

    }

    @Test
    public void testNew() throws Exception {
        String jsonSource = new String(Files.readAllBytes(Paths.get(
                this.getClass().getClassLoader().getResource(
                        "org/apache/camel/component/atlasmap/issue/2552-input.json").toURI())));
        ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
        producerTemplate.sendBody("direct:start-new", jsonSource);

        MockEndpoint.assertIsSatisfied(camelContext);
        ObjectMapper om = new ObjectMapper();
        Exchange exchange = resultNew.getExchanges().get(0);
        String target = exchange.getIn().getBody(String.class);
        JsonNode targetJsonNew = om.readTree(target);
        assertTarget(targetJsonNew);
    }

    private void assertTarget(JsonNode root) {
        ArrayNode bodyArray = (ArrayNode) root.get("body");
        assertEquals(3, bodyArray.size());
        ObjectNode body1 = (ObjectNode) bodyArray.get(0);
        ObjectNode body2 = (ObjectNode) bodyArray.get(1);
        ObjectNode body3 = (ObjectNode) bodyArray.get(2);
        assertEquals(1111, body1.get("id").asInt());
        assertEquals(1, body1.get("completed").asInt());
        assertEquals("task1", body1.get("task").asText());
        assertEquals(2222, body2.get("id").asInt());
        assertTrue(body2.get("completed").isNull());
        assertEquals("task2", body2.get("task").asText());
        assertEquals(3333, body3.get("id").asInt());
        assertEquals(3, body3.get("completed").asInt());
        assertEquals("task3", body3.get("task").asText());
    }
}
