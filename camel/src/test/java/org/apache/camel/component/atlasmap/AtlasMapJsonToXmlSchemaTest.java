package org.apache.camel.component.atlasmap;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.io.StringReader;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringRunner;
import org.apache.camel.test.spring.CamelTestContextBootstrapper;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;

import io.atlasmap.xml.core.schema.AtlasXmlSchemaSetParser;

@RunWith(CamelSpringRunner.class)
@BootstrapWith(CamelTestContextBootstrapper.class)
@ContextConfiguration
public class AtlasMapJsonToXmlSchemaTest {
    private static final Logger LOG = LoggerFactory.getLogger(AtlasMapJsonToXmlSchemaTest.class);

    @Autowired
    protected CamelContext camelContext;

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint result;

    @After
    public void after() {
        result.reset();
    }

    @Test
    @DirtiesContext
    public void test() throws Exception {
        result.setExpectedCount(1);

        final ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        InputStream payloadIs = tccl.getResourceAsStream("json-source.json");
        producerTemplate.sendBody("direct:start", payloadIs);

        MockEndpoint.assertIsSatisfied(camelContext);
        final String body = result.getExchanges().get(0).getIn().getBody(String.class);
        assertNotNull(body);
        LOG.debug(">>>>> {}", body);

        InputStream schemaIs = tccl.getResourceAsStream("xml-target-schemaset.xml");
        AtlasXmlSchemaSetParser schemaParser = new AtlasXmlSchemaSetParser(tccl);
        Validator validator = schemaParser.createSchema(schemaIs).newValidator();
        StreamSource source = new StreamSource(new StringReader(body));
        validator.validate(source);
    }

}
