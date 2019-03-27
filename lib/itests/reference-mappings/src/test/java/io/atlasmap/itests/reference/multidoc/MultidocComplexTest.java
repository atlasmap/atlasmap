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
package io.atlasmap.itests.reference.multidoc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.java.test.BaseOrder;
import io.atlasmap.java.test.SourceAddress;
import io.atlasmap.java.test.SourceContact;
import io.atlasmap.java.test.SourceOrder;
import io.atlasmap.java.test.TargetContact;
import io.atlasmap.java.test.TargetTestClass;
import io.atlasmap.json.test.AtlasJsonTestUnrootedMapper;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;
import io.atlasmap.itests.reference.AtlasTestUtil;
import io.atlasmap.xml.test.v2.AtlasXmlTestHelper;
import io.atlasmap.xml.test.v2.XmlOrderElement;

public class MultidocComplexTest extends AtlasMappingBaseTest {

    @Test
    public void testProcessBasic() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/multidoc/atlasmapping-complex-simple.json").toURI());
        AtlasSession session = context.createSession();
        BaseOrder javaSourceOrder = AtlasTestUtil.generateOrderClass(SourceOrder.class, SourceAddress.class,
                SourceContact.class);
        session.setSourceDocument("JavaSourceOrder", javaSourceOrder);
        String jsonSourceOrder = AtlasTestUtil
                .loadFileAsString("src/test/resources/multidoc/atlas-json-complex-order-autodetect-unrooted.json");
        session.setSourceDocument("JsonSourceOrder", jsonSourceOrder);
        String xmlOrderAttribute = AtlasTestUtil
                .loadFileAsString("src/test/resources/multidoc/atlas-xml-complex-order-autodetect-attribute-ns.xml");
        session.setSourceDocument("XmlOrderAttribute", xmlOrderAttribute);
        context.process(session);

        assertFalse(printAudit(session), session.hasErrors());
        Object javaTargetTestClass = session.getTargetDocument("JavaTargetTestClass");
        assertEquals(TargetTestClass.class.getName(), javaTargetTestClass.getClass().getName());
        TargetTestClass targetTestClass = (TargetTestClass) javaTargetTestClass;
        assertEquals(new Integer(8765309), targetTestClass.getOrder().getOrderId());
        assertEquals("Ozzie", targetTestClass.getContact().getFirstName());
        assertEquals("Smith", targetTestClass.getContact().getLastName());

        Object jsonTargetOrder = session.getTargetDocument("JsonTargetOrder");
        assertTrue(jsonTargetOrder instanceof String);
        AtlasJsonTestUnrootedMapper testMapper = new AtlasJsonTestUnrootedMapper();
        io.atlasmap.json.test.TargetOrder jsonTargetOrderObject = testMapper.readValue((String) jsonTargetOrder,
                io.atlasmap.json.test.TargetOrder.class);
        assertEquals(new Integer(8765309), jsonTargetOrderObject.getOrderId());
        assertEquals("Ozzie", jsonTargetOrderObject.getContact().getFirstName());
        assertEquals("Smith", jsonTargetOrderObject.getContact().getLastName());

        Object xmlOrderElement = session.getTargetDocument("XmlOrderElement");
        assertNotNull(xmlOrderElement);
        assertTrue(xmlOrderElement instanceof String);
        XmlOrderElement xmlOE = AtlasXmlTestHelper
                .unmarshal((String) xmlOrderElement, XmlOrderElement.class).getValue();
        assertEquals("8765309", xmlOE.getOrderId());
        assertEquals("Ozzie", xmlOE.getContact().getFirstName());
        assertEquals("Smith", xmlOE.getContact().getLastName());
    }

    @Test
    public void testProcessComplexBasicNullContact() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/multidoc/atlasmapping-complex-simple.json").toURI());
        AtlasSession session = context.createSession();
        BaseOrder javaSourceOrder = AtlasTestUtil.generateOrderClass(SourceOrder.class, SourceAddress.class,
                SourceContact.class);
        javaSourceOrder.setContact(null);
        session.setSourceDocument("JavaSourceOrder", javaSourceOrder);
        String jsonSourceOrder = AtlasTestUtil
                .loadFileAsString("src/test/resources/multidoc/atlas-json-complex-order-autodetect-unrooted.json");
        session.setSourceDocument("JsonSourceOrder", jsonSourceOrder);
        String xmlOrderAttribute = AtlasTestUtil
                .loadFileAsString("src/test/resources/multidoc/atlas-xml-complex-order-autodetect-attribute-ns.xml");
        session.setSourceDocument("XmlOrderAttribute", xmlOrderAttribute);
        context.process(session);

        assertFalse(printAudit(session), session.hasErrors());
        TargetTestClass targetTestClass = (TargetTestClass) session.getTargetDocument("JavaTargetTestClass");
        assertEquals(TargetTestClass.class.getName(), targetTestClass.getClass().getName());
        assertEquals(TargetContact.class.getName(), targetTestClass.getContact().getClass().getName());
        assertEquals(new Integer(8765309), targetTestClass.getOrder().getOrderId());
        assertEquals("Ozzie", targetTestClass.getContact().getFirstName());
        assertEquals("Smith", targetTestClass.getContact().getLastName());

        Object jsonTargetOrder = session.getTargetDocument("JsonTargetOrder");
        assertTrue(jsonTargetOrder instanceof String);
        AtlasJsonTestUnrootedMapper testMapper = new AtlasJsonTestUnrootedMapper();
        io.atlasmap.json.test.TargetOrder jsonTargetOrderObject = testMapper.readValue((String) jsonTargetOrder,
                io.atlasmap.json.test.TargetOrder.class);
        assertEquals(new Integer(8765309), jsonTargetOrderObject.getOrderId());
        assertEquals("Ozzie", jsonTargetOrderObject.getContact().getFirstName());
        assertNull(jsonTargetOrderObject.getContact().getLastName());

        Object xmlOrderElement = session.getTargetDocument("XmlOrderElement");
        assertNotNull(xmlOrderElement);
        assertTrue(xmlOrderElement instanceof String);
        XmlOrderElement xmlOE = AtlasXmlTestHelper
                .unmarshal((String) xmlOrderElement, XmlOrderElement.class).getValue();
        assertEquals("8765309", xmlOE.getOrderId());
        assertEquals(null, xmlOE.getContact().getLastName());
    }

}
