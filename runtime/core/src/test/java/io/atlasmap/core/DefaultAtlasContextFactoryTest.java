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
package io.atlasmap.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.BeforeClass;
import org.junit.Test;

public class DefaultAtlasContextFactoryTest {

    private static String threadName = null;
    private DefaultAtlasContextFactory factory = null;

    @BeforeClass
    public static void beforeClass() {
        threadName = Thread.currentThread().getName();
    }

    @Test
    public void testInitDestroy() {
        factory = new DefaultAtlasContextFactory();
        factory.init();

        assertNotNull(factory);
        assertEquals(threadName, factory.getThreadName());
        assertEquals("io.atlasmap.core.DefaultAtlasContextFactory", factory.getClassName());
        assertNotNull(factory.getUuid());
        assertNotNull(factory.getJmxObjectName());
        assertNotNull(factory.getMappingService());
        assertNotNull(factory.getModuleInfoRegistry());

        factory.destroy();
        assertNotNull(factory);
        assertNull(factory.getThreadName());
        assertEquals("io.atlasmap.core.DefaultAtlasContextFactory", factory.getClassName());
        assertNull(factory.getUuid());
        assertNull(factory.getJmxObjectName());
        assertNull(factory.getMappingService());
        assertNull(factory.getModuleInfoRegistry());
    }

    @Test
    public void testInitDestroyInitDestroy() {
        factory = new DefaultAtlasContextFactory();
        factory.init();
        String origUuid = factory.getUuid();

        assertNotNull(factory);
        assertEquals(threadName, factory.getThreadName());
        assertEquals("io.atlasmap.core.DefaultAtlasContextFactory", factory.getClassName());
        assertNotNull(factory.getUuid());
        assertNotNull(factory.getJmxObjectName());
        assertNotNull(factory.getMappingService());
        assertNotNull(factory.getModuleInfoRegistry());

        factory.destroy();
        assertNotNull(factory);
        assertNull(factory.getThreadName());
        assertEquals("io.atlasmap.core.DefaultAtlasContextFactory", factory.getClassName());
        assertNull(factory.getUuid());
        assertNull(factory.getJmxObjectName());
        assertNull(factory.getMappingService());
        assertNull(factory.getModuleInfoRegistry());

        factory.init();
        assertNotNull(factory);
        assertEquals(threadName, factory.getThreadName());
        assertEquals("io.atlasmap.core.DefaultAtlasContextFactory", factory.getClassName());
        assertNotNull(factory.getUuid());
        assertNotNull(factory.getJmxObjectName());
        assertNotNull(factory.getMappingService());
        assertNotNull(factory.getModuleInfoRegistry());
        assertNotEquals(origUuid, factory.getUuid());

        factory.destroy();
        assertNotNull(factory);
        assertNull(factory.getThreadName());
        assertEquals("io.atlasmap.core.DefaultAtlasContextFactory", factory.getClassName());
        assertNull(factory.getUuid());
        assertNull(factory.getJmxObjectName());
        assertNull(factory.getMappingService());
        assertNull(factory.getModuleInfoRegistry());
    }

    @Test
    public void testStaticFactoryInitDestroy() {
        factory = DefaultAtlasContextFactory.getInstance();
        assertNotNull(factory);
        assertEquals(threadName, factory.getThreadName());
        assertEquals("io.atlasmap.core.DefaultAtlasContextFactory", factory.getClassName());
        assertNotNull(factory.getUuid());
        assertNotNull(factory.getJmxObjectName());
        assertNotNull(factory.getMappingService());
        assertNotNull(factory.getModuleInfoRegistry());

        factory.destroy();
        assertNotNull(factory);
        assertNull(factory.getThreadName());
        assertEquals("io.atlasmap.core.DefaultAtlasContextFactory", factory.getClassName());
        assertNull(factory.getUuid());
        assertNull(factory.getJmxObjectName());
        assertNull(factory.getMappingService());
        assertNull(factory.getModuleInfoRegistry());
    }
}
