package io.atlasmap.itests.core.issue;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.AtlasMappingService;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.itests.core.TestHelper;
import io.atlasmap.v2.AtlasMapping;

/**
 * https://github.com/atlasmap/atlasmap/issues/759 .
 */
public class Atlasmap759Test {

    private static final Logger LOG = LoggerFactory.getLogger(Atlasmap759Test.class);

    private AtlasMappingService mappingService;

    @Before
    public void before() {
        mappingService = DefaultAtlasContextFactory.getInstance().getMappingService();
    }

    @Test
    public void test() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/issue/atlasmap-759-mapping.json");
        AtlasMapping mapping = mappingService.loadMapping(url);
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("mappings/issue/atlasmap-759-source.json");
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        StringBuilder buf = new StringBuilder();
        String line;
        while((line = r.readLine()) != null) {
            buf.append(line);
        }
        r.close();
        session.setSourceDocument("-LYbkepiv8lNqAFpXmwF", buf.toString());
        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        Object output = session.getTargetDocument("-LYbkkbvv8lNqAFpXmwF");
        assertNotNull(output);
        in = Thread.currentThread().getContextClassLoader().getResourceAsStream("mappings/issue/atlasmap-759-target.json");
        ObjectMapper om = new ObjectMapper();
        JsonNode expected = om.readTree(in);
        JsonNode actual = om.readTree((String)output);
        LOG.info(">>> output >>> {}", actual.toString());
        assertTrue(actual.toString(), expected.equals(actual));
    }

}
