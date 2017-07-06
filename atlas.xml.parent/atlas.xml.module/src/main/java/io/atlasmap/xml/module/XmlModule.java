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

import io.atlasmap.api.AtlasConversionService;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasNotFoundException;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.api.AtlasUnsupportedException;
import io.atlasmap.api.AtlasValidationException;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.core.BaseAtlasModule;
import io.atlasmap.core.DefaultAtlasMappingValidator;
import io.atlasmap.spi.AtlasModule;
import io.atlasmap.spi.AtlasModuleDetail;
import io.atlasmap.spi.AtlasModuleMode;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.Audit;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.BaseMapping;
import io.atlasmap.v2.Collection;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.Mappings;
import io.atlasmap.v2.Validations;
import io.atlasmap.xml.v2.DocumentXmlFieldReader;
import io.atlasmap.xml.v2.XmlField;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
    public void processPreExecution(AtlasSession arg0) throws AtlasException {
        if(logger.isDebugEnabled()) {
            logger.debug("processPreExcution completed");
        }
    }

    @Override
    public void processPreValidation(AtlasSession atlasSession) throws AtlasException {
        
        if(atlasSession == null || atlasSession.getMapping() == null) {
            logger.error("Invalid session: Session and AtlasMapping must be specified");
            throw new AtlasValidationException("Invalid session");
        }
        
        DefaultAtlasMappingValidator defaultValidator = new DefaultAtlasMappingValidator(atlasSession.getMapping());
        Validations validations = defaultValidator.validateAtlasMappingFile();
        
        if(logger.isDebugEnabled()) {
            logger.debug("Detected " + validations.getValidation().size() + " core validation notices");
        }
        
        XmlMappingValidator xmlValidator = new XmlMappingValidator(atlasSession.getMapping(), validations);
        validations = xmlValidator.validateAtlasMappingFile();
        
        if(logger.isDebugEnabled()) {
            logger.debug("Detected " + validations.getValidation().size() + " xml validation notices");
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
        
        if(!(field instanceof XmlField)) {
            Audit audit = new Audit();
            audit.setDocId(field.getDocId());
            audit.setPath(field.getPath());
            audit.setStatus(AuditStatus.ERROR);
            audit.setMessage(String.format("Unsupported input field type=%s", field.getClass().getName()));
            session.getAudits().getAudit().add(audit);
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

        if(inputField.getFieldType() == null) {
            inputField.setFieldType(FieldType.STRING);
        }
        dxfr.read(document, inputField);
        
        if(logger.isDebugEnabled()) {
            logger.debug("Processed p=" + inputField.getPath() + " t=" + inputField.getFieldType().value() + " v=" + inputField.getValue());
        }
    }
    
    @Override
    public void processInputCollection(AtlasSession session, Collection mapping) throws AtlasException {

    }
        
    @Override
    public void processOutputMapping(AtlasSession session, Mapping mapping) throws AtlasException {

    }
    
    @Override
    public void processOutputCollection(AtlasSession session, Collection mapping) throws AtlasException {

    }

    @Override
    public void processPostExecution(AtlasSession arg0) throws AtlasException {
        if(logger.isDebugEnabled()) {
            logger.debug("processPostExecution completed");
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
        return true;
    }

    @Override
    public Boolean isStatisticsEnabled() {
        return null;
    }

    @Override
    public Boolean isSupportedField(Field field) {
        if (field instanceof XmlField) {
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
    
    private Document getDocument(String data, boolean namespaced) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(namespaced); //this must be done to use namespaces
        DocumentBuilder b = dbf.newDocumentBuilder();
        return b.parse(new ByteArrayInputStream(data.getBytes("UTF-8")));
    }
}
