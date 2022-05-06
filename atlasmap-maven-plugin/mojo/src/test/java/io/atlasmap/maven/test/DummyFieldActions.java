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
package io.atlasmap.maven.test;

import java.util.Arrays;
import java.util.List;

import io.atlasmap.spi.AtlasActionProcessor;
import io.atlasmap.spi.AtlasFieldAction;

public class DummyFieldActions implements AtlasFieldAction {

    @AtlasActionProcessor
    public static Number dummyOneToOne(DummyOneToOne action, String input) {
        return 0;
    }

    @AtlasActionProcessor
    public static List<Number> dummyOneToMany(DummyOneToMany action, String input) {
        return Arrays.asList(0);
    }

    @AtlasActionProcessor
    public static Number dummyManyToOne(DummyManyToOne action, List<String> input) {
        return 0;
    }

    @AtlasActionProcessor
    public static Number dummyZeroToOne(DummyZeroToOne action) {
        return 0;
    }

}
