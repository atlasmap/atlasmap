package io.atlasmap.core.issue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

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

public class PropertyOrderTest {

    private AtlasMappingService mappingService;

    @Before
    public void before() throws Exception {
        mappingService = DefaultAtlasContextFactory.getInstance().getMappingService();
    }

    @Test
    public void test() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/issue/property-order-mapping.xml");
        AtlasMapping mapping = mappingService.loadMapping(url, AtlasMappingFormat.XML);
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();

        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        TargetClass output = TargetClass.class.cast(session.getTargetDocument("io.atlasmap.core.issue.TargetClass"));
        assertEquals("testValue", output.getTargetName());
        assertNotEquals("testPath", output.getTargetFirstName());

        System.setProperty("testProp", "testProp-sysProp");
        System.setProperty("PATH", "PATH-sysProp");
        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        output = TargetClass.class.cast(session.getTargetDocument("io.atlasmap.core.issue.TargetClass"));
        assertEquals("testProp-sysProp", output.getTargetName());
        assertEquals("PATH-sysProp", output.getTargetFirstName());

        session.getProperties().put("testProp", "testProp-runtimeProp");
        session.getProperties().put("PATH", "PATH-runtimeProp");
        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        output = TargetClass.class.cast(session.getTargetDocument("io.atlasmap.core.issue.TargetClass"));
        assertEquals("testProp-runtimeProp", output.getTargetName());
        assertEquals("PATH-runtimeProp", output.getTargetFirstName());
    }

}
