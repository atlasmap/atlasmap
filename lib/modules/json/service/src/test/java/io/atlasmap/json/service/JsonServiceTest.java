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
package io.atlasmap.json.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JsonServiceTest {

    private JsonService jsonService = null;

    @BeforeEach
    public void setUp() {
        jsonService = new JsonService();
    }

    @AfterEach
    public void tearDown() {
        jsonService = null;
    }

    @Test
    public void testValidJsonData() {
        assertTrue(jsonService.validJsonData("{ \"foo\":\"bar\" }"));
        assertTrue(jsonService.validJsonData("[ { \"foo\":\"bar\" }, { \"meow\":\"blah\" } ]"));
        assertFalse(jsonService.validJsonData("  [ { \"foo\":\"bar\" }, { \"meow\":\"blah\" } ]"));
        assertFalse(jsonService.validJsonData("  \"foo\":\"bar\" }, { \"meow\":\"blah\" } ]"));

        assertTrue(jsonService.validJsonData(jsonService.cleanJsonData("  { \"foo\":\"bar\" }")));
        assertTrue(jsonService.validJsonData(jsonService.cleanJsonData("{ \"foo\":\"bar\" }   ")));
        assertTrue(jsonService
                .validJsonData(jsonService.cleanJsonData("  [ { \"foo\":\"bar\" }, { \"meow\":\"blah\" } ]")));
        assertTrue(jsonService.validJsonData(jsonService.cleanJsonData("\b\t\n\f\r   { \"foo\":\"bar\" }")));

    }
}
