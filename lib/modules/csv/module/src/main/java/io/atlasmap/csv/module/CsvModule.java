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
package io.atlasmap.csv.module;

import io.atlasmap.core.AtlasUtil;
import io.atlasmap.core.BaseAtlasModule;
import io.atlasmap.csv.core.CsvConfig;
import io.atlasmap.csv.core.CsvFieldReader;
import io.atlasmap.csv.core.CsvFieldWriter;
import io.atlasmap.csv.v2.CsvField;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;
import io.atlasmap.spi.AtlasModuleDetail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@AtlasModuleDetail(name = "CsvModule", uri = "atlas:csv", modes = { "SOURCE", "TARGET" }, dataFormats = {
        "csv" }, configPackages = { "io.atlasmap.csv.v2" })
public class CsvModule extends BaseAtlasModule {
    private static final Logger LOG = LoggerFactory.getLogger(CsvModule.class);

    @Override
    public void processPreValidation(AtlasInternalSession session) throws AtlasException {

    }

    @Override
    public void processPreSourceExecution(AtlasInternalSession session) throws AtlasException {
        Object sourceDocument = session.getSourceDocument(getDocId());
        InputStream sourceInputStream = null;

        if (sourceDocument == null || !((sourceDocument instanceof String) || (sourceDocument instanceof InputStream))) {
            AtlasUtil.addAudit(session, getDocId(), String.format(
                "Null, non-String or non-Stream source document: docId='%s'", getDocId()),
                null, AuditStatus.WARN, null);
        } else if (sourceDocument instanceof String){
            String sourceDocumentString = String.class.cast(sourceDocument);
            sourceInputStream = new ByteArrayInputStream(sourceDocumentString.getBytes());
        } else {
            sourceInputStream = (InputStream) sourceDocument;
        }

        CsvConfig csvConfig = CsvConfig.newConfig(getUriParameters());
        CsvFieldReader reader = new CsvFieldReader(csvConfig);
        reader.setDocument(sourceInputStream);
        session.setFieldReader(getDocId(), reader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processPreSourceExecution completed", getDocId());
        }
    }

    @Override
    public void processPreTargetExecution(AtlasInternalSession session) throws AtlasException {
        CsvConfig csvConfig = CsvConfig.newConfig(getUriParameters());
        CsvFieldWriter writer = new CsvFieldWriter(csvConfig);
        session.setFieldWriter(getDocId(), writer);

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processPreTargetExcution completed", getDocId());
        }
    }

    @Override
    public void readSourceValue(AtlasInternalSession session) throws AtlasException {
        Field sourceField = session.head().getSourceField();
        CsvFieldReader reader = session.getFieldReader(getDocId(), CsvFieldReader.class);
        if (reader == null) {
            AtlasUtil.addAudit(session, sourceField.getDocId(), String.format(
                "Source document '%s' doesn't exist", getDocId()),
                sourceField.getPath(), AuditStatus.ERROR, null);
            return;
        }

        reader.read(session);

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processSourceFieldMapping completed: SourceField:[docId={}, path={}, type={}, value={}]",
                getDocId(), sourceField.getDocId(), sourceField.getPath(), sourceField.getFieldType(),
                sourceField.getValue());
        }
    }

    @Override
    public void writeTargetValue(AtlasInternalSession session) throws AtlasException {
        CsvFieldWriter writer = session.getFieldWriter(getDocId(), CsvFieldWriter.class);
        writer.write(session);
    }

    @Override
    public void processPostSourceExecution(AtlasInternalSession session) throws AtlasException {
        session.removeFieldReader(getDocId());

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processPostSourceExecution completed", getDocId());
        }
    }

    @Override
    public void processPostTargetExecution(AtlasInternalSession session) throws AtlasException {
        CsvFieldWriter writer = session.getFieldWriter(getDocId(), CsvFieldWriter.class);
        if (writer != null && writer.getDocument() != null) {
            String targetDocumentString = writer.toCsv();
            session.setTargetDocument(getDocId(), targetDocumentString);
        } else {
            AtlasUtil.addAudit(session, getDocId(), String
                    .format("No target document created for DataSource:[id=%s, uri=%s]", getDocId(), this.getUri()),
                null, AuditStatus.WARN, null);
        }
        session.removeFieldWriter(getDocId());

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processPostTargetExecution completed", getDocId());
        }
    }

    @Override
    public Field cloneField(Field field) throws AtlasException {
        return CsvField.cloneOf((CsvField) field);
    }

    @Override
    public Boolean isSupportedField(Field field) {
        return field instanceof CsvField || field instanceof FieldGroup;
    }
}
