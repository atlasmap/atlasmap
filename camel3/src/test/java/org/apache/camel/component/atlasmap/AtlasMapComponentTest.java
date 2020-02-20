package org.apache.camel.component.atlasmap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

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
public class AtlasMapComponentTest {
    @Autowired
    protected CamelContext camelContext;

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint result;

    @Test
    @DirtiesContext
    public void testMocksAreValid() throws Exception {
        result.setExpectedCount(1);

        ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
        producerTemplate.sendBody("direct:start", Util.generateMockTwitterStatus());

        MockEndpoint.assertIsSatisfied(camelContext);
        Object body = result.getExchanges().get(0).getIn().getBody();
        assertEquals(String.class, body.getClass());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode outJson = mapper.readTree((String)body);
        assertEquals("Bob", outJson.get("FirstName").asText());
        assertEquals("Vila", outJson.get("LastName").asText());
        assertEquals("bobvila1982", outJson.get("Title").asText());
        assertEquals("Let's build a house!", outJson.get("Description").asText());
        assertEquals("const foobar", outJson.get("Constant").asText());
    }

    @Test
    @DirtiesContext
    public void testSeparateNotSucceed() throws Exception {
        result.setExpectedCount(1);

        ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
        Status s = Util.generateMockTwitterStatus();
        when(s.getUser().getName()).thenReturn("BobVila");
        producerTemplate.sendBody("direct:start", s);

        MockEndpoint.assertIsSatisfied(camelContext);
        Object body = result.getExchanges().get(0).getIn().getBody();
        assertEquals(String.class, body.getClass());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode outJson = mapper.readTree((String)body);
        assertEquals("BobVila", outJson.get("FirstName").asText());
        assertNull(outJson.get("LastName"));
        assertEquals("bobvila1982", outJson.get("Title").asText());
        assertEquals("Let's build a house!", outJson.get("Description").asText());
    }
}
