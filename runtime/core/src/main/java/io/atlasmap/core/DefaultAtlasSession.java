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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.atlasmap.api.AtlasConstants;
import io.atlasmap.api.AtlasContext;
import io.atlasmap.spi.AtlasFieldReader;
import io.atlasmap.spi.AtlasFieldWriter;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.Audit;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.Audits;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.LookupTable;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.Validations;

public class DefaultAtlasSession implements AtlasInternalSession {

    private AtlasContext atlasContext;
    private final AtlasMapping mapping;
    private Audits audits;
    private Validations validations;
    private Map<String, Object> properties;
    private Map<String, Object> sourceMap = new HashMap<>();
    private Map<String, Object> targetMap = new HashMap<>();
    private Map<String, AtlasFieldReader> fieldReaderMap = new HashMap<>();
    private Map<String, AtlasFieldWriter> fieldWriterMap = new HashMap<>();
    private Head head = new HeadImpl();

    public DefaultAtlasSession(AtlasMapping mapping) {
        initialize();
        this.mapping = mapping;
    }

    protected void initialize() {
        properties = new ConcurrentHashMap<String, Object>();
        validations = new Validations();
        audits = new Audits();
        head.unset();
    }

    @Override
    public AtlasContext getAtlasContext() {
        return atlasContext;
    }

    @Override
    public void setAtlasContext(AtlasContext atlasContext) {
        this.atlasContext = atlasContext;
        head.unset();
    }

    @Override
    public AtlasMapping getMapping() {
        return mapping;
    }

    @Override
    public Validations getValidations() {
        return this.validations;
    }

    @Override
    public void setValidations(Validations validations) {
        this.validations = validations;
    }

    @Override
    public Audits getAudits() {
        return this.audits;
    }

    @Override
    public void setAudits(Audits audits) {
        this.audits = audits;
    }

    @Override
    public Object getDefaultSourceDocument() {
        return sourceMap.get(AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID);
    }

    @Override
    public Object getSourceDocument(String docId) {
        if (docId == null || docId.isEmpty()) {
            return getDefaultSourceDocument();
        }
        if (sourceMap.containsKey(docId)) {
            return sourceMap.get(docId);
        } else if (sourceMap.size() == 1 && sourceMap.containsKey(AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID)) {
            AtlasUtil.addAudit(this, null, String.format(
                    "There's no source document with docId='%s', returning default", docId),
                    null, AuditStatus.WARN, null);
            return getDefaultSourceDocument();
        }
        AtlasUtil.addAudit(this, null, String.format(
                "There's no source document with docId='%s'", docId), null, AuditStatus.WARN, null);
        return null;
    }

    @Override
    public boolean hasSourceDocument(String docId) {
        if (docId == null || docId.isEmpty()) {
            return sourceMap.containsKey(AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID);
        }
        return sourceMap.containsKey(docId);
    }

    @Override
    public Map<String, Object> getSourceDocumentMap() {
        return Collections.unmodifiableMap(sourceMap);
    }

    @Override
    public Object getDefaultTargetDocument() {
        return targetMap.get(AtlasConstants.DEFAULT_TARGET_DOCUMENT_ID);
    }

    @Override
    public Object getTargetDocument(String docId) {
        if (docId == null || docId.isEmpty()) {
            return getDefaultTargetDocument();
        }
        if (targetMap.containsKey(docId)) {
            return targetMap.get(docId);
        } else if (targetMap.size() == 1 && targetMap.containsKey(AtlasConstants.DEFAULT_TARGET_DOCUMENT_ID)) {
            AtlasUtil.addAudit(this, null, String.format(
                    "There's no target document with docId='%s', returning default", docId),
                    null, AuditStatus.WARN, null);
            return getDefaultTargetDocument();
        }
        AtlasUtil.addAudit(this, null, String.format(
                "There's no target document with docId='%s'", docId), null, AuditStatus.WARN, null);
        return null;
    }

    @Override
    public boolean hasTargetDocument(String docId) {
        if (docId == null || docId.isEmpty()) {
            return targetMap.containsKey(AtlasConstants.DEFAULT_TARGET_DOCUMENT_ID);
        }
        return targetMap.containsKey(docId);
    }

    @Override
    public Map<String, Object> getTargetDocumentMap() {
        return Collections.unmodifiableMap(targetMap);
    }

    @Override
    public void setDefaultSourceDocument(Object sourceDoc) {
        this.sourceMap.put(AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID, sourceDoc);
    }

    @Override
    public void setSourceDocument(String docId, Object sourceDoc) {
        if (docId == null || docId.isEmpty()) {
            setDefaultSourceDocument(sourceDoc);
        } else {
            // first document is mapped to 'default' as well
            if (this.sourceMap.isEmpty()) {
                setDefaultSourceDocument(sourceDoc);
            }
            this.sourceMap.put(docId, sourceDoc);
        }
    }

    @Override
    public void setDefaultTargetDocument(Object targetDoc) {
        this.targetMap.put(AtlasConstants.DEFAULT_TARGET_DOCUMENT_ID, targetDoc);
    }

