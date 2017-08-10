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
package io.atlasmap.java.inspect.itests;

import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.inspect.ClassInspectionService;
import io.atlasmap.java.v2.JavaClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;

public class Twitter4jInspectTest {

    private static final Logger logger = LoggerFactory.getLogger(Twitter4jInspectTest.class);

    private ClassInspectionService classInspectionService = null;

    @Before
    public void setUp() {
        classInspectionService = new ClassInspectionService();
        classInspectionService.setConversionService(DefaultAtlasConversionService.getRegistry());
    }

    @After
    public void tearDown() {
        classInspectionService = null;
    }

    @Test
    public void testInspectTwitter4jStatus() {
        JavaClass j = classInspectionService.inspectClass("twitter4j.Status");
        assertNotNull(j);
        logger.debug("Hello");
    }

    @Test
    public void testInspectTwitter4jStatusJSONImpl() {
        JavaClass j = classInspectionService.inspectClass("twitter4j.StatusJSONImpl");
        assertNotNull(j);
        logger.debug("Hello");
    }

}
