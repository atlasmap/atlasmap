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
package io.atlasmap.reference.jsonToJava;

import static org.junit.Assert.*;

import java.io.File;
import org.junit.Test;
import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.java.test.TargetOrder;
import io.atlasmap.reference.AtlasMappingBaseTest;
import io.atlasmap.reference.AtlasTestUtil;

public class JsonJavaComplexTest extends AtlasMappingBaseTest {

    @Test
    public void testProcessJsonJavaComplexOrderAutodetectUnrooted() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/jsonToJava/atlasmapping-complex-order-autodetect-unrooted.xml").toURI());

        AtlasSession session = context.createSession();
        String source = AtlasTestUtil
                .loadFileAsString("src/test/resources/jsonToJava/atlas-json-complex-order-autodetect-unrooted.json");
        session.setInput(source);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof TargetOrder);
        AtlasTestUtil.validateOrder((TargetOrder) object);
    }

    @Test
    public void testProcessJsonJavaComplexOrderAutodetectRooted() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/jsonToJava/atlasmapping-complex-order-autodetect-rooted.xml").toURI());

        AtlasSession session = context.createSession();
        String source = AtlasTestUtil
                .loadFileAsString("src/test/resources/jsonToJava/atlas-json-complex-order-autodetect-rooted.json");
        session.setInput(source);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof TargetOrder);
        AtlasTestUtil.validateOrder((TargetOrder) object);
    }

}
