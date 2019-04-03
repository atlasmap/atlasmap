package org.apache.camel.component.atlasmap;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;

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

@RunWith(CamelSpringRunner.class)
@BootstrapWith(CamelTestContextBootstrapper.class)
@ContextConfiguration
public class AtlasMapExtractMappingsTest {
	
    private static final String EXPECTED_BODY = "{\"order\":{\"orderId\":\"A123\"}}";

    @Autowired
    protected CamelContext camelContext;

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint result;

    @EndpointInject(uri = "mock:result-n")
    protected MockEndpoint result_n;

    @Test
    @DirtiesContext
    public void testXMLMappingsExtraction() throws Exception {
        result.setExpectedCount(1);

        ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
        producerTemplate.sendBody("direct:start",
            new ByteArrayInputStream("{ \"order\": { \"orderId\": \"A123\" }}".getBytes()));

        MockEndpoint.assertIsSatisfied(camelContext);

        final Object body = result.getExchanges().get(0).getIn().getBody();
        assertEquals(EXPECTED_BODY, body);
    }

    @Test
    @DirtiesContext
    public void testXMLMappingsExtractionNumberedMappingFile() throws Exception {
        result_n.setExpectedCount(1);

        ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
        producerTemplate.sendBody("direct:start-n",
            new ByteArrayInputStream("{ \"order\": { \"orderId\": \"A123\" }}".getBytes()));

        MockEndpoint.assertIsSatisfied(camelContext);

        final Object body = result_n.getExchanges().get(0).getIn().getBody();
        assertEquals(EXPECTED_BODY, body);
    }
}
