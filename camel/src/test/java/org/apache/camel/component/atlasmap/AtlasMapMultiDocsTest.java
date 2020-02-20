package org.apache.camel.component.atlasmap;

import static org.junit.Assert.assertEquals;
import static org.xmlunit.assertj.XmlAssert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.support.DefaultMessage;
import org.apache.camel.test.spring.CamelSpringRunner;
import org.apache.camel.test.spring.CamelTestContextBootstrapper;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.atlasmap.java.test.SourceContact;
import io.atlasmap.java.test.TargetContact;

@RunWith(CamelSpringRunner.class)
@BootstrapWith(CamelTestContextBootstrapper.class)
@ContextConfiguration
public class AtlasMapMultiDocsTest {

    private static final String JSON_SOURCE =
            "{" +
                    "\"firstName\": \"JsonFirstName\"," +
                    "\"lastName\": \"JsonLastName\"," +
                    "\"phoneNumber\": \"JsonPhoneNumber\"," +
                    "\"zipCode\": \"JsonZipCode\"" +
                    "}";

    private static final String XML_SOURCE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<ns:Contact xmlns:ns=\"http://atlasmap.io/xml/test/v2\"\n" +
                    "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "    firstName=\"XmlFirstName\" lastName=\"XmlLastName\"\n" +
                    "    phoneNumber=\"XmlPhoneNumber\" zipCode=\"XmlZipCode\" />\n";

    @Autowired
    protected CamelContext camelContext;

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint result;

    @EndpointInject(uri = "mock:result-body")
    protected MockEndpoint resultBody;

    @After
    public void after() {
        result.reset();
        resultBody.reset();
    }

    @Test
    @DirtiesContext
    public void test() throws Exception {
        result.setExpectedCount(1);

        Map<String, Message> sourceMap = new HashMap<>();
        SourceContact javaSource = new SourceContact();
        javaSource.setFirstName("JavaFirstName");
        javaSource.setLastName("JavaLastName");
        javaSource.setPhoneNumber("JavaPhoneNumber");
        javaSource.setZipCode("JavaZipCode");
        Message msg = new DefaultMessage(camelContext);
        msg.setBody(javaSource);
        sourceMap.put("DOCID:JAVA:CONTACT:S", msg);
        msg = new DefaultMessage(camelContext);
        msg.setBody(JSON_SOURCE);
        sourceMap.put("DOCID:JSON:CONTACT:S", msg);
        msg = new DefaultMessage(camelContext);
        msg.setBody(XML_SOURCE);
        sourceMap.put("DOCID:XML:CONTACT:S", msg);

        ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
        producerTemplate.sendBodyAndProperty("direct:start", null, "CAPTURED_OUT_MESSAGES_MAP", sourceMap);

        MockEndpoint.assertIsSatisfied(camelContext);
        Exchange exchange = result.getExchanges().get(0);
        Map<?, ?> targetMap = exchange.getProperty("TARGET_MESSAGES_MAP", Map.class);

        TargetContact javaTarget = (TargetContact) targetMap.get("DOCID:JAVA:CONTACT:T");
        assertEquals("JavaFirstName", javaTarget.getFirstName());
        assertEquals("XmlLastName", javaTarget.getLastName());
        assertEquals("JsonPhoneNumber", javaTarget.getPhoneNumber());

        String jsonTarget = (String) targetMap.get("DOCID:JSON:CONTACT:T");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonTargetNode = mapper.readTree(jsonTarget);
        assertEquals("JsonFirstName", jsonTargetNode.get("firstName").asText());
        assertEquals("JavaLastName", jsonTargetNode.get("lastName").asText());
        assertEquals("XmlPhoneNumber", jsonTargetNode.get("phoneNumber").asText());

        String xmlTarget = (String) targetMap.get("DOCID:XML:CONTACT:T");
        HashMap<String,String> ns = new HashMap<>();
        ns.put("ns", "http://atlasmap.io/xml/test/v2");
        assertThat(xmlTarget).withNamespaceContext(ns).valueByXPath("/Contact/@firstName").isEqualTo("XmlFirstName");
        assertThat(xmlTarget).withNamespaceContext(ns).valueByXPath("/Contact/@lastName").isEqualTo("JsonLastName");
        assertThat(xmlTarget).withNamespaceContext(ns).valueByXPath("/Contact/@phoneNumber").isEqualTo("JavaPhoneNumber");
    }

    @Test
    public void testBody() throws Exception {
        resultBody.setExpectedCount(1);

        Map<String, Object> sourceMap = new HashMap<>();
        SourceContact javaSource = new SourceContact();
        javaSource.setFirstName("JavaFirstName");
        javaSource.setLastName("JavaLastName");
        javaSource.setPhoneNumber("JavaPhoneNumber");
        javaSource.setZipCode("JavaZipCode");
        sourceMap.put("DOCID:JAVA:CONTACT:S", javaSource);
        sourceMap.put("DOCID:JSON:CONTACT:S", JSON_SOURCE);
        sourceMap.put("DOCID:XML:CONTACT:S", XML_SOURCE);

        ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
        producerTemplate.sendBody("direct:start-body", sourceMap);

        MockEndpoint.assertIsSatisfied(camelContext);
        Exchange exchange = resultBody.getExchanges().get(0);
        Map<?, ?> targetMap = exchange.getIn().getBody(Map.class);

        TargetContact javaTarget = (TargetContact) targetMap.get("DOCID:JAVA:CONTACT:T");
        assertEquals("JavaFirstName", javaTarget.getFirstName());
        assertEquals("XmlLastName", javaTarget.getLastName());
        assertEquals("JsonPhoneNumber", javaTarget.getPhoneNumber());

        String jsonTarget = (String) targetMap.get("DOCID:JSON:CONTACT:T");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonTargetNode = mapper.readTree(jsonTarget);
        assertEquals("JsonFirstName", jsonTargetNode.get("firstName").asText());
        assertEquals("JavaLastName", jsonTargetNode.get("lastName").asText());
        assertEquals("XmlPhoneNumber", jsonTargetNode.get("phoneNumber").asText());

        String xmlTarget = (String) targetMap.get("DOCID:XML:CONTACT:T");
        HashMap<String,String> ns = new HashMap<>();
        ns.put("ns", "http://atlasmap.io/xml/test/v2");
        assertThat(xmlTarget).withNamespaceContext(ns).valueByXPath("/Contact/@firstName").isEqualTo("XmlFirstName");
        assertThat(xmlTarget).withNamespaceContext(ns).valueByXPath("/Contact/@lastName").isEqualTo("JsonLastName");
        assertThat(xmlTarget).withNamespaceContext(ns).valueByXPath("/Contact/@phoneNumber").isEqualTo("JavaPhoneNumber");
    }

}
