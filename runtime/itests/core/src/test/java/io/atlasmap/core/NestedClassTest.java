package io.atlasmap.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.AtlasMappingService.AtlasMappingFormat;
import io.atlasmap.v2.AtlasMapping;

public class NestedClassTest {

    private AtlasMappingService mappingService;

    @Before
    public void before() {
        mappingService = DefaultAtlasContextFactory.getInstance().getMappingService();
    }

    @Test
    public void test() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-nested-class.xml");
        AtlasMapping mapping = mappingService.loadMapping(url, AtlasMappingFormat.XML);
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();
        SourceClass sc = new SourceClass();
        sc.setSomeField("some source class value");
        session.setSourceDocument("io.atlasmap.core.SourceClass", sc);
        BaseClass.SomeNestedClass sic = new BaseClass.SomeNestedClass();
        sic.setSomeField("some nested class value");
        session.setSourceDocument("io.atlasmap.core.BaseClass$SomeNestedClass", sic);
        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        assertFalse(TestHelper.printAudit(session), session.hasWarns());
        Object tc = session.getTargetDocument("io.atlasmap.core.TargetClass");
        assertEquals(TargetClass.class, tc.getClass());
        TargetClass target = TargetClass.class.cast(tc);
        assertEquals("some nested class value", target.getSomeField());
        Object tsic = session.getTargetDocument("io.atlasmap.core.BaseClass$SomeNestedClass");
        assertEquals(BaseClass.SomeNestedClass.class, tsic.getClass());
        BaseClass.SomeNestedClass targetSic = BaseClass.SomeNestedClass.class.cast(tsic);
        assertEquals("some source class value", targetSic.getSomeField());
    }

}
