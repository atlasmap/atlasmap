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

public class CollectionComplexTest {
    private AtlasMappingService mappingService;

    @Before
    public void before() {
        mappingService = DefaultAtlasContextFactory.getInstance().getMappingService();
    }

    @Test
    public void test() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/issue/collection-complex-mapping.xml");
        AtlasMapping mapping = mappingService.loadMapping(url, AtlasMappingFormat.XML);
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();
        SourceClass sourceClass = new SourceClass().setSourceName("javaSourceName");
        sourceClass.getSourceList().add(new Item().setName("java1"));
        sourceClass.getSourceList().add(new Item().setName("java2"));
        sourceClass.getSourceList().add(new Item().setName("java3"));
        session.setSourceDocument("SourceClass", sourceClass);
        session.setSourceDocument("SourceJson", "{\"sourceList\":[\"json1\", \"json2\", \"json3\"]}");
        session.setSourceDocument("SourceXml", "<root><sourceList><name>xml1</name></sourceList><sourceList><name>xml2</name></sourceList><sourceList><name>xml3</name></sourceList></root>");
        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        Object targetJava = session.getTargetDocument("TargetClass");
        assertEquals(TargetClass.class, targetJava.getClass());
        assertEquals("xml3", ((TargetClass)targetJava).getTargetName());
        Object targetJson = session.getTargetDocument("TargetJson");
        assertEquals("{\"javaList\":[{\"name\":\"java1\"},{\"name\":\"java2\"},{\"name\":\"java3\"}]}",
                targetJson);
        Object targetXml = session.getTargetDocument("TargetXml");
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root><javaList><name>java1</name></javaList><javaList><name>java2</name></javaList><javaList><name>java3</name></javaList><jsonList/><jsonList/><jsonList/></root>",
                targetXml);
    }

}
