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
package io.atlasmap.reference.java_to_json;

import static org.junit.Assert.*;

import java.io.File;
import org.junit.Test;
import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.java.test.BaseFlatPrimitiveClass;
import io.atlasmap.java.test.SourceFlatPrimitiveClass;
import io.atlasmap.json.test.AtlasJsonTestUnrootedMapper;
import io.atlasmap.json.test.TargetFlatPrimitive;
import io.atlasmap.reference.AtlasMappingBaseTest;
import io.atlasmap.reference.AtlasTestUtil;

public class JavaJsonAutoConversionTest extends AtlasMappingBaseTest {

    @Test
    public void testProcessJsonJavaFlatFieldMappingAutoConversion1() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/javaToJson/atlasmapping-flatprimitive-unrooted-autoconversion-1.xml")
                        .toURI());
        AtlasSession session = context.createSession();
        BaseFlatPrimitiveClass source = AtlasTestUtil.generateFlatPrimitiveClass(SourceFlatPrimitiveClass.class);
        session.setInput(source);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        AtlasJsonTestUnrootedMapper testMapper = new AtlasJsonTestUnrootedMapper();
        TargetFlatPrimitive targetObject = testMapper.readValue((String) object, TargetFlatPrimitive.class);
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion1((TargetFlatPrimitive) targetObject);
    }

    @Test
    public void testProcessJsonJavaFlatFieldMappingAutoConversion2() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/javaToJson/atlasmapping-flatprimitive-unrooted-autoconversion-2.xml")
                        .toURI());
        AtlasSession session = context.createSession();
        BaseFlatPrimitiveClass source = AtlasTestUtil.generateFlatPrimitiveClass(SourceFlatPrimitiveClass.class);
        session.setInput(source);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        AtlasJsonTestUnrootedMapper testMapper = new AtlasJsonTestUnrootedMapper();
        TargetFlatPrimitive targetObject = testMapper.readValue((String) object, TargetFlatPrimitive.class);
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion2((TargetFlatPrimitive) targetObject);
    }

    @Test
    public void testProcessJsonJavaFlatFieldMappingAutoConversion3() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/javaToJson/atlasmapping-flatprimitive-unrooted-autoconversion-3.xml")
                        .toURI());
        AtlasSession session = context.createSession();
        BaseFlatPrimitiveClass source = AtlasTestUtil.generateFlatPrimitiveClass(SourceFlatPrimitiveClass.class);
        session.setInput(source);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        AtlasJsonTestUnrootedMapper testMapper = new AtlasJsonTestUnrootedMapper();
        TargetFlatPrimitive targetObject = testMapper.readValue((String) object, TargetFlatPrimitive.class);
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion3((TargetFlatPrimitive) targetObject);
    }

    @Test
    public void testProcessJsonJavaFlatFieldMappingAutoConversion4() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/javaToJson/atlasmapping-flatprimitive-unrooted-autoconversion-4.xml")
                        .toURI());
        AtlasSession session = context.createSession();
        BaseFlatPrimitiveClass source = AtlasTestUtil.generateFlatPrimitiveClass(SourceFlatPrimitiveClass.class);
        session.setInput(source);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        AtlasJsonTestUnrootedMapper testMapper = new AtlasJsonTestUnrootedMapper();
        TargetFlatPrimitive targetObject = testMapper.readValue((String) object, TargetFlatPrimitive.class);
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion4((TargetFlatPrimitive) targetObject);
    }

    @Test
    public void testProcessJsonJavaFlatFieldMappingAutoConversion5() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/javaToJson/atlasmapping-flatprimitive-unrooted-autoconversion-5.xml")
                        .toURI());
        AtlasSession session = context.createSession();
        BaseFlatPrimitiveClass source = AtlasTestUtil.generateFlatPrimitiveClass(SourceFlatPrimitiveClass.class);
        session.setInput(source);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        AtlasJsonTestUnrootedMapper testMapper = new AtlasJsonTestUnrootedMapper();
        TargetFlatPrimitive targetObject = testMapper.readValue((String) object, TargetFlatPrimitive.class);
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion5((TargetFlatPrimitive) targetObject);
    }

    @Test
    public void testProcessJsonJavaFlatFieldMappingAutoConversion6() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/javaToJson/atlasmapping-flatprimitive-unrooted-autoconversion-6.xml")
                        .toURI());
        AtlasSession session = context.createSession();
        BaseFlatPrimitiveClass source = AtlasTestUtil.generateFlatPrimitiveClass(SourceFlatPrimitiveClass.class);
        session.setInput(source);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        AtlasJsonTestUnrootedMapper testMapper = new AtlasJsonTestUnrootedMapper();
        TargetFlatPrimitive targetObject = testMapper.readValue((String) object, TargetFlatPrimitive.class);
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion6((TargetFlatPrimitive) targetObject);
    }

}
