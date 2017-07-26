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

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasConversionService;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.api.AtlasValidationException;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.core.BaseAtlasModule;
import io.atlasmap.spi.AtlasModuleDetail;
import io.atlasmap.spi.AtlasModuleMode;
import io.atlasmap.v2.Audit;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.Collection;
import io.atlasmap.v2.ConstantField;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.PropertyField;
import io.atlasmap.v2.Validation;
import io.atlasmap.xml.v2.DocumentXmlFieldReader;
import io.atlasmap.xml.v2.DocumentXmlFieldWriter;
import io.atlasmap.xml.v2.XmlDataSource;
import io.atlasmap.xml.v2.XmlField;
import io.atlasmap.xml.v2.XmlNamespace;
import io.atlasmap.xml.v2.XmlNamespaces;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
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
import org.xml.sax.SAXException;

@AtlasModuleDetail(name = "XmlModule", uri = "atlas:xml", modes = { "SOURCE", "TARGET" }, dataFormats = { "xml" }, configPackages = { "io.atlasmap.xml.v2" })
public class XmlModule extends BaseAtlasModule {
    private static final Logger logger = LoggerFactory.getLogger(XmlModule.class);
    private AtlasConversionService atlasConversionService = null;
    private AtlasModuleMode atlasModuleMode = AtlasModuleMode.UNSET;
    
