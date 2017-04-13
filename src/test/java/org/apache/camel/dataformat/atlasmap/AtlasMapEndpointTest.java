package org.apache.camel.dataformat.atlasmap;

import static org.junit.Assert.*;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringRunner;
import org.apache.camel.test.spring.CamelTestContextBootstrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;

@Ignore // TODO: Refactor or deprecate DataFormat support
@RunWith(CamelSpringRunner.class)
@BootstrapWith(CamelTestContextBootstrapper.class)
@ContextConfiguration
public class AtlasMapEndpointTest {
    @Autowired
    protected CamelContext camelContext;
 
    @EndpointInject(uri = "mock:result")
    protected MockEndpoint result;
 
    @Test
    @DirtiesContext
    public void testMocksAreValid() throws Exception {
    	result.setExpectedCount(1);
    	
    	ProducerTemplate producerTemplate = camelContext.createProducerTemplate();    	
    	producerTemplate.sendBody("direct:start", "Hello World!");

        MockEndpoint.assertIsSatisfied(camelContext);
    }
}
