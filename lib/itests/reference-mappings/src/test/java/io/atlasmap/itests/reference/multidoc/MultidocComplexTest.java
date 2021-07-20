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
package io.atlasmap.itests.reference.multidoc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.xmlunit.assertj.XmlAssert.assertThat;

import java.io.File;
import java.util.HashMap;

import org.junit.jupiter.api.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;
import io.atlasmap.itests.reference.AtlasTestUtil;
import io.atlasmap.java.test.BaseOrder;
import io.atlasmap.java.test.SourceAddress;
import io.atlasmap.java.test.SourceContact;
import io.atlasmap.java.test.SourceOrder;
import io.atlasmap.java.test.TargetContact;
import io.atlasmap.java.test.TargetTestClass;
import io.atlasmap.json.test.AtlasJsonTestUnrootedMapper;

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

        assertFalse(session.hasErrors(), printAudit(session));
        Object javaTargetTestClass = session.getTargetDocument("JavaTargetTestClass");
        assertEquals(TargetTestClass.class.getName(), javaTargetTestClass.getClass().getName());
        TargetTestClass targetTestClass = (TargetTestClass) javaTargetTestClass;
        assertEquals(Integer.valueOf(8765309), targetTestClass.getOrder().getOrderId());
        assertEquals("Ozzie", targetTestClass.getContact().getFirstName());
        assertEquals("Smith", targetTestClass.getContact().getLastName());

        Object jsonTargetOrder = session.getTargetDocument("JsonTargetOrder");
        assertTrue(jsonTargetOrder instanceof String);
        AtlasJsonTestUnrootedMapper testMapper = new AtlasJsonTestUnrootedMapper();
        io.atlasmap.json.test.TargetOrder jsonTargetOrderObject = testMapper.readValue((String) jsonTargetOrder,
                io.atlasmap.json.test.TargetOrder.class);
        assertEquals(Integer.valueOf(8765309), jsonTargetOrderObject.getOrderId());
        assertEquals("Ozzie", jsonTargetOrderObject.getContact().getFirstName());
        assertEquals("Smith", jsonTargetOrderObject.getContact().getLastName());

        Object xmlOrderElement = session.getTargetDocument("XmlOrderElement");
        assertNotNull(xmlOrderElement);
        assertTrue(xmlOrderElement instanceof String);
        HashMap<String,String> ns = new HashMap<>();
        ns.put("ns", "http://atlasmap.io/xml/test/v2");
        assertThat(xmlOrderElement).withNamespaceContext(ns).valueByXPath("/ns:XmlOE/ns:orderId").isEqualTo("8765309");
        assertThat(xmlOrderElement).withNamespaceContext(ns).valueByXPath("/ns:XmlOE/ns:Contact/ns:firstName").isEqualTo("Ozzie");
        assertThat(xmlOrderElement).withNamespaceContext(ns).valueByXPath("/ns:XmlOE/ns:Contact/ns:lastName").isEqualTo("Smith");
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

        assertFalse(session.hasErrors(), printAudit(session));
        TargetTestClass targetTestClass = (TargetTestClass) session.getTargetDocument("JavaTargetTestClass");
        assertEquals(TargetTestClass.class.getName(), targetTestClass.getClass().getName());
        assertEquals(TargetContact.class.getName(), targetTestClass.getContact().getClass().getName());
        assertEquals(Integer.valueOf(8765309), targetTestClass.getOrder().getOrderId());
        assertEquals("Ozzie", targetTestClass.getContact().getFirstName());
        assertEquals("Smith", targetTestClass.getContact().getLastName());

        Object jsonTargetOrder = session.getTargetDocument("JsonTargetOrder");
        assertTrue(jsonTargetOrder instanceof String);
        AtlasJsonTestUnrootedMapper testMapper = new AtlasJsonTestUnrootedMapper();
        io.atlasmap.json.test.TargetOrder jsonTargetOrderObject = testMapper.readValue((String) jsonTargetOrder,
                io.atlasmap.json.test.TargetOrder.class);
        assertEquals(Integer.valueOf(8765309), jsonTargetOrderObject.getOrderId());
        assertEquals("Ozzie", jsonTargetOrderObject.getContact().getFirstName());
        assertNull(jsonTargetOrderObject.getContact().getLastName());

        Object xmlOrderElement = session.getTargetDocument("XmlOrderElement");
        assertNotNull(xmlOrderElement);
        assertTrue(xmlOrderElement instanceof String);
        HashMap<String,String> ns = new HashMap<>();
        ns.put("ns", "http://atlasmap.io/xml/test/v2");
        assertThat(xmlOrderElement).withNamespaceContext(ns).valueByXPath("/ns:XmlOE/ns:orderId").isEqualTo("8765309");
        assertThat(xmlOrderElement).withNamespaceContext(ns).valueByXPath("/ns:XmlOE/ns:Contact/ns:lastName").isNullOrEmpty();
    }

}
