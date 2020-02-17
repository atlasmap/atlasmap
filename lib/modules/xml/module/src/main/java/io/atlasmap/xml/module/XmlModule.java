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
package io.atlasmap.xml.module;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import io.atlasmap.v2.Audit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasValidationException;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.core.BaseAtlasModule;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasModuleDetail;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.Validation;
import io.atlasmap.xml.core.XmlFieldReader;
import io.atlasmap.xml.core.XmlFieldWriter;
import io.atlasmap.xml.core.XmlIOHelper;
import io.atlasmap.xml.core.XmlPath;
import io.atlasmap.xml.v2.AtlasXmlModelFactory;
import io.atlasmap.xml.v2.XmlDataSource;
import io.atlasmap.xml.v2.XmlField;
import io.atlasmap.xml.v2.XmlNamespace;
import io.atlasmap.xml.v2.XmlNamespaces;

@AtlasModuleDetail(name = "XmlModule", uri = "atlas:xml", modes = { "SOURCE", "TARGET" }, dataFormats = {
        "xml" }, configPackages = { "io.atlasmap.xml.v2" })
public class XmlModule extends BaseAtlasModule {
    private static final Logger LOG = LoggerFactory.getLogger(XmlModule.class);

    private XmlIOHelper ioHelper;

    @Override
    public void init() throws AtlasException {
        super.init();
        this.ioHelper = new XmlIOHelper(this.getClassLoader());
    }

