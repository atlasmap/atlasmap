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
package io.atlasmap.api;

import io.atlasmap.v2.Audits;
import io.atlasmap.v2.Mapping;

public interface AtlasContext {

    AtlasContextFactory getContextFactory();

    AtlasSession createSession() throws AtlasException;

    /*
     * https://github.com/atlasmap/atlasmap/issues/872
     * Consider moving following 3 methods into AtlasSession in V2
     */

    void process(AtlasSession session) throws AtlasException;

    void processValidation(AtlasSession session) throws AtlasException;

    Audits processPreview(Mapping mapping) throws AtlasException;

}
