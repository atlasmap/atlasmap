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
package io.atlasmap.java.inspect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.test.TestMapOrders;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.v2.CollectionType;

public class MapTest {

    private ClassInspectionService classInspectionService = null;

    @Before
    public void setUp() {
        classInspectionService = new ClassInspectionService();
        classInspectionService.setConversionService(DefaultAtlasConversionService.getInstance());
    }

    @After
    public void tearDown() {
        classInspectionService = null;
    }

    @Test
    public void testInspectJavaMapField() {
        JavaClass c = classInspectionService.inspectClass(TestMapOrders.class, CollectionType.NONE, null);
        assertNotNull(c);
        assertEquals("io.atlasmap.java.test.TestMapOrders", c.getClassName());
        assertNotNull(c.getJavaFields().getJavaField());
        assertEquals(c.getJavaFields().getJavaField().size(), 1);
        JavaField field = c.getJavaFields().getJavaField().get(0);
        assertEquals(field.getCollectionType().value(), "Map");
    }

    @Test
    public void testInspectJavaMapRoot() {

        JavaClass c = classInspectionService.inspectClass(Map.class, CollectionType.MAP, null);
        assertEquals("java.util.Map", c.getClassName());
        assertEquals(c.getCollectionType().value(), "Map");

    }
}
