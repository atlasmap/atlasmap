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

import io.atlasmap.api.AtlasException;
import io.atlasmap.spi.AtlasFieldReader;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.Mapping;

/**
 * Limited version of AtlasMap session dedicated for preview processing.
 * Since preview exchanges field values via {@code Field} object, {@code PreviewFieldReader}
 * just have to look into the values of source fields no matter what the Document ID is.
 */
class DefaultAtlasPreviewSession extends DefaultAtlasSession {

    private AtlasFieldReader reader;

    DefaultAtlasPreviewSession(DefaultAtlasContext context, Mapping mapping) throws AtlasException {
        super(context);
        reader = new PreviewFieldReader(mapping);
    }

    private class PreviewFieldReader implements AtlasFieldReader {
        private Mapping mapping;

        PreviewFieldReader(Mapping mapping) {
            this.mapping = mapping;
        }

        @Override
        public Field read(AtlasInternalSession session) throws AtlasException {
            String path = session.head().getSourceField().getPath();
            FieldGroup sourceFieldGroup = mapping.getInputFieldGroup();
            if (sourceFieldGroup != null) {
                 return readFromGroup(sourceFieldGroup, path);
            }
            for (Field f : mapping.getInputField()) {
                if (f.getPath() != null && f.getPath().equals(path)) {
                    return f;
                }
            }
            return null;
        }

        private Field readFromGroup(FieldGroup group, String path) {
            if (group.getField() == null) {
                return null;
            }
            for (Field f : group.getField()) {
                if (f.getPath() != null && f.getPath().equals(path)) {
                    return f;
                }
                if (f instanceof FieldGroup) {
                    Field deeper = readFromGroup((FieldGroup)f, path);
                    if (deeper != null) {
                        return deeper;
                    }
                }
            }
            return null;
        }

    }

    @Override
    public AtlasFieldReader getFieldReader(String docId) {
        return reader;
    }

    @Override
    public <T extends AtlasFieldReader> T getFieldReader(String docId, Class<T> clazz) {
        if (clazz.isInstance(reader)) {
            return clazz.cast(reader);
        }
        throw new IllegalArgumentException(clazz.getName() + " is not available for preview");
    }

}