    @Override
    public void init() {
        // TODO Auto-generated method stub
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    @Override
    public void processPreInputExecution(AtlasSession session) throws AtlasException {
        if(logger.isDebugEnabled()) {
            logger.debug("processPreInputExcution completed");
        }
    }
    
    @Override
    public void processPreOutputExecution(AtlasSession session) throws AtlasException {
        
        XmlNamespaces xmlNs = null;
        String template = null;
        for(DataSource ds : session.getMapping().getDataSource()) {
            if(DataSourceType.TARGET.equals(ds.getDataSourceType()) && ds instanceof XmlDataSource) {
                xmlNs = ((XmlDataSource)ds).getXmlNamespaces();
                template = ((XmlDataSource)ds).getTemplate();
            }
        }
        
        Map<String, String> nsMap = new HashMap<String, String>();
        if(xmlNs != null && xmlNs.getXmlNamespace() != null && !xmlNs.getXmlNamespace().isEmpty()) {
            for(XmlNamespace ns : xmlNs.getXmlNamespace()) {
                nsMap.put(ns.getAlias(), ns.getUri());
            }
        }
        
        DocumentXmlFieldWriter writer = new DocumentXmlFieldWriter(nsMap, template);
        session.setOutput(writer);
        
        if(logger.isDebugEnabled()) {
            logger.debug("processPreOutputExcution completed");
        }
    }

    @Override
    public void processPreValidation(AtlasSession atlasSession) throws AtlasException {
        
        if(atlasSession == null || atlasSession.getMapping() == null) {
            logger.error("Invalid session: Session and AtlasMapping must be specified");
            throw new AtlasValidationException("Invalid session");
        }
        
        XmlValidationService xmlValidationService = new XmlValidationService(getConversionService());
        List<Validation> xmlValidations = xmlValidationService.validateMapping(atlasSession.getMapping());
        atlasSession.getValidations().getValidation().addAll(xmlValidations);
        
        if(logger.isDebugEnabled()) {
            logger.debug("Detected " + xmlValidations.size() + " xml validation notices");
        }

        if(logger.isDebugEnabled()) {
            logger.debug("processPreValidation completed");
        }
    }
    
    @Override
    public void processInputMapping(AtlasSession session, Mapping mapping) throws AtlasException {
        
        if(mapping.getInputField() == null || mapping.getInputField().isEmpty() || mapping.getInputField().size() != 1) {
            Audit audit = new Audit();
            audit.setStatus(AuditStatus.WARN);
            audit.setMessage(String.format("Mapping does not contain exactly one input field alias=%s desc=%s", mapping.getAlias(), mapping.getDescription()));
            session.getAudits().getAudit().add(audit);
            return;
        }
        
        Field field = mapping.getInputField().get(0);
        
        if(!isSupportedField(field)) {
            Audit audit = new Audit();
            audit.setDocId(field.getDocId());
            audit.setPath(field.getPath());
            audit.setStatus(AuditStatus.ERROR);
            audit.setMessage(String.format("Unsupported input field type=%s", field.getClass().getName()));
            session.getAudits().getAudit().add(audit);
            return;
        }
        
        if(field instanceof PropertyField) {
            processPropertyField(session, mapping, session.getAtlasContext().getContextFactory().getPropertyStrategy());
            if(logger.isDebugEnabled()) {
                logger.debug("Processed input propertyField sPath=" + field.getPath() + " sV=" + field.getValue() + " sT=" + field.getFieldType() + " docId: " + field.getDocId());
            }
            return;
        }
        
        XmlField inputField = (XmlField)field;
        
        Object sourceObject = null;
        if(field.getDocId() != null) {
            sourceObject = session.getInput(field.getDocId());
        } else {
            sourceObject = session.getInput();
        }
        
        if(session.getInput() == null || !(session.getInput() instanceof String)) {
            Audit audit = new Audit();
            audit.setDocId(field.getDocId());
            audit.setPath(field.getPath());
            audit.setStatus(AuditStatus.ERROR);
            audit.setMessage(String.format("Unsupported input object type=%s", field.getClass().getName()));
            session.getAudits().getAudit().add(audit);
            return;
        }
        
        Document document = null;
        
        Map<String,String> sourceUriParams = AtlasUtil.getUriParameters(session.getMapping().getDataSource().get(0).getUri());
                
        boolean enableNamespaces = true;
        for(String key : sourceUriParams.keySet()) {
            if("disableNamespaces".equals(key)) {
                if("true".equals(sourceUriParams.get("disableNamespaces"))) {
                    if(logger.isDebugEnabled()) {
                        logger.debug("Disabling namespace support");
                    }
                    enableNamespaces = false;
                }
            }
        }
        
        try {
            document = getDocument((String)sourceObject, enableNamespaces);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            logger.error(String.format("Error parsing xml input object msg=%s", e.getMessage()), e);
            Audit audit = new Audit();
            audit.setDocId(field.getDocId());
            audit.setPath(field.getPath());
            audit.setStatus(AuditStatus.ERROR);
            audit.setMessage(String.format("Error parsing xml input object msg=%s", field.getClass().getName()));
            session.getAudits().getAudit().add(audit);
            return;
        }
        
        DocumentXmlFieldReader dxfr = new DocumentXmlFieldReader();
        dxfr.read(document, inputField);
        
        if(inputField.getFieldType() == null) {
            inputField.setFieldType(FieldType.STRING);
        }
        
        if(logger.isDebugEnabled()) {
            logger.debug("Processed input field sPath=" + field.getPath() + " sV=" + field.getValue() + " sT=" + field.getFieldType() + " docId: " + field.getDocId());
        }   
    }
    
    @Override
    public void processInputCollection(AtlasSession session, Collection mapping) throws AtlasException {

    }
        
    @Override
    public void processOutputMapping(AtlasSession session, Mapping mapping) throws AtlasException {        
        switch(mapping.getMappingType()) {
        case MAP: processMapOutputMapping(session, mapping); break;
        case COMBINE: break;
        case SEPARATE: break;
        default: logger.warn(String.format("Unsupported mapping type=%s", mapping.getMappingType())); return;
        }  
    }
    
    protected void processMapOutputMapping(AtlasSession session, Mapping mapping) throws AtlasException {
        Field inField = mapping.getInputField().get(0);
        Field outField = mapping.getOutputField().get(0);
        if(!(outField instanceof XmlField)) {
            logger.error(String.format("Unsupported field type %s", outField.getClass().getName()));
            return;
        }
        
        if(inField.getValue() == null) {
            return;
        }
        
        XmlField outputField = (XmlField)outField;
        Object outputValue = null;
        
        if(outputField.getFieldType() == null) {
            outputField.setFieldType(getConversionService().fieldTypeFromClass(inField.getValue().getClass()));
        }
        
        if(inField.getFieldType() != null && inField.getFieldType().equals(outputField.getFieldType())) {
            outputValue = inField.getValue();
        } else {
            try {
                outputValue = getConversionService().convertType(inField.getValue(), inField.getFieldType(), outputField.getFieldType());
            } catch (AtlasConversionException e) {
                logger.error(String.format("Unable to auto-convert for iT=%s oT=%s oF=%s msg=%s", inField.getFieldType(),  outputField.getFieldType(), outputField.getPath(), e.getMessage()), e);
                return;
            }
        }
        
        outputField.setValue(outputValue);        
        
        if(session.getOutput() != null && session.getOutput() instanceof DocumentXmlFieldWriter) {
            DocumentXmlFieldWriter writer = (DocumentXmlFieldWriter) session.getOutput();
            writer.write(outputField);
        } else {
            //TODO: add error handler to detect if the output writer isn't there or is wrong class instance
        }        
        
        if(logger.isDebugEnabled()) {
            logger.debug(String.format("Processed output field oP=%s oV=%s oT=%s docId: %s", outputField.getPath(), outputField.getValue(), outputField.getFieldType(), outputField.getDocId()));
        }
    }
    
    @Override
    public void processOutputCollection(AtlasSession session, Collection mapping) throws AtlasException {

    }

    @Override
    public void processPostInputExecution(AtlasSession session) throws AtlasException {
        if(logger.isDebugEnabled()) {
            logger.debug("processPostInputExecution completed");
        }
    }
    
    @Override
    public void processPostOutputExecution(AtlasSession session) throws AtlasException {
        if(logger.isDebugEnabled()) {
            logger.debug("processPostOutputExecution completed");
        }
        
        Object output = session.getOutput();
        if(output != null) {
            if(output instanceof DocumentXmlFieldWriter) {
                session.setOutput(convertDocumentToString(((DocumentXmlFieldWriter)output).getDocument()));
            }
        }
    }

    @Override
    public void processPostValidation(AtlasSession arg0) throws AtlasException {
        if(logger.isDebugEnabled()) {
            logger.debug("processPostValidation completed");
        }
    }
    
    @Override
    public AtlasModuleMode getMode() {
        return this.atlasModuleMode;
    }

    @Override
    public void setMode(AtlasModuleMode atlasModuleMode) {
        this.atlasModuleMode = atlasModuleMode;
    }

    @Override
    public List<AtlasModuleMode> listSupportedModes() {
        return Arrays.asList(AtlasModuleMode.SOURCE, AtlasModuleMode.TARGET);
    }

    @Override
    public Boolean isStatisticsSupported() {
        return false;
    }

    @Override
    public Boolean isStatisticsEnabled() {
        return false;
    }

    @Override
    public Boolean isSupportedField(Field field) {
        if (field instanceof XmlField) {
            return true;
        } else if (field instanceof PropertyField) {
            return true;
        } else if (field instanceof ConstantField) {
            return true;
        }
        return false;
    }

    @Override
    public AtlasConversionService getConversionService() {
        return this.atlasConversionService;
    }

    @Override
    public void setConversionService(AtlasConversionService atlasConversionService) {
        this.atlasConversionService = atlasConversionService;
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
        } catch (TransformerException e ) {
            logger.error(String.format("Error converting Xml document to string msg=%s", e.getMessage()), e);
            throw new AtlasException(e.getMessage(), e);
        }
    }
    
    private Document getDocument(String data, boolean namespaced) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(namespaced); //this must be done to use namespaces
        DocumentBuilder b = dbf.newDocumentBuilder();
        return b.parse(new ByteArrayInputStream(data.getBytes("UTF-8")));
    }
}
