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
package io.atlasmap.reference.jsonToJson;

import static org.junit.Assert.*;

import java.io.File;
import org.junit.Test;
import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.json.test.AtlasJsonTestRootedMapper;
import io.atlasmap.json.test.AtlasJsonTestUnrootedMapper;
import io.atlasmap.json.test.TargetOrder;
import io.atlasmap.reference.AtlasMappingBaseTest;
import io.atlasmap.reference.AtlasTestUtil;

public class JsonJsonComplexTest extends AtlasMappingBaseTest {

    @Test
    public void testProcessJsonJsonComplexOrderAutodetectUnrooted() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/jsonToJson/atlasmapping-complex-order-autodetect-unrooted.xml").toURI());

        AtlasSession session = context.createSession();
        String source = AtlasTestUtil
                .loadFileAsString("src/test/resources/jsonToJson/atlas-json-complex-order-autodetect-unrooted.json");
        session.setInput(source);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        AtlasJsonTestUnrootedMapper testMapper = new AtlasJsonTestUnrootedMapper();
        TargetOrder targetObject = testMapper.readValue((String) object, TargetOrder.class);
        AtlasTestUtil.validateJsonOrder(targetObject);
    }

    @Test
    public void testProcessJsonJsonComplexOrderAutodetectRooted() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/jsonToJson/atlasmapping-complex-order-autodetect-rooted.xml").toURI());

        AtlasSession session = context.createSession();
        String source = AtlasTestUtil
                .loadFileAsString("src/test/resources/jsonToJson/atlas-json-complex-order-autodetect-rooted.json");
        session.setInput(source);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        AtlasJsonTestRootedMapper testMapper = new AtlasJsonTestRootedMapper();
        TargetOrder targetObject = testMapper.readValue((String) object, TargetOrder.class);
        AtlasTestUtil.validateJsonOrder(targetObject);
    }

}
