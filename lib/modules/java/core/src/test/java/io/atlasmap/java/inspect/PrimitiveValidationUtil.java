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
package io.atlasmap.java.inspect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.atlasmap.core.AtlasUtil;
import io.atlasmap.java.v2.AtlasJavaModelFactory;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.v2.CollectionType;

public class PrimitiveValidationUtil {

    public static void validatePrimitive(JavaClass j, String name) throws Exception {
        validatePrimitiveCommon(j);
        assertEquals(name, j.getClassName());
        assertEquals(CollectionType.NONE, j.getCollectionType());
        assertNull(j.getArrayDimensions());
    }

    public static void validatePrimitiveArray(JavaClass j, String name, int dim) throws Exception {
        validatePrimitiveCommon(j);
        assertEquals(name, j.getClassName());
        assertEquals(Integer.valueOf(dim), j.getArrayDimensions());
        assertEquals(CollectionType.ARRAY, j.getCollectionType());
    }

    protected static void validatePrimitiveCommon(JavaClass j) throws Exception {
        assertNotNull(j);
        assertFalse(j.isAnnonymous());
        assertFalse(j.isAnnotation());
        assertFalse(j.isEnumeration());
        assertFalse(j.isInterface());
        assertFalse(j.isLocalClass());
        assertFalse(j.isMemberClass());
        assertTrue(j.isPrimitive());
        assertFalse(j.isSynthetic());
        assertNotNull(j.getJavaFields());
        assertNotNull(j.getJavaFields().getJavaField());
        assertTrue(j.getJavaFields().getJavaField().size() == 0);
        assertNotNull(j.getJavaEnumFields());
        assertNotNull(j.getJavaEnumFields().getJavaEnumField());
        assertTrue(j.getJavaEnumFields().getJavaEnumField().size() == 0);
        assertNull(j.getPackageName());
        assertNotNull(j.getUri());
        assertEquals(String.format(AtlasJavaModelFactory.URI_FORMAT,
                AtlasUtil.escapeForUri(j.getClassName())), j.getUri());
    }
}
