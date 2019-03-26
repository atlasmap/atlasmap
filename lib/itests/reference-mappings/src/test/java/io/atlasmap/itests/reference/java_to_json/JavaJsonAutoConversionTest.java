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
package io.atlasmap.itests.reference.java_to_json;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.java.test.BaseFlatPrimitiveClass;
import io.atlasmap.java.test.SourceFlatPrimitiveClass;
import io.atlasmap.java.test.TargetFlatPrimitiveClass;
import io.atlasmap.json.test.AtlasJsonTestUnrootedMapper;
import io.atlasmap.json.test.TargetFlatPrimitive;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;
import io.atlasmap.itests.reference.AtlasTestUtil;

public class JavaJsonAutoConversionTest extends AtlasMappingBaseTest {

    protected Object executeMapping2(String fileName) throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File(fileName).toURI());
        AtlasSession session = context.createSession();

        BaseFlatPrimitiveClass sourceObject = AtlasTestUtil.generateFlatPrimitiveClass(SourceFlatPrimitiveClass.class);
        session.setDefaultSourceDocument(sourceObject);
        context.process(session);

        assertFalse(printAudit(session), session.hasErrors());
        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof TargetFlatPrimitiveClass);
        return object;
    }

    protected Object executeMapping(String fileName) throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File(fileName).toURI());
        AtlasSession session = context.createSession();
        BaseFlatPrimitiveClass source = AtlasTestUtil.generateFlatPrimitiveClass(SourceFlatPrimitiveClass.class);
        session.setDefaultSourceDocument(source);
        context.process(session);

        assertFalse(printAudit(session), session.hasErrors());
        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);
        AtlasJsonTestUnrootedMapper testMapper = new AtlasJsonTestUnrootedMapper();
        TargetFlatPrimitive targetObject = testMapper.readValue((String) object, TargetFlatPrimitive.class);
        return targetObject;
    }

    @Test
    public void testProcessJsonJavaFlatFieldMappingAutoConversion1() throws Exception {
        Object object = executeMapping(
                "src/test/resources/javaToJson/atlasmapping-flatprimitive-unrooted-autoconversion-1.json");
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion1((TargetFlatPrimitive) object);
    }

    @Test
    public void testProcessJsonJavaFlatFieldMappingAutoConversion2() throws Exception {
        Object object = executeMapping(
                "src/test/resources/javaToJson/atlasmapping-flatprimitive-unrooted-autoconversion-2.json");
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion2((TargetFlatPrimitive) object);
    }

    @Test
    public void testProcessJsonJavaFlatFieldMappingAutoConversion3() throws Exception {
        Object object = executeMapping(
                "src/test/resources/javaToJson/atlasmapping-flatprimitive-unrooted-autoconversion-3.json");
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion3((TargetFlatPrimitive) object);
    }

    @Test
    public void testProcessJsonJavaFlatFieldMappingAutoConversion4() throws Exception {
        Object object = executeMapping(
                "src/test/resources/javaToJson/atlasmapping-flatprimitive-unrooted-autoconversion-4.json");
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion4((TargetFlatPrimitive) object);
    }

    @Test
    public void testProcessJsonJavaFlatFieldMappingAutoConversion5() throws Exception {
        Object object = executeMapping(
                "src/test/resources/javaToJson/atlasmapping-flatprimitive-unrooted-autoconversion-5.json");
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion5((TargetFlatPrimitive) object);
    }

    @Test
    public void testProcessJsonJavaFlatFieldMappingAutoConversion6() throws Exception {
        Object object = executeMapping(
                "src/test/resources/javaToJson/atlasmapping-flatprimitive-unrooted-autoconversion-6.json");
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion6((TargetFlatPrimitive) object);
    }

    @Test
    public void testProcessJsonJavaFlatFieldMappingAutoConversion7() throws Exception {
        Object object = executeMapping(
                "src/test/resources/javaToJson/atlasmapping-flatprimitive-unrooted-autoconversion-7.json");
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion7((TargetFlatPrimitive) object);
    }
}
