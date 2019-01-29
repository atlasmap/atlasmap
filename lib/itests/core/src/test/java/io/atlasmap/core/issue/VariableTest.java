package io.atlasmap.core.issue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.AtlasMappingService;
import io.atlasmap.core.AtlasMappingService.AtlasMappingFormat;
import io.atlasmap.core.DefaultAtlasContext;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.core.TestHelper;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.Property;

/**
 * https://github.com/atlasmap/atlasmap/issues/704
 */
public class VariableTest {

    private AtlasMappingService mappingService;

    @Before
    public void before() {
        mappingService = DefaultAtlasContextFactory.getInstance().getMappingService();
    }

    @Test
    public void test() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/issue/variable-mapping.xml");
        AtlasMapping mapping = mappingService.loadMapping(url, AtlasMappingFormat.XML);
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();
        session.setSourceDocument("io.atlasmap.core.issue.SourceClass", new SourceClass().setSourceString("foo"));
        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        Object output = session.getTargetDocument("io.atlasmap.core.issue.TargetClass");
        assertEquals(TargetClass.class, output.getClass());
        assertEquals("prepend-foo-prepend-foo", ((TargetClass)output).getTargetString());
        assertNotNull(DefaultAtlasContext.activeMappingRoot);
        assertNotNull(DefaultAtlasContext.activeMappingRoot.getProperties());
        List<Property> props = DefaultAtlasContext.activeMappingRoot.getProperties().getProperty();
        assertEquals(2, props.size());
        assertEquals("$1$", props.get(0).getName());
        assertEquals("prepend-foo", props.get(0).getValue());
        assertEquals("$2$", props.get(1).getName());
        assertEquals("prepend-foo-prepend-foo", props.get(1).getValue());
    }
}
