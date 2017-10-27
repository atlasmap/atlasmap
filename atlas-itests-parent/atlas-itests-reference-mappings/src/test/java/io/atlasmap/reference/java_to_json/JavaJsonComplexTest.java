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

import java.io.File;
import org.junit.Test;
import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.java.test.BaseOrder;
import io.atlasmap.java.test.SourceAddress;
import io.atlasmap.java.test.SourceContact;
import io.atlasmap.java.test.SourceOrder;
import io.atlasmap.json.test.AtlasJsonTestRootedMapper;
import io.atlasmap.json.test.AtlasJsonTestUnrootedMapper;
import io.atlasmap.reference.AtlasMappingBaseTest;
import io.atlasmap.reference.AtlasTestUtil;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JavaJsonComplexTest extends AtlasMappingBaseTest {

    @Test
    public void testProcessJsonJavaComplexOrderAutodetectUnrooted() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/javaToJson/atlasmapping-complex-order-autodetect-unrooted.xml").toURI());

        AtlasSession session = context.createSession();
        BaseOrder source = AtlasTestUtil.generateOrderClass(SourceOrder.class, SourceAddress.class,
                SourceContact.class);
        session.setInput(source);
        context.process(session);

        Object object = session.getOutput();
        assertTrue(object instanceof String);
        AtlasJsonTestUnrootedMapper testMapper = new AtlasJsonTestUnrootedMapper();
        io.atlasmap.json.test.TargetOrder targetObject = testMapper.readValue((String) object,
                io.atlasmap.json.test.TargetOrder.class);
        AtlasTestUtil.validateJsonOrder(targetObject);
    }

    @Test
    public void testProcessJavaJsonComplexOrderAutodetectRooted() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/javaToJson/atlasmapping-complex-order-autodetect-rooted.xml").toURI());

        AtlasSession session = context.createSession();
        BaseOrder source = AtlasTestUtil.generateOrderClass(SourceOrder.class, SourceAddress.class,
                SourceContact.class);
        session.setInput(source);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        AtlasJsonTestRootedMapper testMapper = new AtlasJsonTestRootedMapper();
        io.atlasmap.json.test.TargetOrder targetObject = testMapper.readValue((String) object,
                io.atlasmap.json.test.TargetOrder.class);
        AtlasTestUtil.validateJsonOrder(targetObject);
    }

}
