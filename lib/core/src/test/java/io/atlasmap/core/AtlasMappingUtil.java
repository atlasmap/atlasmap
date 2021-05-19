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
package io.atlasmap.core;

import java.io.File;

import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.Json;

public class AtlasMappingUtil {

    public AtlasMapping loadMapping(String fileName) throws Exception {
        return Json.mapper().readValue(new File(fileName), AtlasMapping.class);
    }

    public void marshallMapping(AtlasMapping mapping, String fileName) throws Exception {
        Json.mapper().writeValue(new File(fileName), mapping);
    }
}
