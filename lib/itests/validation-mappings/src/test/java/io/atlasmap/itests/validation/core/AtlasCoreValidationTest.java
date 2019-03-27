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
package io.atlasmap.itests.validation.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.ValidationScope;
import io.atlasmap.v2.ValidationStatus;
import io.atlasmap.v2.Validations;
import io.atlasmap.itests.validation.AtlasMappingBaseTest;
import io.atlasmap.validators.AtlasValidationTestHelper;

public class AtlasCoreValidationTest extends AtlasMappingBaseTest {

    @Rule
    public TestName name = new TestName();

    protected AtlasContext context = null;
    protected AtlasSession session = null;

    @Before
    public void setUp() {
        super.setUp();
        try {
            context = atlasContextFactory.createContext(
                    new File("src/test/resources/validation/core/atlasmapping-" + name.getMethodName() + ".json"));
            session = context.createSession();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error initializing test: " + e.getMessage(), e);

        }
    }

    @After
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void testMappingNameNull() throws AtlasException {
        assertNotNull(context);
        assertNotNull(session);
        context.processValidation(session);
        Validations validations = session.getValidations();
        assertNotNull(validations);
        assertNotNull(validations.getValidation());
        assertFalse(validations.getValidation().isEmpty());

        boolean found = false;
        for (Validation v : validations.getValidation()) {
            if ("Mapping name must not be null nor empty".equals(v.getMessage())) {
                found = true;
                assertEquals(ValidationStatus.ERROR, v.getStatus());
                assertEquals(ValidationScope.ALL, v.getScope());
            }
            System.out.println(AtlasValidationTestHelper.validationToString(v));

        }

        assertTrue(found);
    }

    @Test
    public void testMappingNameEmpty() throws AtlasException {
        assertNotNull(context);
        assertNotNull(session);
        context.processValidation(session);
        Validations validations = session.getValidations();
        assertNotNull(validations);
        assertNotNull(validations.getValidation());
        assertFalse(validations.getValidation().isEmpty());

        boolean found = false;
        for (Validation v : validations.getValidation()) {
            if ("Mapping name must not be null nor empty".equals(v.getMessage())) {
                found = true;
                assertEquals(ValidationStatus.ERROR, v.getStatus());
                assertEquals(ValidationScope.ALL, v.getScope());
            }
        }

        assertTrue(found);
    }

    @Test
    public void testJavaToJson() throws AtlasException {
        assertNotNull(context);
        assertNotNull(session);
        context.processValidation(session);
        Validations validations = session.getValidations();
        assertNotNull(validations);
        assertNotNull(validations.getValidation());
        assertTrue(validations.getValidation().isEmpty());
    }

    @Test
    public void testJavaToXml() throws AtlasException {
        assertNotNull(context);
        assertNotNull(session);
        context.processValidation(session);
        Validations validations = session.getValidations();
        assertNotNull(validations);
        assertNotNull(validations.getValidation());
        assertTrue(validations.getValidation().isEmpty());
    }

    @Test
    public void testJsonToJava() throws AtlasException {
        assertNotNull(context);
        assertNotNull(session);
        context.processValidation(session);
        Validations validations = session.getValidations();
        assertNotNull(validations);
        assertNotNull(validations.getValidation());
        assertTrue(validations.getValidation().isEmpty());
    }

    @Test
    public void testJsonToXml() throws AtlasException {
        assertNotNull(context);
        assertNotNull(session);
        context.processValidation(session);
        Validations validations = session.getValidations();
        assertNotNull(validations);
        assertNotNull(validations.getValidation());
        assertTrue(validations.getValidation().isEmpty());
    }

    @Test
    public void testXmlToJava() throws AtlasException {
        assertNotNull(context);
        assertNotNull(session);
        context.processValidation(session);
        Validations validations = session.getValidations();
        assertNotNull(validations);
        assertNotNull(validations.getValidation());
        assertTrue(validations.getValidation().isEmpty());
    }

    @Test
    public void testXmlToJson() throws AtlasException {
        assertNotNull(context);
        assertNotNull(session);
        context.processValidation(session);
        Validations validations = session.getValidations();
        assertNotNull(validations);
        assertNotNull(validations.getValidation());
        assertTrue(validations.getValidation().isEmpty());
    }
}
