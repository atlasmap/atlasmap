/*
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
package io.atlasmap.itests.reference.json_to_java;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;
import io.atlasmap.itests.reference.AtlasTestUtil;
import io.atlasmap.java.test.TargetFlatPrimitiveClass;

public class JsonJavaAutoConversionTest extends AtlasMappingBaseTest {

    protected Object executeMapping(String fileName) throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File(fileName).toURI());
        AtlasSession session = context.createSession();
        String source = AtlasTestUtil.loadFileAsString(
                "src/test/resources/jsonToJava/atlas-json-flatprimitive-unrooted-autoconversion.json");
        session.setDefaultSourceDocument(source);
        context.process(session);

        assertFalse(session.hasErrors(), printAudit(session));
        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof TargetFlatPrimitiveClass);
        return object;
    }

    @Test
    public void testProcessJsonJavaFlatFieldMappingAutoConversion1() throws Exception {
        Object object = executeMapping(
                "src/test/resources/jsonToJava/atlasmapping-flatprimitive-unrooted-autoconversion-1.json");
        AtlasTestUtil.validateFlatPrimitiveClassPrimitiveFieldAutoConversion1((TargetFlatPrimitiveClass) object);
    }

    @Test
    public void testProcessJsonJavaFlatFieldMappingAutoConversion2() throws Exception {
        Object object = executeMapping(
                "src/test/resources/jsonToJava/atlasmapping-flatprimitive-unrooted-autoconversion-2.json");
        AtlasTestUtil.validateFlatPrimitiveClassPrimitiveFieldAutoConversion2((TargetFlatPrimitiveClass) object);
    }

    @Test
    public void testProcessJsonJavaFlatFieldMappingAutoConversion3() throws Exception {
        Object object = executeMapping(
                "src/test/resources/jsonToJava/atlasmapping-flatprimitive-unrooted-autoconversion-3.json");
        AtlasTestUtil.validateFlatPrimitiveClassPrimitiveFieldAutoConversion3((TargetFlatPrimitiveClass) object);
    }

    @Test
    public void testProcessJsonJavaFlatFieldMappingAutoConversion4() throws Exception {
        Object object = executeMapping(
                "src/test/resources/jsonToJava/atlasmapping-flatprimitive-unrooted-autoconversion-4.json");
        AtlasTestUtil.validateFlatPrimitiveClassPrimitiveFieldAutoConversion4((TargetFlatPrimitiveClass) object);
    }

    @Test
    public void testProcessJsonJavaFlatFieldMappingAutoConversion5() throws Exception {
        Object object = executeMapping(
                "src/test/resources/jsonToJava/atlasmapping-flatprimitive-unrooted-autoconversion-5.json");
        AtlasTestUtil.validateFlatPrimitiveClassPrimitiveFieldAutoConversion5((TargetFlatPrimitiveClass) object);
    }

    @Test
    public void testProcessJsonJavaFlatFieldMappingAutoConversion6() throws Exception {
        Object object = executeMapping(
                "src/test/resources/jsonToJava/atlasmapping-flatprimitive-unrooted-autoconversion-6.json");
        AtlasTestUtil.validateFlatPrimitiveClassPrimitiveFieldAutoConversion6((TargetFlatPrimitiveClass) object);
    }

    @Test
    public void testProcessJsonJavaFlatFieldMappingAutoConversion7() throws Exception {
        Object object = executeMapping(
                "src/test/resources/jsonToJava/atlasmapping-flatprimitive-unrooted-autoconversion-7.json");
        AtlasTestUtil.validateFlatPrimitiveClassPrimitiveFieldAutoConversion7((TargetFlatPrimitiveClass) object);
    }

}
