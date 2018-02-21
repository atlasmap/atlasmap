package org.apache.camel.component.atlasmap;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
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

import io.atlasmap.java.test.SourceContact;

@RunWith(CamelSpringRunner.class)
@BootstrapWith(CamelTestContextBootstrapper.class)
@ContextConfiguration
public class AtlasMapMultiNSTest {

    private static final String XML_EXPECTED =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<tns:request xmlns:tns=\"http://syndesis.io/v1/swagger-connector-template/request\">\n" +
                    "  <tns:body>\n" +
                    "    <Pet>\n" +
                    "      <name>Jackson</name>\n" +
                    "    </Pet>\n" +
                    "  </tns:body>\n" +
                    "</tns:request>";

    @Autowired
    protected CamelContext camelContext;

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint result;

    @Test
    @DirtiesContext
    public void test() throws Exception {
        result.setExpectedCount(1);

        ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
        SourceContact c = new SourceContact();
        c.setFirstName("Jackson");
        producerTemplate.sendBody("direct:start", c);

        MockEndpoint.assertIsSatisfied(camelContext);
        Message message = result.getExchanges().get(0).getIn();
        Assert.assertEquals("application/xml", message.getHeader(Exchange.CONTENT_TYPE));
        String out = message.getBody(String.class);
        Assert.assertNotNull(out);
        Diff d = DiffBuilder.compare(Input.fromString(XML_EXPECTED).build())
                .withTest(Input.fromString(out).build())
                .ignoreWhitespace().build();
        Assert.assertFalse(d.toString() + ": " + out, d.hasDifferences());
    }

}
