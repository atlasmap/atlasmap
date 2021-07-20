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
package io.atlasmap.java.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class AtlasJavaModelFactoryTest {

    @Test
    public void testCreateJavaClass() {
        JavaClass javaClass = AtlasJavaModelFactory.createJavaClass();
        assertNotNull(javaClass);
        assertNotNull(javaClass.getJavaFields());
        assertNotNull(javaClass.getJavaFields().getJavaField());
        assertEquals(Integer.valueOf(0), Integer.valueOf(javaClass.getJavaFields().getJavaField().size()));
        assertNotNull(javaClass.getJavaEnumFields());
        assertNotNull(javaClass.getJavaEnumFields().getJavaEnumField());
        assertEquals(Integer.valueOf(0), Integer.valueOf(javaClass.getJavaEnumFields().getJavaEnumField().size()));
    }

}

