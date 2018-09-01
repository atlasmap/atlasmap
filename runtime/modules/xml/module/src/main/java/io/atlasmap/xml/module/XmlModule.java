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

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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

    @Override
    public void processPreValidation(AtlasInternalSession atlasSession) throws AtlasException {
        if (atlasSession == null || atlasSession.getMapping() == null) {
            LOG.error("Invalid session: Session and AtlasMapping must be specified");
            throw new AtlasValidationException("Invalid session");
        }

        XmlValidationService xmlValidationService = new XmlValidationService(getConversionService());
        xmlValidationService.setDocId(getDocId());
        List<Validation> xmlValidations = xmlValidationService.validateMapping(atlasSession.getMapping());
        atlasSession.getValidations().getValidation().addAll(xmlValidations);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Detected " + xmlValidations.size() + " xml validation notices");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processPreValidation completed", getDocId());
        }
    }

    @Override
    public void processPreSourceExecution(AtlasInternalSession session) throws AtlasException {
        Object sourceDocument = session.getSourceDocument(getDocId());
        if (sourceDocument == null || !(sourceDocument instanceof String)) {
            AtlasUtil.addAudit(session, getDocId(), String.format(
                    "Null or non-String source document: docId='%s'", getDocId()),
                    null, AuditStatus.WARN, null);
        } else {
            Map<String, String> sourceUriParams = AtlasUtil.getUriParameters(getUri());
            boolean enableNamespaces = true;
            for (String key : sourceUriParams.keySet()) {
                if ("disableNamespaces".equals(key) && ("true".equals(sourceUriParams.get("disableNamespaces")))) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Disabling namespace support");
                    }
                    enableNamespaces = false;
                }
            }

            XmlFieldReader reader = new XmlFieldReader(getConversionService());
            reader.setDocument(String.class.cast(sourceDocument), enableNamespaces);
            session.setFieldReader(getDocId(), reader);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processPreSourceExecution completed", getDocId());
        }
    }

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

        XmlFieldWriter writer = new XmlFieldWriter(nsMap, template);
        session.setFieldWriter(getDocId(), writer);

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processPreTargetExcution completed", getDocId());
        }
    }

    @Override
    public void processSourceFieldMapping(AtlasInternalSession session) throws AtlasException {
        Field sourceField = session.head().getSourceField();
        XmlFieldReader reader = session.getFieldReader(getDocId(), XmlFieldReader.class);
        if (reader == null) {
            AtlasUtil.addAudit(session, sourceField.getDocId(), String.format(
                    "Source document '%s' doesn't exist", getDocId()),
                    sourceField.getPath(), AuditStatus.ERROR, null);
            return;
        }
        reader.read(session);
        sourceField = applySourceFieldActions(session);
        session.head().setSourceField(sourceField);

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processSourceFieldMapping completed: SourceField:[docId={}, path={}, type={}, value={}]",
                    getDocId(), sourceField.getDocId(), sourceField.getPath(), sourceField.getFieldType(),
                    sourceField.getValue());
        }
    }

    @Override
    public void processTargetFieldMapping(AtlasInternalSession session) throws AtlasException {
        Field sourceField = session.head().getSourceField();
        Field targetField = session.head().getTargetField();
        XmlPath path = new XmlPath(targetField.getPath());
        FieldGroup targetFieldGroup = null;
        if (path.hasCollection() && !path.isIndexedCollection()) {
            targetFieldGroup = AtlasModelFactory.createFieldGroupFrom(targetField);
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
                    // The last one wins for compatibility
                    sourceField = subFields.get(subFields.size() - 1);
                    session.head().setSourceField(sourceField);
                }
            }
            populateTargetFieldValue(session);
        } else if (sourceField instanceof FieldGroup) {
            for (int i=0; i<((FieldGroup)sourceField).getField().size(); i++) {
                Field sourceSubField = ((FieldGroup)sourceField).getField().get(i);
                XmlField targetSubField = new XmlField();
                AtlasXmlModelFactory.copyField(targetField, targetSubField, false);
                XmlPath subPath = new XmlPath(targetField.getPath());
                subPath.setVacantCollectionIndex(i);
                targetSubField.setPath(subPath.toString());
                targetFieldGroup.getField().add(targetSubField);
                session.head().setSourceField(sourceSubField);
                session.head().setTargetField(targetSubField);
                populateTargetFieldValue(session);
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
            populateTargetFieldValue(session);
            session.head().setTargetField(targetFieldGroup);
        }

        session.head().setTargetField(applyTargetFieldActions(session));

        XmlFieldWriter writer = session.getFieldWriter(getDocId(), XmlFieldWriter.class);
        if (targetFieldGroup != null) {
            for (Field f : targetFieldGroup.getField()) {
                session.head().setTargetField(f);
                writer.write(session);
            }
        } else {
            writer.write(session);
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
            session.setTargetDocument(getDocId(), convertDocumentToString(writer.getDocument()));
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
    public Boolean isSupportedField(Field field) {
        if (super.isSupportedField(field)) {
            return true;
        }
        return field instanceof XmlField;
    }

    private String convertDocumentToString(Document document) throws AtlasException {
        DocumentBuilderFactory domFact = DocumentBuilderFactory.newInstance();
        domFact.setNamespaceAware(true);

        StringWriter writer = null;
        try {
            DOMSource domSource = new DOMSource(document);
            writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            return writer.toString();
        } catch (TransformerException e) {
            LOG.error(String.format("Error converting Xml document to string msg=%s", e.getMessage()), e);
            throw new AtlasException(e.getMessage(), e);
        }
    }

    @Override
    public Field cloneField(Field field) throws AtlasException {
        return AtlasXmlModelFactory.cloneField(field);
    }
}
