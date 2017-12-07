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

import java.util.Map;

import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.Audits;
import io.atlasmap.v2.Validations;

public interface AtlasSession {

    Map<String, Object> getProperties();

    AtlasContext getAtlasContext();

    void setAtlasContext(AtlasContext atlasContext);

    AtlasMapping getMapping();

    Object getDefaultSourceDocument();

    void setDefaultSourceDocument(Object sourceDoc);

    Object getSourceDocument(String docId);

    void setSourceDocument(String docId, Object sourceDoc);

    boolean hasSourceDocument(String docId);

    Map<String, Object> getSourceDocumentMap();

    Object getDefaultTargetDocument();

    void setDefaultTargetDocument(Object targetDoc);

    Object getTargetDocument(String docId);

    void setTargetDocument(String docId, Object targetDoc);

    boolean hasTargetDocument(String docId);

    Map<String, Object> getTargetDocumentMap();

    Validations getValidations();

    void setValidations(Validations validations);

    Audits getAudits();

    void setAudits(Audits audits);

    boolean hasErrors();

    boolean hasWarns();

    Integer errorCount();

    Integer warnCount();
}
