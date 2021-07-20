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
package io.atlasmap.itests.reference.json_to_json;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;
import io.atlasmap.itests.reference.AtlasTestUtil;
import io.atlasmap.json.test.AtlasJsonTestUnrootedMapper;
import io.atlasmap.json.test.TargetFlatPrimitive;

public class JsonJsonAutoConversionTest extends AtlasMappingBaseTest {

    protected TargetFlatPrimitive executeMapping(String fileName) throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File(fileName).toURI());
        AtlasSession session = context.createSession();
        String source = AtlasTestUtil.loadFileAsString(
                "src/test/resources/jsonToJson/atlas-json-flatprimitive-unrooted-autoconversion.json");
        session.setDefaultSourceDocument(source);
        context.process(session);

        assertFalse(session.hasErrors(), printAudit(session));
        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);
        AtlasJsonTestUnrootedMapper testMapper = new AtlasJsonTestUnrootedMapper();
        return testMapper.readValue((String) object, TargetFlatPrimitive.class);
    }

    @Test
    public void testProcessJsonJsonFlatFieldMappingAutoConversion1() throws Exception {
        TargetFlatPrimitive targetObject = executeMapping(
                "src/test/resources/jsonToJson/atlasmapping-flatprimitive-unrooted-autoconversion-1.json");
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion1(targetObject);
    }

    @Test
    public void testProcessJsonJsonFlatFieldMappingAutoConversion2() throws Exception {
        TargetFlatPrimitive targetObject = executeMapping(
                "src/test/resources/jsonToJson/atlasmapping-flatprimitive-unrooted-autoconversion-2.json");
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion2(targetObject);
    }

    @Test
    public void testProcessJsonJsonFlatFieldMappingAutoConversion3() throws Exception {
        TargetFlatPrimitive targetObject = executeMapping(
                "src/test/resources/jsonToJson/atlasmapping-flatprimitive-unrooted-autoconversion-3.json");
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion3(targetObject);
    }

    @Test
    public void testProcessJsonJsonFlatFieldMappingAutoConversion4() throws Exception {
        TargetFlatPrimitive targetObject = executeMapping(
                "src/test/resources/jsonToJson/atlasmapping-flatprimitive-unrooted-autoconversion-4.json");
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion4(targetObject);
    }

    @Test
    public void testProcessJsonJsonFlatFieldMappingAutoConversion5() throws Exception {
        TargetFlatPrimitive targetObject = executeMapping(
                "src/test/resources/jsonToJson/atlasmapping-flatprimitive-unrooted-autoconversion-5.json");
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion5(targetObject);
    }

    @Test
    public void testProcessJsonJsonFlatFieldMappingAutoConversion6() throws Exception {
        TargetFlatPrimitive targetObject = executeMapping(
                "src/test/resources/jsonToJson/atlasmapping-flatprimitive-unrooted-autoconversion-6.json");
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion6(targetObject);
    }

    @Test
    public void testProcessJsonJsonFlatFieldMappingAutoConversion7() throws Exception {
        TargetFlatPrimitive targetObject = executeMapping(
                "src/test/resources/jsonToJson/atlasmapping-flatprimitive-unrooted-autoconversion-7.json");
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFieldAutoConversion7(targetObject);
    }

}
