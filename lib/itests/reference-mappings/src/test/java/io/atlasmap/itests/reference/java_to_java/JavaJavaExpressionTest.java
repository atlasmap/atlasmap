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
package io.atlasmap.itests.reference.java_to_java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;
import io.atlasmap.itests.reference.AtlasTestUtil;
import io.atlasmap.java.test.BaseAddress;
import io.atlasmap.java.test.BaseContact;
import io.atlasmap.java.test.SourceAddress;
import io.atlasmap.java.test.SourceContact;
import io.atlasmap.java.test.TargetContact;

public class JavaJavaExpressionTest extends AtlasMappingBaseTest {

    @Test
    public void test() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/javaToJava/atlasmapping-expression.json").toURI());
        AtlasSession session = context.createSession();
        BaseContact sourceContact = AtlasTestUtil.generateContact(SourceContact.class);
        sourceContact.setFirstName("Ozzie");
        BaseAddress sourceAddress = AtlasTestUtil.generateAddress(SourceAddress.class);
        sourceAddress.setZipCode("90210");
        session.setSourceDocument("con", sourceContact);
        session.setSourceDocument("addr", sourceAddress);
        context.process(session);

        assertFalse(printAudit(session), session.hasErrors());
        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertEquals(TargetContact.class.getName(), object.getClass().getName());
        TargetContact targetContact = (TargetContact) object;
        assertEquals("Ozzie90210", targetContact.getFirstName());
    }

}
