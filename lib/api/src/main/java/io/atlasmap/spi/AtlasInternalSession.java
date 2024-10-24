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

import java.util.List;

import io.atlasmap.api.AtlasSession;
import io.atlasmap.v2.*;

public interface AtlasInternalSession extends AtlasSession {

    AtlasFieldReader getFieldReader(String docId);

    <T extends AtlasFieldReader> T getFieldReader(String docId, Class<T> clazz);

    void setFieldReader(String docId, AtlasFieldReader reader);

    AtlasFieldReader removeFieldReader(String docId);

    AtlasFieldWriter getFieldWriter(String docId);

    <T extends AtlasFieldWriter> T getFieldWriter(String docId, Class<T> clazz);

    void setFieldWriter(String docId, AtlasFieldWriter writer);

    AtlasFieldWriter removeFieldWriter(String docId);

    AtlasModule resolveModule(String docId);

    Head head();

    public interface Head {

        Mapping getMapping();

        Field getSourceField();

        Field getTargetField();

        LookupTable getLookupTable();

        Head setMapping(Mapping mapping);

        Head setLookupTable(LookupTable table);

        Head setSourceField(Field sourceField);

        Head setTargetField(Field targetField);

        Head unset();

        boolean hasError();

        Head addAudit(AuditStatus status, Field field, String message);

        List<Audit> getAudits();

        //AUTOMAP:required to get the custom mapping in extension of DefaultAtlasMappingBuilder
        Head setCustomMapping(CustomMapping customMapping);
        CustomMapping getCustomMapping();
    }

}
