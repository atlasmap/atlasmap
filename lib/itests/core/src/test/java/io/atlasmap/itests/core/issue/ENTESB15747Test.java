package io.atlasmap.itests.core.issue;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlunit.assertj.XmlAssert;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.itests.core.TestHelper;

/**
 * https://issues.redhat.com/browse/ENTESB-15747 .
 */
public class ENTESB15747Test {

    private static final Logger LOG = LoggerFactory.getLogger(ENTESB15747Test.class);

    @Test
    public void test() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/issue/entesb-15747-mapping.json");
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
        AtlasSession session = context.createSession();
        URL in1 = Thread.currentThread().getContextClassLoader().getResource("data/issue/entesb-15747-source1.json");
        String source1 = new String(Files.readAllBytes(Paths.get(in1.toURI())));
        URL in2 = Thread.currentThread().getContextClassLoader().getResource("data/issue/entesb-15747-source2.json");
        String source2 = new String(Files.readAllBytes(Paths.get(in2.toURI())));
        session.setSourceDocument("-MT5ptb_dciq61nmn53T", source1);
        session.setSourceDocument("-MT5pxPYdciq61nmn53T", source2);
        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        Object target = session.getTargetDocument("-MT5pwFQdciq61nmn53T");
        assertNotNull(target);
        LOG.info(target.toString());
        HashMap<String, String> namespaces = new HashMap<>();
        namespaces.put("tns", "http://hl7.org/fhir");
        XmlAssert targetAssert = XmlAssert.assertThat(target).withNamespaceContext(namespaces);
        targetAssert.valueByXPath("//tns:Transaction/tns:Patient/tns:id/@tns:value").isEqualTo("ls1 ls2 ls3");
        targetAssert.valueByXPath("//tns:Transaction/tns:Patient/tns:name[1]/tns:given/@tns:value").isEqualTo("fn1");
        targetAssert.valueByXPath("//tns:Transaction/tns:Patient/tns:name[2]/tns:given/@tns:value").isEqualTo("fn2");
        targetAssert.valueByXPath("//tns:Transaction/tns:Patient/tns:name[3]/tns:given/@tns:value").isEqualTo("fn3");
        targetAssert.valueByXPath("//tns:Transaction/tns:Patient/tns:name[1]/tns:family/@tns:value").isEqualTo("ln1");
        targetAssert.valueByXPath("//tns:Transaction/tns:Patient/tns:name[2]/tns:family/@tns:value").isEqualTo("ln2");
        targetAssert.valueByXPath("//tns:Transaction/tns:Patient/tns:name[3]/tns:family/@tns:value").isEqualTo("ln3");
        targetAssert.valueByXPath("//tns:Transaction/tns:Basic/tns:id/@tns:value").isEqualTo("1 2 3");
        targetAssert.valueByXPath("//tns:Transaction/tns:Basic/tns:language/@tns:value").isEqualTo("t1 t2 t3");
    }

}
