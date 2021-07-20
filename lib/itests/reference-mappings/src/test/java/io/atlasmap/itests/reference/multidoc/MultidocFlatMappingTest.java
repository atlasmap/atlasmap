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

import java.io.File;

import org.junit.jupiter.api.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;
import io.atlasmap.itests.reference.AtlasTestUtil;
import io.atlasmap.java.test.BaseFlatPrimitiveClass;
import io.atlasmap.java.test.SourceFlatPrimitiveClass;
import io.atlasmap.java.test.TargetFlatPrimitiveClass;
import io.atlasmap.json.test.AtlasJsonTestUnrootedMapper;
import io.atlasmap.json.test.TargetFlatPrimitive;

public class MultidocFlatMappingTest extends AtlasMappingBaseTest {

    @Test
    public void testProcessJavaJavaFlatFieldMapping() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/multidoc/atlasmapping-flatprimitive.json").toURI());
        AtlasSession session = context.createSession();
        BaseFlatPrimitiveClass sourceJava = generateFlatPrimitiveClass(SourceFlatPrimitiveClass.class);
        session.setSourceDocument("SourceJava", sourceJava);
        String sourceJson = AtlasTestUtil
                .loadFileAsString("src/test/resources/multidoc/atlas-json-flatprimitive-unrooted.json");
        session.setSourceDocument("SourceJson", sourceJson);
        String sourceXml = AtlasTestUtil
                .loadFileAsString("src/test/resources/multidoc/atlas-xml-flatprimitive-attribute.xml");
        session.setSourceDocument("SourceXml", sourceXml);
        context.process(session);

        assertFalse(session.hasErrors(), printAudit(session));
        Object targetJava = session.getTargetDocument("TargetJava");
        assertNotNull(targetJava);
        assertTrue(targetJava instanceof TargetFlatPrimitiveClass);
        validateFlatPrimitiveClassPrimitiveFields((TargetFlatPrimitiveClass) targetJava);
        Object targetJson = session.getTargetDocument("TargetJson");
        assertNotNull(targetJson);
        assertTrue(targetJson instanceof String);
        AtlasJsonTestUnrootedMapper testMapper = new AtlasJsonTestUnrootedMapper();
        TargetFlatPrimitive targetObject = testMapper.readValue((String) targetJson, TargetFlatPrimitive.class);
        AtlasTestUtil.validateJsonFlatPrimitivePrimitiveFields(targetObject);
        Object targetXml = session.getTargetDocument("TargetXml");
        assertNotNull(targetXml);
        assertTrue(targetXml instanceof String);
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><XmlFPA booleanField=\"false\" byteField=\"99\" charField=\"a\" doubleField=\"5.0E7\" floatField=\"4.0E7\" intField=\"2\" longField=\"30000\" shortField=\"1\"/>",
                targetXml,
                targetXml.toString());
    }

    private BaseFlatPrimitiveClass generateFlatPrimitiveClass(Class<? extends BaseFlatPrimitiveClass> clazz)
            throws Exception {
        Class<?> targetClazz = this.getClass().getClassLoader().loadClass(clazz.getName());
        BaseFlatPrimitiveClass newObject = (BaseFlatPrimitiveClass) targetClazz.getDeclaredConstructor().newInstance();

        newObject.setBooleanField(false);
        newObject.setByteField((byte) 99);
        newObject.setCharField('a');
        newObject.setDoubleField(50000000d);
        newObject.setFloatField(40000000f);
        newObject.setIntField(2);
        newObject.setLongField(30000L);
        newObject.setShortField((short) 1);
        return newObject;
    }

    private void validateFlatPrimitiveClassPrimitiveFields(BaseFlatPrimitiveClass targetObject) {
        assertNotNull(targetObject);
        assertEquals(Double.valueOf(50000000d), Double.valueOf(targetObject.getDoubleField()));
        assertEquals(Float.valueOf(40000000f), Float.valueOf(targetObject.getFloatField()));
        assertEquals(Integer.valueOf(2), Integer.valueOf(targetObject.getIntField()));
        assertEquals(Long.valueOf(30000L), Long.valueOf(targetObject.getLongField()));
        assertEquals(Short.valueOf((short) 1), Short.valueOf(targetObject.getShortField()));
        assertEquals(Boolean.FALSE, targetObject.isBooleanField());
        assertEquals(Byte.valueOf((byte) 99), Byte.valueOf(targetObject.getByteField()));
        assertEquals(Character.valueOf('a'), Character.valueOf(targetObject.getCharField()));
        assertNull(targetObject.getBooleanArrayField());
        assertNull(targetObject.getBoxedBooleanArrayField());
        assertNull(targetObject.getBoxedBooleanField());
        assertNull(targetObject.getBoxedByteArrayField());
        assertNull(targetObject.getBoxedByteField());
        assertNull(targetObject.getBoxedCharArrayField());
        assertNull(targetObject.getBoxedCharField());
        assertNull(targetObject.getBoxedDoubleArrayField());
        assertNull(targetObject.getBoxedDoubleField());
        assertNull(targetObject.getBoxedFloatArrayField());
        assertNull(targetObject.getBoxedFloatField());
        assertNull(targetObject.getBoxedIntArrayField());
        assertNull(targetObject.getBoxedIntField());
        assertNull(targetObject.getBoxedLongArrayField());
        assertNull(targetObject.getBoxedLongField());
        assertNull(targetObject.getBoxedShortArrayField());
        assertNull(targetObject.getBoxedShortField());
        assertNull(targetObject.getBoxedStringArrayField());
        assertNull(targetObject.getBoxedStringField());
    }

}
