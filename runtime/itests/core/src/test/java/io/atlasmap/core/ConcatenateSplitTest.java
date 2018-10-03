package io.atlasmap.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.AtlasMappingService.AtlasMappingFormat;
import io.atlasmap.core.issue.SourceClass;
import io.atlasmap.core.issue.TargetClass;
import io.atlasmap.v2.AtlasMapping;

public class ConcatenateSplitTest {

    private AtlasMappingService mappingService;

    @Before
    public void before() {
        mappingService = DefaultAtlasContextFactory.getInstance().getMappingService();
    }

    @Test
    public void test() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-concatenate-split.xml");
        AtlasMapping mapping = mappingService.loadMapping(url, AtlasMappingFormat.XML);
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();
        SourceClass source = new SourceClass()
                .setSourceFirstName("Manjiro")
                .setSourceLastName("Nakahama")
                .setSourceName("Manjiro,Nakahama");
        session.setSourceDocument("io.atlasmap.core.issue.SourceClass", source);
        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        assertFalse(TestHelper.printAudit(session), session.hasWarns());
        Object output = session.getTargetDocument("io.atlasmap.core.issue.TargetClass");
        assertEquals(TargetClass.class, output.getClass());
        TargetClass target = TargetClass.class.cast(output);
        assertEquals("Manjiro", target.getTargetFirstName());
        assertEquals("Nakahama", target.getTargetLastName());
        assertEquals("Manjiro,Nakahama", target.getTargetName());
    }

}