    @Override
    public void processPreValidation(AtlasInternalSession atlasSession) throws AtlasException {
        if (atlasSession == null || atlasSession.getMapping() == null) {
            LOG.error("Invalid session: Session and AtlasMapping must be specified");
            throw new AtlasValidationException("Invalid session");
        }

        List<Validation> xmlValidations = createValidationService().validateMapping(atlasSession.getMapping());
        atlasSession.getValidations().getValidation().addAll(xmlValidations);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Detected " + xmlValidations.size() + " xml validation notices");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processPreValidation completed", getDocId());
        }
    }

    protected XmlValidationService createValidationService() {
        XmlValidationService xmlValidationService = new XmlValidationService(getConversionService(), getFieldActionService());
        xmlValidationService.setMode(getMode());
        xmlValidationService.setDocId(getDocId());
        return xmlValidationService;
    }

    @Override
    public void processPreSourceExecution(AtlasInternalSession session) throws AtlasException {
        Object sourceDocument = session.getSourceDocument(getDocId());
        String sourceDocumentString = null;
        boolean enableNamespaces = false;
        if (sourceDocument == null || !(sourceDocument instanceof String)) {
            AtlasUtil.addAudit(session, getDocId(), String.format(
                    "Null or non-String source document: docId='%s'", getDocId()),
                    null, AuditStatus.WARN, null);
        } else {
            enableNamespaces = true;
            String param = this.getUriParameters().get("disableNamespaces");
            if (param != null && "true".equalsIgnoreCase(param)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Disabling namespace support");
                }
                enableNamespaces = false;
            }
            sourceDocumentString = String.class.cast(sourceDocument);
        }
        Document sourceXmlDocument = convertToXmlDocument(sourceDocumentString, enableNamespaces);
        XmlFieldReader reader = new XmlFieldReader(getClassLoader(), getConversionService());
        reader.setDocument(sourceXmlDocument);
        session.setFieldReader(getDocId(), reader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processPreSourceExecution completed", getDocId());
        }
    }

    /**
     * Convert a source document into XML. The modules extending this class can
     * override this to convert some format into XML so that XML field reader can read it.
     * @param source some document which can be converted to XML
     * @return converted
     */
    protected Document convertToXmlDocument(String source, boolean namespaced) throws AtlasException {
        if (source == null || source.isEmpty()) {
            return null;
        }

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(namespaced); // this must be done to use namespaces
            DocumentBuilder b = dbf.newDocumentBuilder();
            return b.parse(new ByteArrayInputStream(source.getBytes("UTF-8")));
        } catch (Exception e) {
            LOG.warn("Failed to parse XML document", e);
            return null;
        }
    };

    @Override
    public void processPreTargetExecution(AtlasInternalSession session) throws AtlasException {
        XmlNamespaces xmlNs = null;
        String template = null;
        for (DataSource ds : session.getMapping().getDataSource()) {
            if (DataSourceType.TARGET.equals(ds.getDataSourceType()) && ds instanceof XmlDataSource
                    && (ds.getId() == null || ds.getId().equals(getDocId()))) {
                xmlNs = ((XmlDataSource) ds).getXmlNamespaces();
                template = ((XmlDataSource) ds).getTemplate();
            }
        }

        Map<String, String> nsMap = new HashMap<String, String>();
        if (xmlNs != null && xmlNs.getXmlNamespace() != null && !xmlNs.getXmlNamespace().isEmpty()) {
            for (XmlNamespace ns : xmlNs.getXmlNamespace()) {
                nsMap.put(ns.getAlias(), ns.getUri());
            }
        }

        XmlFieldWriter writer = new XmlFieldWriter(getClassLoader(), nsMap, template);
        session.setFieldWriter(getDocId(), writer);

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processPreTargetExcution completed", getDocId());
        }
    }

    @Override
    public void readSourceValue(AtlasInternalSession session) throws AtlasException {
        Field sourceField = session.head().getSourceField();
        XmlFieldReader reader = session.getFieldReader(getDocId(), XmlFieldReader.class);
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
    public void populateTargetField(AtlasInternalSession session) throws AtlasException {
        Field sourceField = session.head().getSourceField();
        Field targetField = session.head().getTargetField();
        XmlPath path = new XmlPath(targetField.getPath());
        FieldGroup targetFieldGroup = null;
        if (path.hasCollection() && !path.isIndexedCollection()) {
            targetFieldGroup = AtlasModelFactory.createFieldGroupFrom(targetField, true);
            session.head().setTargetField(targetFieldGroup);
        }

        // Attempt to Auto-detect field type based on input value
        if (targetField.getFieldType() == null && sourceField.getValue() != null) {
            targetField.setFieldType(getConversionService().fieldTypeFromClass(sourceField.getValue().getClass()));
        }

        if (targetFieldGroup == null) {
            if (sourceField instanceof FieldGroup) {
                List<Field> subFields = ((FieldGroup)sourceField).getField();
                if (subFields != null && subFields.size() > 0) {
                    Integer index = targetField.getIndex();
                    if (index != null) {
                        if (subFields.size() > index) {
                            sourceField = subFields.get(index);
                        } else {
                            AtlasUtil.addAudit(session, getDocId(), String.format(
                                    "The number of source fields (%s) is smaller than target index (%s) - ignoring",
                                    subFields.size(), index),
                                    null, AuditStatus.WARN, null);
                            return;
                        }
                    } else {
                        // The last one wins for compatibility
                        sourceField = subFields.get(subFields.size() - 1);
                    }
                    session.head().setSourceField(sourceField);
                }
            }
            super.populateTargetField(session);
        } else if (sourceField instanceof FieldGroup) {
            for (int i=0; i<((FieldGroup)sourceField).getField().size(); i++) {
                Field sourceSubField = ((FieldGroup)sourceField).getField().get(i);
                XmlField targetSubField = new XmlField();
                AtlasXmlModelFactory.copyField(targetField, targetSubField, false);
                XmlPath subPath = new XmlPath(targetField.getPath());
                if (subPath.getCollectionSegmentCount() == 1) {
                    //handle asymmetric case with single target
                    subPath.setVacantCollectionIndex(i);
                } else {
                    //handle symmetric case with matching collection counts
                    List<Audit> audits = subPath.copyCollectionIndexes(new XmlPath(sourceSubField.getPath()));
                    AtlasUtil.addAudits(session, getDocId(), audits);
                }
                targetSubField.setPath(subPath.toString());
                // Attempt to Auto-detect field type based on input value
                if (targetSubField.getFieldType() == null && sourceSubField.getValue() != null) {
                    targetSubField.setFieldType(getConversionService().fieldTypeFromClass(sourceSubField.getValue().getClass()));
                }
                targetFieldGroup.getField().add(targetSubField);
                session.head().setSourceField(sourceSubField);
                session.head().setTargetField(targetSubField);
                super.populateTargetField(session);
            }
            session.head().setSourceField(sourceField);
            session.head().setTargetField(targetFieldGroup);
        } else {
            XmlField targetSubField = new XmlField();
            AtlasXmlModelFactory.copyField(targetField, targetSubField, false);
            path.setVacantCollectionIndex(0);
            targetSubField.setPath(path.toString());
            targetFieldGroup.getField().add(targetSubField);
            session.head().setTargetField(targetSubField);
            super.populateTargetField(session);
            session.head().setTargetField(targetFieldGroup);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                    "{}: processTargetFieldMapping completed: SourceField:[docId={}, path={}, type={}, value={}], TargetField:[docId={}, path={}, type={}, value={}]",
                    getDocId(), sourceField.getDocId(), sourceField.getPath(), sourceField.getFieldType(),
                    sourceField.getValue(), targetField.getDocId(), targetField.getPath(), targetField.getFieldType(),
                    targetField.getValue());
        }
    }

    @Override
    public void writeTargetValue(AtlasInternalSession session) throws AtlasException {
        XmlFieldWriter writer = session.getFieldWriter(getDocId(), XmlFieldWriter.class);
        if (session.head().getTargetField() instanceof FieldGroup) {
            FieldGroup targetFieldGroup = (FieldGroup) session.head().getTargetField();
            if (targetFieldGroup.getField().size() > 0) {
                for (Field f : targetFieldGroup.getField()) {
                    session.head().setTargetField(f);
                    writer.write(session);
                }
                return;
            }
        }
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
        XmlFieldWriter writer = session.getFieldWriter(getDocId(), XmlFieldWriter.class);
        if (writer != null && writer.getDocument() != null) {
            String targetDocumentString = convertFromXmlDocument(writer.getDocument());
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

    /**
     * Convert a target XML document into some format. The modules extending this class can
     * override this to convert interim XML document written by XML field writer into final format.
     * @param xml XML document written by XML field writer
     * @return converted
     */
    protected String convertFromXmlDocument(Document xml) throws AtlasException {
        return getXmlIOHelper().writeDocumentToString(false, xml);
    }

    @Override
    public Boolean isSupportedField(Field field) {
        if (super.isSupportedField(field)) {
            return true;
        }
        return field instanceof XmlField;
    }

    @Override
    public Field cloneField(Field field) throws AtlasException {
        return AtlasXmlModelFactory.cloneField((XmlField)field, true);
    }

    protected XmlIOHelper getXmlIOHelper() {
        return this.ioHelper;
    }

}