    @Override
    public void setTargetDocument(String docId, Object targetDoc) {
        if (docId == null || docId.isEmpty()) {
            setDefaultTargetDocument(targetDoc);
        } else {
            // first document is mapped to 'default' as well
            if (this.targetMap.isEmpty()) {
                setDefaultTargetDocument(targetDoc);
            }
            this.targetMap.put(docId, targetDoc);
        }
    }

    @Override
    public AtlasFieldReader getFieldReader(String docId) {
        if (docId == null || docId.isEmpty()) {
            return this.fieldReaderMap.get(AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID);
        }
        return this.fieldReaderMap.get(docId);
    }

    @Override
    public <T extends AtlasFieldReader> T getFieldReader(String docId, Class<T> clazz) {
        return clazz.cast(getFieldReader(docId));
    }

    @Override
    public void setFieldReader(String docId, AtlasFieldReader reader) {
        if (docId == null || docId.isEmpty()) {
            this.fieldReaderMap.put(AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID, reader);
        } else {
            this.fieldReaderMap.put(docId, reader);
        }
    }

    @Override
    public AtlasFieldReader removeFieldReader(String docId) {
        if (docId == null || docId.isEmpty()) {
            return this.fieldReaderMap.remove(AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID);
        }
        return fieldReaderMap.remove(docId);
    }

    @Override
    public AtlasFieldWriter getFieldWriter(String docId) {
        if (docId == null || docId.isEmpty()) {
            return this.fieldWriterMap.get(AtlasConstants.DEFAULT_TARGET_DOCUMENT_ID);
        }
        return this.fieldWriterMap.get(docId);
    }

    @Override
    public <T extends AtlasFieldWriter> T getFieldWriter(String docId, Class<T> clazz) {
        return clazz.cast(getFieldWriter(docId));
    }

    @Override
    public void setFieldWriter(String docId, AtlasFieldWriter writer) {
        if (docId == null || docId.isEmpty()) {
            this.fieldWriterMap.put(AtlasConstants.DEFAULT_TARGET_DOCUMENT_ID, writer);
        }
        this.fieldWriterMap.put(docId, writer);
    }

    @Override
    public AtlasFieldWriter removeFieldWriter(String docId) {
        if (docId == null || docId.isEmpty()) {
            return this.fieldWriterMap.remove(AtlasConstants.DEFAULT_TARGET_DOCUMENT_ID);
        }
        return this.fieldWriterMap.remove(docId);
    }

    @Override
    public Head head() {
        return this.head;
    }

    @Override
    public Map<String, Object> getProperties() {
        return this.properties;
    }

    @Override
    public Integer errorCount() {
        int e = 0;
        for (Audit audit : getAudits().getAudit()) {
            if (AuditStatus.ERROR.equals(audit.getStatus())) {
                e++;
            }
        }
        return e;
    }

    @Override
    public boolean hasErrors() {
        for (Audit audit : getAudits().getAudit()) {
            if (AuditStatus.ERROR.equals(audit.getStatus())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasWarns() {
        for (Audit audit : getAudits().getAudit()) {
            if (AuditStatus.WARN.equals(audit.getStatus())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Integer warnCount() {
        int w = 0;
        for (Audit audit : getAudits().getAudit()) {
            if (AuditStatus.WARN.equals(audit.getStatus())) {
                w++;
            }
        }
        return w;
    }

    private class HeadImpl implements Head {
        private Mapping mapping;
        private LookupTable lookupTable;
        private Field sourceField;
        private Field targetField;
        private List<Audit> audits = new LinkedList<Audit>();

        @Override
        public Mapping getMapping() {
            return this.mapping;
        }

        @Override
        public LookupTable getLookupTable() {
            return this.lookupTable;
        }

        @Override
        public Field getSourceField() {
            return this.sourceField;
        }

        @Override
        public Field getTargetField() {
            return this.targetField;
        }

        @Override
        public Head setMapping(Mapping mapping) {
            this.mapping = mapping;
            return this;
        }

        @Override
        public Head setLookupTable(LookupTable table) {
            this.lookupTable = table;
            return this;
        }

        @Override
        public Head setSourceField(Field sourceField) {
            this.sourceField = sourceField;
            return this;
        }

        @Override
        public Head setTargetField(Field targetField) {
            this.targetField = targetField;
            return this;
        }

        @Override
        public Head unset() {
            this.mapping = null;
            this.lookupTable = null;
            this.sourceField = null;
            this.targetField = null;
            return this;
        }

        @Override
        public boolean hasError() {
            for(Audit audit : audits) {
                if (audit.getStatus() == AuditStatus.ERROR) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Head addAudit(AuditStatus status, String docId, String path, String message) {
            Audit audit = AtlasUtil.createAudit(status, docId, path, null, message);
            this.audits.add(audit);
            return this;
        }

        @Override
        public List<Audit> getAudits() {
            return this.audits;
        }

    }

}
