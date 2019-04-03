package io.atlasmap.itests.core.issue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.xmlunit.assertj.XmlAssert.assertThat;

import java.net.URL;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.AtlasMappingService;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.itests.core.TestHelper;
import io.atlasmap.v2.AtlasMapping;

/**
 * https://github.com/atlasmap/atlasmap/issues/846
 */
public class AtlasMap846Test {

    private static Logger LOG = LoggerFactory.getLogger(Atlasmap759Test.class);

    private AtlasMappingService mappingService;

    @Before
    public void before() {
        mappingService = DefaultAtlasContextFactory.getInstance().getMappingService();
    }

    @Test
    public void test() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/issue/atlasmap-846-mapping.json");
        AtlasMapping mapping = mappingService.loadMapping(url);
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();
        session.setSourceDocument("source", "[]");
        context.process(session);

        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        Object outputJson = session.getTargetDocument("target-json");
        assertNotNull("target-json document was null", outputJson);
        ObjectMapper om = new ObjectMapper();
        JsonNode expected = om.readTree("{\"body\":[],\"three\":[]}");
        JsonNode actual = om.readTree((String)outputJson);
        LOG.info(">>> output:target-json >>> {}", actual.toString());
        assertTrue(actual.toString(), expected.equals(actual));

        Object outputXml = session.getTargetDocument("target-xml");
        assertNotNull("target-xml document was null", outputXml);
        assertThat(outputXml).nodesByXPath("/root").exist();
        LOG.info(">>> output:target-xml >>> {}", outputXml.toString());

        Object outputJava = session.getTargetDocument("target-java");
        assertNotNull("target-java document was null", outputJava);
        assertEquals(TargetClass.class, outputJava.getClass());
        TargetClass targetClass = (TargetClass)outputJava;
        assertEquals(0, targetClass.getTargetList().size());
        assertEquals(0, targetClass.getTargetStringList().size());
    }

    @Test
    public void testHappyPath() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/issue/atlasmap-846-mapping.json");
        AtlasMapping mapping = mappingService.loadMapping(url);
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();
        session.setSourceDocument("source", "[{\"first_name\":\"Tom\",\"last_name\":\"Silva\",\"three\":\"three\"}]");
        context.process(session);

        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        Object outputJson = session.getTargetDocument("target-json");
        assertNotNull("target-json document was null", outputJson);
        ObjectMapper om = new ObjectMapper();
        JsonNode expected = om.readTree("{\"body\":[{\"A\":\"Tom\",\"B\":\"Silva\"}],\"three\":[\"three\"]}");
        JsonNode actual = om.readTree((String)outputJson);
        LOG.info(">>> output:target-json >>> {}", actual.toString());
        assertTrue(actual.toString(), expected.equals(actual));

        Object outputXml = session.getTargetDocument("target-xml");
        assertNotNull("target-xml document was null", outputXml);
        assertThat(outputXml).valueByXPath("/root/body/A").isEqualTo("Tom");
        assertThat(outputXml).valueByXPath("/root/body/B").isEqualTo("Silva");
        assertThat(outputXml).valueByXPath("/root/three").isEqualTo("three");
        LOG.info(">>> output:target-xml >>> {}", outputXml.toString());

        Object outputJava = session.getTargetDocument("target-java");
        assertNotNull("target-java document was null", outputJava);
        assertEquals(TargetClass.class, outputJava.getClass());
        TargetClass targetClass = (TargetClass)outputJava;
        assertEquals(1, targetClass.getTargetList().size());
        assertEquals(1, targetClass.getTargetStringList().size());
    }

}
