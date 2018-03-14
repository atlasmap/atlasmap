package io.atlasmap.core.issue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.AtlasMappingService;
import io.atlasmap.core.AtlasMappingService.AtlasMappingFormat;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.core.TestHelper;
import io.atlasmap.v2.AtlasMapping;

public class Atlasmap233Test {

    private AtlasMappingService mappingService;

    @Before
    public void before() {
        mappingService = DefaultAtlasContextFactory.getInstance().getMappingService();
    }

    @Test
    public void test() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/issue/atlasmap-233-mapping.xml");
        AtlasMapping mapping = mappingService.loadMapping(url, AtlasMappingFormat.XML);
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();
        session.setSourceDocument("io.atlasmap.core.issue.SourceClass", new SourceClass().setSourceInteger(-1));
        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        Object output = session.getTargetDocument("io.atlasmap.core.issue.TargetClass");
        assertEquals(TargetClass.class, output.getClass());
        assertEquals(1, ((TargetClass)output).getTargetInteger());
    }

}
