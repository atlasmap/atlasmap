package io.atlasmap.itests.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.util.HashMap;

import org.junit.Test;
import org.xmlunit.assertj.XmlAssert;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasContextFactory;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContextFactory;

public class XMLAttributeTest {

    @Test
    public void test() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-xml-attribute-ns.json");
        AtlasContextFactory factory = DefaultAtlasContextFactory.getInstance();
        AtlasContext context = factory.createContext(url.toURI());
        AtlasSession session = context.createSession();
        String xmlSource = TestHelper.readStringFromFile("data/xml-source-attribute-ns.xml");
        session.setSourceDocument("po-example-schema-f81424a0-8871-4483-abaf-059cc432ea78", xmlSource);

        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        assertFalse(TestHelper.printAudit(session), session.hasWarns());
        Object target = session.getTargetDocument("po-example-schema-b12bc688-7bf3-4626-97a2-d8c3981ecd3a");
        assertNotNull("target XML is null", target);
        HashMap<String, String> namespaces = new HashMap<>();
        namespaces.put("ns1", "http://tempuri.org/po.xsd");
        XmlAssert.assertThat(target).withNamespaceContext(namespaces)
            .valueByXPath("//ns1:purchaseOrder/@ns1:orderDate").isEqualTo("1985-05-02");
        XmlAssert.assertThat(target).withNamespaceContext(namespaces)
        .valueByXPath("//ns1:purchaseOrder/ns1:comment").isEqualTo("1985-05-02");
    }

}
