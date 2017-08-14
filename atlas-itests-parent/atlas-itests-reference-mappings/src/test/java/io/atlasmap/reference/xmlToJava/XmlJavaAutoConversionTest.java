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
package io.atlasmap.reference.xmlToJava;

import static org.junit.Assert.*;

import java.io.File;
import org.junit.Test;
import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.java.test.TargetFlatPrimitiveClass;
import io.atlasmap.reference.AtlasMappingBaseTest;
import io.atlasmap.reference.AtlasTestUtil;

public class XmlJavaAutoConversionTest extends AtlasMappingBaseTest {

    @Test
    public void testProcessXmlJavaFlatFieldMappingAutoConversion1() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/xmlToJava/atlasmapping-flatprimitive-attribute-autoconversion-1.xml")
                        .toURI());
        AtlasSession session = context.createSession();
        String source = AtlasTestUtil
                .loadFileAsString("src/test/resources/xmlToJava/atlas-xml-flatprimitive-attribute-autoconversion.xml");
        session.setInput(source);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof TargetFlatPrimitiveClass);
        AtlasTestUtil.validateFlatPrimitiveClassPrimitiveFieldAutoConversion1((TargetFlatPrimitiveClass) object);
    }

    @Test
    public void testProcessXmlJavaFlatFieldMappingAutoConversion2() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/xmlToJava/atlasmapping-flatprimitive-attribute-autoconversion-2.xml")
                        .toURI());
        AtlasSession session = context.createSession();
        String source = AtlasTestUtil
                .loadFileAsString("src/test/resources/xmlToJava/atlas-xml-flatprimitive-attribute-autoconversion.xml");
        session.setInput(source);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof TargetFlatPrimitiveClass);
        AtlasTestUtil.validateFlatPrimitiveClassPrimitiveFieldAutoConversion2((TargetFlatPrimitiveClass) object);
    }

    @Test
    public void testProcessXmlJavaFlatFieldMappingAutoConversion3() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/xmlToJava/atlasmapping-flatprimitive-attribute-autoconversion-3.xml")
                        .toURI());
        AtlasSession session = context.createSession();
        String source = AtlasTestUtil
                .loadFileAsString("src/test/resources/xmlToJava/atlas-xml-flatprimitive-attribute-autoconversion.xml");
        session.setInput(source);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof TargetFlatPrimitiveClass);
        AtlasTestUtil.validateFlatPrimitiveClassPrimitiveFieldAutoConversion3((TargetFlatPrimitiveClass) object);
    }

    @Test
    public void testProcessXmlJavaFlatFieldMappingAutoConversion4() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/xmlToJava/atlasmapping-flatprimitive-attribute-autoconversion-4.xml")
                        .toURI());
        AtlasSession session = context.createSession();
        String source = AtlasTestUtil
                .loadFileAsString("src/test/resources/xmlToJava/atlas-xml-flatprimitive-attribute-autoconversion.xml");
        session.setInput(source);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof TargetFlatPrimitiveClass);
        AtlasTestUtil.validateFlatPrimitiveClassPrimitiveFieldAutoConversion4((TargetFlatPrimitiveClass) object);
    }

    @Test
    public void testProcessXmlJavaFlatFieldMappingAutoConversion5() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/xmlToJava/atlasmapping-flatprimitive-attribute-autoconversion-5.xml")
                        .toURI());
        AtlasSession session = context.createSession();
        String source = AtlasTestUtil
                .loadFileAsString("src/test/resources/xmlToJava/atlas-xml-flatprimitive-attribute-autoconversion.xml");
        session.setInput(source);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof TargetFlatPrimitiveClass);
        AtlasTestUtil.validateFlatPrimitiveClassPrimitiveFieldAutoConversion5((TargetFlatPrimitiveClass) object);
    }

    @Test
    public void testProcessXmlJavaFlatFieldMappingAutoConversion6() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/xmlToJava/atlasmapping-flatprimitive-attribute-autoconversion-6.xml")
                        .toURI());
        AtlasSession session = context.createSession();
        String source = AtlasTestUtil
                .loadFileAsString("src/test/resources/xmlToJava/atlas-xml-flatprimitive-attribute-autoconversion.xml");
        session.setInput(source);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof TargetFlatPrimitiveClass);
        AtlasTestUtil.validateFlatPrimitiveClassPrimitiveFieldAutoConversion6((TargetFlatPrimitiveClass) object);
    }

}
