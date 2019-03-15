/**
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atlasmap.itests.core.issue;

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
import io.atlasmap.itests.core.TestHelper;
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
        TargetClass targetClass = (TargetClass)targetJava;
        assertEquals("xml3", targetClass.getTargetName());
        assertEquals(3, targetClass.getTargetList().size());
        assertEquals("json1", targetClass.getTargetList().get(0).getName());
        assertEquals("json2", targetClass.getTargetList().get(1).getName());
        assertEquals("json3", targetClass.getTargetList().get(2).getName());
        Object targetJson = session.getTargetDocument("TargetJson");
        assertEquals("{\"javaList\":[{\"name\":\"java1\"},{\"name\":\"java2\"},{\"name\":\"java3\"}],\"xmlList\":[\"xml1\",\"xml2\",\"xml3\"]}",
                targetJson);
        Object targetXml = session.getTargetDocument("TargetXml");
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root>"
                + "<javaList><name>java1</name></javaList><javaList><name>java2</name></javaList><javaList><name>java3</name></javaList>"
                + "<jsonList><name>json1</name></jsonList><jsonList><name>json2</name></jsonList><jsonList><name>json3</name></jsonList></root>",
                targetXml);
    }

}
