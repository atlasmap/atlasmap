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

import com.redhat.ipaas.connector.salesforce.Contact;

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
        producerTemplate.sendBody("direct:start", generateTwitterStatus());

        MockEndpoint.assertIsSatisfied(camelContext);
        Object body = result.getExchanges().get(0).getIn().getBody();
        assertEquals(Contact.class, body.getClass());
        Contact output = (Contact) body;
        assertEquals("Bob", output.getFirstName());
        assertEquals("Vila", output.getLastName());
        assertEquals("bobvila1982", output.getTitle());
        assertEquals("Let's build a house!", output.getDescription());
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
