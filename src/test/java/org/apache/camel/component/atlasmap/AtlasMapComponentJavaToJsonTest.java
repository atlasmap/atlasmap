package org.apache.camel.component.atlasmap;

import static org.junit.Assert.*;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringRunner;
import org.apache.camel.test.spring.CamelTestContextBootstrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import twitter4j.Status;

@RunWith(CamelSpringRunner.class)
@BootstrapWith(CamelTestContextBootstrapper.class)
@ContextConfiguration
public class AtlasMapComponentJavaToJsonTest {
    @Autowired
    protected CamelContext camelContext;

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint result;

    @Test
    @DirtiesContext
    public void testMocksAreValid() throws Exception {
        result.setExpectedCount(1);

        ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
        producerTemplate.sendBody("direct:start", generateTwitterStatus());

        MockEndpoint.assertIsSatisfied(camelContext);
        Object body = result.getExchanges().get(0).getIn().getBody();
        assertEquals(String.class, body.getClass());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode sfJson = mapper.readTree((String)body);
        assertNotNull(sfJson.get("TwitterScreenName__c"));
        assertEquals("bobvila1982", sfJson.get("TwitterScreenName__c").asText());
    }

    protected Status generateTwitterStatus() {
        MockStatus status = new MockStatus();
        MockUser user = new MockUser();
        user.setName("Bob Vila");
        user.setScreenName("bobvila1982");
        status.setUser(user);
        status.setText("Let's build a house!");
        return status;
    }

}
