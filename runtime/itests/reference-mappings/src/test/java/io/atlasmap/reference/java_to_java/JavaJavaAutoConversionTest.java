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
package io.atlasmap.reference.java_to_java;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.java.test.BaseFlatPrimitiveClass;
import io.atlasmap.java.test.SourceFlatPrimitiveClass;
import io.atlasmap.java.test.TargetFlatPrimitiveClass;
import io.atlasmap.reference.AtlasMappingBaseTest;
import io.atlasmap.reference.AtlasTestUtil;

public class JavaJavaAutoConversionTest extends AtlasMappingBaseTest {

    protected Object executeMapping(String fileName) throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File(fileName).toURI());
        AtlasSession session = context.createSession();
        BaseFlatPrimitiveClass sourceObject = AtlasTestUtil.generateFlatPrimitiveClass(SourceFlatPrimitiveClass.class);
        session.setInput(sourceObject);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof TargetFlatPrimitiveClass);
        return object;
    }

    @Test
    public void testProcessJavaJavaFlatFieldMappingAutoConversion1() throws Exception {
        Object object = executeMapping("src/test/resources/javaToJava/atlasmapping-flatprimitive-autoconversion-1.xml");
        AtlasTestUtil.validateFlatPrimitiveClassPrimitiveFieldAutoConversion1((TargetFlatPrimitiveClass) object);
    }

    @Test
    public void testProcessJavaJavaFlatFieldMappingAutoConversion2() throws Exception {
        Object object = executeMapping("src/test/resources/javaToJava/atlasmapping-flatprimitive-autoconversion-2.xml");
        AtlasTestUtil.validateFlatPrimitiveClassPrimitiveFieldAutoConversion2((TargetFlatPrimitiveClass) object);
    }

    @Test
    public void testProcessJavaJavaFlatFieldMappingAutoConversion3() throws Exception {
        Object object = executeMapping("src/test/resources/javaToJava/atlasmapping-flatprimitive-autoconversion-3.xml");
        AtlasTestUtil.validateFlatPrimitiveClassPrimitiveFieldAutoConversion3((TargetFlatPrimitiveClass) object);
    }

    @Test
    public void testProcessJavaJavaFlatFieldMappingAutoConversion4() throws Exception {
        Object object = executeMapping("src/test/resources/javaToJava/atlasmapping-flatprimitive-autoconversion-4.xml");
        AtlasTestUtil.validateFlatPrimitiveClassPrimitiveFieldAutoConversion4((TargetFlatPrimitiveClass) object);
    }

    @Test
    public void testProcessJavaJavaFlatFieldMappingAutoConversion5() throws Exception {
        Object object = executeMapping("src/test/resources/javaToJava/atlasmapping-flatprimitive-autoconversion-5.xml");
        AtlasTestUtil.validateFlatPrimitiveClassPrimitiveFieldAutoConversion5((TargetFlatPrimitiveClass) object);
    }

    @Test
    public void testProcessJavaJavaFlatFieldMappingAutoConversion6() throws Exception {
        Object object = executeMapping("src/test/resources/javaToJava/atlasmapping-flatprimitive-autoconversion-6.xml");
        AtlasTestUtil.validateFlatPrimitiveClassPrimitiveFieldAutoConversion6((TargetFlatPrimitiveClass) object);
    }

    @Test
    public void testProcessJavaJavaFlatFieldMappingAutoConversion7() throws Exception {
        Object object = executeMapping("src/test/resources/javaToJava/atlasmapping-flatprimitive-autoconversion-7.xml");
        AtlasTestUtil.validateFlatPrimitiveClassPrimitiveFieldAutoConversion7((TargetFlatPrimitiveClass) object);
    }
}
