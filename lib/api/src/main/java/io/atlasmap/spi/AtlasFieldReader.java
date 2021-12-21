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
package io.atlasmap.spi;

import io.atlasmap.api.AtlasException;
import io.atlasmap.v2.Field;

/**
 * AtlasFieldReader reads the field value from source document. Each format specific module implement
 * its own field reader.
 */
public interface AtlasFieldReader {

    /**
     * Reads the field.
     * @param session session
     * @return field
     * @throws AtlasException unexpected error
     */
    Field read(AtlasInternalSession session) throws AtlasException;

}
