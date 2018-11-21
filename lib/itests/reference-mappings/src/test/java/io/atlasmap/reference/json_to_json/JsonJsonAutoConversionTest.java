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
package io.atlasmap.reference.json_to_json;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.json.test.AtlasJsonTestUnrootedMapper;
import io.atlasmap.json.test.TargetFlatPrimitive;
import io.atlasmap.reference.AtlasMappingBaseTest;
import io.atlasmap.reference.AtlasTestUtil;

public class JsonJsonAutoConversionTest extends AtlasMappingBaseTest {

    protected TargetFlatPrimitive executeMapping(String fileName) throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File(fileName).toURI());
        AtlasSession session = context.createSession();
        String source = AtlasTestUtil.loadFileAsString(
                "src/test/resources/jsonToJson/atlas-json-flatprimitive-unrooted-autoconversion.json");
        session.setDefaultSourceDocument(source);
        context.process(session);

        assertFalse(printAudit(session), session.hasErrors());
        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);
        AtlasJsonTestUnrootedMapper testMapper = new AtlasJsonTestUnrootedMapper();
        return testMapper.readValue((String) object, TargetFlatPrimitive.class);
    }

    @Test
    public void testProcessJsonJsonFlatFieldMappingAutoConversion1() throws Exception {
        TargetFlatPrimitive targetObject = executeMapping(
                "src/test/resources/jsonToJson/atlasmapping-flatprimitive-unrooted-autoconversion-1.xml");
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion1(targetObject);
    }

    @Test
    public void testProcessJsonJsonFlatFieldMappingAutoConversion2() throws Exception {
        TargetFlatPrimitive targetObject = executeMapping(
                "src/test/resources/jsonToJson/atlasmapping-flatprimitive-unrooted-autoconversion-2.xml");
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion2(targetObject);
    }

    @Test
    public void testProcessJsonJsonFlatFieldMappingAutoConversion3() throws Exception {
        TargetFlatPrimitive targetObject = executeMapping(
                "src/test/resources/jsonToJson/atlasmapping-flatprimitive-unrooted-autoconversion-3.xml");
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion3(targetObject);
    }

    @Test
    public void testProcessJsonJsonFlatFieldMappingAutoConversion4() throws Exception {
        TargetFlatPrimitive targetObject = executeMapping(
                "src/test/resources/jsonToJson/atlasmapping-flatprimitive-unrooted-autoconversion-4.xml");
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion4(targetObject);
    }

    @Test
    public void testProcessJsonJsonFlatFieldMappingAutoConversion5() throws Exception {
        TargetFlatPrimitive targetObject = executeMapping(
                "src/test/resources/jsonToJson/atlasmapping-flatprimitive-unrooted-autoconversion-5.xml");
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion5(targetObject);
    }

    @Test
    public void testProcessJsonJsonFlatFieldMappingAutoConversion6() throws Exception {
        TargetFlatPrimitive targetObject = executeMapping(
                "src/test/resources/jsonToJson/atlasmapping-flatprimitive-unrooted-autoconversion-6.xml");
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion6(targetObject);
    }

    @Test
    public void testProcessJsonJsonFlatFieldMappingAutoConversion7() throws Exception {
        TargetFlatPrimitive targetObject = executeMapping(
                "src/test/resources/jsonToJson/atlasmapping-flatprimitive-unrooted-autoconversion-7.xml");
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion7(targetObject);
    }

}
