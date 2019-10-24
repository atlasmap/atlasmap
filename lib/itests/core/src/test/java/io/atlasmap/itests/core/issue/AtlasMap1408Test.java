package io.atlasmap.itests.core.issue;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

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
 * https://github.com/atlasmap/atlasmap/issues/1408
 */
public class AtlasMap1408Test {

    private static Logger LOG = LoggerFactory.getLogger(AtlasMap1408Test.class);

    private AtlasMappingService mappingService;

    @Before
    public void before() {
        mappingService = DefaultAtlasContextFactory.getInstance().getMappingService();
    }

    @Test
    public void test() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/issue/atlasmap-1408-mapping.json");
        AtlasMapping mapping = mappingService.loadMapping(url);
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();
        url = Thread.currentThread().getContextClassLoader().getResource("mappings/issue/atlasmap-1408-source.xml");
        session.setSourceDocument("swagger-request", new String(Files.readAllBytes(Paths.get(url.toURI()))));
        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        Object outputJson = session.getTargetDocument("request");
        assertNotNull("target json document was null", outputJson);
        ObjectMapper om = new ObjectMapper();
        JsonNode expected = om.readTree(Thread.currentThread().getContextClassLoader().getResource("mappings/issue/atlasmap-1408-target.json"));
        JsonNode actual = om.readTree((String)outputJson);
        LOG.info(">>> output:target json >>> {}", actual.toString());
        assertTrue(actual.toString(), expected.equals(actual));
    }

}