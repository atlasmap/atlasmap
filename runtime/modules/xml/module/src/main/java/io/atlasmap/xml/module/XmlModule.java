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
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasValidationException;
import io.atlasmap.core.AtlasPath.SegmentContext;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.core.BaseAtlasModule;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasModuleDetail;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.LookupTable;
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

    @Override
    public void processPreValidation(AtlasInternalSession atlasSession) throws AtlasException {
        if (atlasSession == null || atlasSession.getMapping() == null) {
            LOG.error("Invalid session: Session and AtlasMapping must be specified");
            throw new AtlasValidationException("Invalid session");
        }

        XmlValidationService xmlValidationService = new XmlValidationService(getConversionService());
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
            throw new AtlasException(String.format("Unsupported source document '%s'", sourceDocument));
        }

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

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processPreSourceExecution completed", getDocId());
        }
    }

    @Override
    public void processPreTargetExecution(AtlasInternalSession session) throws AtlasException {
        XmlNamespaces xmlNs = null;
        String template = null;
        for (DataSource ds : session.getMapping().getDataSource()) {
            if (DataSourceType.TARGET.equals(ds.getDataSourceType()) && ds instanceof XmlDataSource) {
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
        reader.read(session);

        if (sourceField.getActions() != null && sourceField.getActions().getActions() != null) {
            getFieldActionService().processActions(sourceField.getActions(), sourceField);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processSourceFieldMapping completed: SourceField:[docId={}, path={}, type={}, value={}]",
                    getDocId(), sourceField.getDocId(), sourceField.getPath(), sourceField.getFieldType(), sourceField.getValue());
        }
    }

    @Override
    public void processTargetFieldMapping(AtlasInternalSession session) throws AtlasException {
        Field sourceField = session.head().getSourceField();
        Field targetField = session.head().getTargetField();
        // Attempt to Auto-detect field type based on input value
        if (targetField.getFieldType() == null && sourceField.getValue() != null) {
            targetField.setFieldType(getConversionService().fieldTypeFromClass(sourceField.getValue().getClass()));
        }

        Object outputValue = null;

        // Do auto-conversion
        if (sourceField.getFieldType() != null && sourceField.getFieldType().equals(targetField.getFieldType())) {
            outputValue = sourceField.getValue();
        } else if (sourceField.getValue() != null) {
            try {
                outputValue = getConversionService().convertType(sourceField.getValue(), sourceField.getFieldType(),
                        targetField.getFieldType());
            } catch (AtlasConversionException e) {
                AtlasUtil.addAudit(session, targetField.getDocId(),
                        String.format("Unable to auto-convert for sT=%s tT=%s tF=%s msg=%s",
                                sourceField.getFieldType(), targetField.getFieldType(), targetField.getPath(),
                                e.getMessage()), targetField.getPath(), AuditStatus.ERROR, null);
                return;
            }
        }
        targetField.setValue(outputValue);

        LookupTable lookupTable = session.head().getLookupTable();
        if (lookupTable != null) {
            processLookupField(session, lookupTable, targetField.getValue(), targetField);
        }
        if (isAutomaticallyProcessOutputFieldActions() && targetField.getActions() != null && targetField.getActions().getActions() != null) {
            getFieldActionService().processActions(targetField.getActions(), targetField);
        }

        XmlFieldWriter writer = session.getFieldWriter(getDocId(), XmlFieldWriter.class);
        writer.write(session);

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processTargetFieldMapping completed: SourceField:[docId={}, path={}, type={}, value={}], TargetField:[docId={}, path={}, type={}, value={}]",
                    getDocId(), sourceField.getDocId(), sourceField.getPath(), sourceField.getFieldType(), sourceField.getValue(),
                    targetField.getDocId(), targetField.getPath(), targetField.getFieldType(), targetField.getValue());
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
            AtlasUtil.addAudit(session, getDocId(),
                    String.format("No target document created for DataSource:[id=%s, uri=%s]",
                            getDocId(), this.getUri()), null, AuditStatus.WARN, null);
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

    private Document getDocument(String data, boolean namespaced)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(namespaced); // this must be done to use namespaces
        DocumentBuilder b = dbf.newDocumentBuilder();
        return b.parse(new ByteArrayInputStream(data.getBytes("UTF-8")));
    }

    @Override
    public int getCollectionSize(AtlasInternalSession session, Field field) throws AtlasException {
        // TODO could this use FieldReader?
        try {
            Object sourceObject = session.getSourceDocument(getDocId());
            Document document = getDocument((String) sourceObject, false);
            Element parentNode = document.getDocumentElement();
            for (SegmentContext sc : new XmlPath(field.getPath()).getSegmentContexts(false)) {
                if (sc.getPrev() == null) {
                    // processing root node part of path such as the "XOA" part of
                    // "/XOA/contact<>/firstName", skip.
                    continue;
                }
                String childrenElementName = XmlPath.cleanPathSegment(sc.getSegment());
                String namespaceAlias = XmlPath.getNamespace(sc.getSegment());
                if (namespaceAlias != null && !"".equals(namespaceAlias)) {
                    childrenElementName = namespaceAlias + ":" + childrenElementName;
                }
                List<Element> children = XmlIOHelper.getChildrenWithName(childrenElementName, parentNode);
                if (children == null || children.isEmpty()) {
                    return 0;
                }
                if (XmlPath.isCollectionSegment(sc.getSegment())) {
                    return children.size();
                }
                parentNode = children.get(0);
            }
            return 0;
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

    @Override
    public Field cloneField(Field field) throws AtlasException {
        return AtlasXmlModelFactory.cloneField(field);
    }
}
