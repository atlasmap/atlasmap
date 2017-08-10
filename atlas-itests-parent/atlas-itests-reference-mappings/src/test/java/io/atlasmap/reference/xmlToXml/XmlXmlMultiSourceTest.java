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
package io.atlasmap.reference.xmlToXml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import org.junit.Test;
import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.reference.AtlasMappingBaseTest;
import io.atlasmap.reference.AtlasTestUtil;

public class XmlXmlMultiSourceTest extends AtlasMappingBaseTest {

    @Test
    public void testProcessBasic() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/xmlToXml/atlasmapping-multisource-basic.xml").toURI());
        AtlasSession session = context.createSession();
        String sourceContact = AtlasTestUtil
                .loadFileAsString("src/test/resources/xmlToXml/atlas-xml-contact-attribute.xml");
        String sourceAddress = AtlasTestUtil
                .loadFileAsString("src/test/resources/xmlToXml/atlas-xml-address-attribute.xml");
        session.setInput(sourceContact, "con");
        session.setInput(sourceAddress, "addr");
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><Contact firstName=\"Ozzie\" zipCode=\"90210\"/>",
                (String) object);
    }

    @Test
    public void testProcessComplex() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/xmlToXml/atlasmapping-multisource-complex.xml").toURI());
        AtlasSession session = context.createSession();
        String sourceContact = AtlasTestUtil
                .loadFileAsString("src/test/resources/xmlToXml/atlas-xml-contact-attribute.xml");
        String sourceAddress = AtlasTestUtil
                .loadFileAsString("src/test/resources/xmlToXml/atlas-xml-address-attribute.xml");
        session.setInput(sourceContact, "con");
        session.setInput(sourceAddress, "addr");
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><XOA orderId=\"8765309\"><Contact firstName=\"Ozzie\" lastName=\"Smith\" phoneNumber=\"5551212\" zipCode=\"81111\"/><Address addressLine1=\"123 Main St\" addressLine2=\"Suite 42b\" city=\"Anytown\" state=\"NY\" zipCode=\"90210\"/></XOA>",
                (String) object);
    }
}
