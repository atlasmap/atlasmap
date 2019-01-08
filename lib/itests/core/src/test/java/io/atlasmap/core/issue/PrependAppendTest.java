package io.atlasmap.core.issue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.AtlasMappingService;
import io.atlasmap.core.AtlasMappingService.AtlasMappingFormat;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.core.TestHelper;
import io.atlasmap.v2.AtlasMapping;

/**
 * https://github.com/atlasmap/atlasmap/issues/704
 */
public class PrependAppendTest {

    private AtlasMappingService mappingService;

    @Before
    public void before() {
        mappingService = DefaultAtlasContextFactory.getInstance().getMappingService();
    }

    @Test
    public void test() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/issue/prepend-append-mapping.xml");
        AtlasMapping mapping = mappingService.loadMapping(url, AtlasMappingFormat.XML);
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();
        session.setSourceDocument("io.atlasmap.core.issue.SourceClass", new SourceClass().setSourceString("foo"));
        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        Object output = session.getTargetDocument("io.atlasmap.core.issue.TargetClass");
        assertEquals(TargetClass.class, output.getClass());
        assertEquals("prepend-foo-append", ((TargetClass)output).getTargetString());
    }

}
