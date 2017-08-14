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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import org.junit.Test;
import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.json.test.AtlasJsonTestUnrootedMapper;
import io.atlasmap.json.test.TargetContact;
import io.atlasmap.json.test.TargetOrder;
import io.atlasmap.reference.AtlasMappingBaseTest;
import io.atlasmap.reference.AtlasTestUtil;

public class JsonJsonMultiSourceTest extends AtlasMappingBaseTest {

    @Test
    public void testProcessBasic() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/jsonToJson/atlasmapping-multisource-basic.xml").toURI());
        AtlasSession session = context.createSession();
        String sourceContact = AtlasTestUtil
                .loadFileAsString("src/test/resources/jsonToJson/atlas-json-contact-unrooted.json");
        String sourceAddress = AtlasTestUtil
                .loadFileAsString("src/test/resources/jsonToJson/atlas-json-address-unrooted.json");
        session.setInput(sourceContact, "con");
        session.setInput(sourceAddress, "addr");
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        AtlasJsonTestUnrootedMapper testMapper = new AtlasJsonTestUnrootedMapper();
        TargetContact targetContact = testMapper.readValue((String) object, TargetContact.class);
        assertEquals("Ozzie", targetContact.getFirstName());
        assertNull(targetContact.getLastName());
        assertNull(targetContact.getPhoneNumber());
        assertEquals("90210", targetContact.getZipCode());
    }

    @Test
    public void testProcessComplex() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/jsonToJson/atlasmapping-multisource-complex.xml").toURI());
        AtlasSession session = context.createSession();
        String sourceContact = AtlasTestUtil
                .loadFileAsString("src/test/resources/jsonToJson/atlas-json-contact-unrooted.json");
        String sourceAddress = AtlasTestUtil
                .loadFileAsString("src/test/resources/jsonToJson/atlas-json-address-unrooted.json");
        session.setInput(sourceContact, "con");
        session.setInput(sourceAddress, "addr");
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        AtlasJsonTestUnrootedMapper testMapper = new AtlasJsonTestUnrootedMapper();
        TargetOrder targetOrder = testMapper.readValue((String) object, TargetOrder.class);
        AtlasTestUtil.validateJsonOrder(targetOrder);
    }
}
