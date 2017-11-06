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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasConversionService;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasFieldActionService;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.spi.AtlasModule;
import io.atlasmap.spi.AtlasModuleMode;
import io.atlasmap.spi.AtlasPropertyStrategy;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.Audit;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.BaseMapping;
import io.atlasmap.v2.Collection;
import io.atlasmap.v2.ConstantField;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.LookupEntry;
import io.atlasmap.v2.LookupTable;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import io.atlasmap.v2.PropertyField;
import io.atlasmap.v2.SimpleField;

public abstract class BaseAtlasModule implements AtlasModule {
    private static final Logger LOG = LoggerFactory.getLogger(BaseAtlasModule.class);

    private AtlasConversionService atlasConversionService = null;
    private AtlasModuleMode atlasModuleMode = AtlasModuleMode.UNSET;
    protected boolean automaticallyProcessOutputFieldActions = true;

    @Override
    public void init() {
        // no-op now
    }

    @Override
    public void destroy() {
        // no-op now
    }

    @Override
    public void processInputActions(AtlasSession atlasSession, BaseMapping baseMapping) throws AtlasException {
        if (baseMapping.getMappingType().equals(MappingType.COLLECTION)) {
            return;
        }
        AtlasFieldActionService fieldActionService = atlasSession.getAtlasContext().getContextFactory()
                .getFieldActionService();
        Mapping mapping = (Mapping) baseMapping;
        for (Field field : mapping.getInputField()) {
            processFieldActions(fieldActionService, field);
        }
    }

    @Override
    public void processOutputActions(AtlasSession atlasSession, BaseMapping baseMapping) throws AtlasException {
        if (!automaticallyProcessOutputFieldActions) {
            return;
        }
        if (baseMapping.getMappingType().equals(MappingType.COLLECTION)) {
            return;
        }
        AtlasFieldActionService fieldActionService = atlasSession.getAtlasContext().getContextFactory()
                .getFieldActionService();
        Mapping mapping = (Mapping) baseMapping;
        for (Field field : mapping.getOutputField()) {
            processFieldActions(fieldActionService, field);
        }
    }

    public abstract int getCollectionSize(AtlasSession session, Field field) throws AtlasException;

    public abstract Field cloneField(Field field) throws AtlasException;

    public List<Mapping> generateInputMappings(AtlasSession session, BaseMapping baseMapping) throws AtlasException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Generating Input Mappings from mapping: " + baseMapping);
        }
        if (!baseMapping.getMappingType().equals(MappingType.COLLECTION)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Mapping is not a collection mapping, not cloning: " + baseMapping);
            }
            return Arrays.asList((Mapping) baseMapping);
        }
        List<Mapping> mappings = new LinkedList<>();
        for (BaseMapping m : ((Collection) baseMapping).getMappings().getMapping()) {
            Mapping mapping = (Mapping) m;
            Field inputField = mapping.getInputField().get(0);
            boolean inputIsCollection = PathUtil.isCollection(inputField.getPath());
            if (!inputIsCollection) {
                // this is a input non-collection to output collection, ie: contact.firstName ->
                // contact[].firstName
                // this will be expanded later by generateOutputMappings, for input processing,
                // just copy it over
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Internal mapping's input field is not a collection, not cloning: " + mapping);
                }

                // this is a output collection such as contact<>.firstName, but input is non
                // collection such as contact.firstName
                // so just set the output collection field path to be contact<0>.firstName,
                // which will cause at least one
                // output object to be created for our copied firstName value
                for (Field f : mapping.getOutputField()) {
                    f.setPath(PathUtil.overwriteCollectionIndex(f.getPath(), 0));
                }
                mappings.add(mapping);
                continue;
            }

            int inputCollectionSize = this.getCollectionSize(session, inputField);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Internal mapping's input field is a collection. Cloning it for each item ("
                        + inputCollectionSize + " clones): " + mapping);
            }
            for (int i = 0; i < inputCollectionSize; i++) {
                Mapping cloneMapping = (Mapping) AtlasModelFactory.cloneMapping(mapping, false);
                for (Field f : mapping.getInputField()) {
                    Field clonedField = cloneField(f);
                    clonedField.setPath(PathUtil.overwriteCollectionIndex(clonedField.getPath(), i));
                    cloneMapping.getInputField().add(clonedField);
                }
                for (Field f : mapping.getOutputField()) {
                    Field clonedField = cloneField(f);
                    if (PathUtil.isCollection(clonedField.getPath())) {
                        clonedField.setPath(PathUtil.overwriteCollectionIndex(clonedField.getPath(), i));
                    }
                    cloneMapping.getOutputField().add(clonedField);
                }
                mappings.add(cloneMapping);
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Generated " + mappings.size() + " mappings from mapping: " + baseMapping);
        }
        ((Collection) baseMapping).getMappings().getMapping().clear();
        ((Collection) baseMapping).getMappings().getMapping().addAll(mappings);

        return mappings;
    }

    public List<Mapping> getOutputMappings(AtlasSession session, BaseMapping baseMapping) throws AtlasException {
        if (!baseMapping.getMappingType().equals(MappingType.COLLECTION)) {
            return Arrays.asList((Mapping) baseMapping);
        }
        List<Mapping> mappings = new LinkedList<>();
        for (BaseMapping m : ((Collection) baseMapping).getMappings().getMapping()) {
            mappings.add((Mapping) m);
        }
        return mappings;
    }

    @Override
    public void processPreInputExecution(AtlasSession session) throws AtlasException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("processPreInputExcution completed");
        }
    }

    @Override
    public void processPostInputExecution(AtlasSession session) throws AtlasException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("processPostInputExecution completed");
        }
    }

    @Override
    public void processPostValidation(AtlasSession session) throws AtlasException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("processPostValidation completed");
        }
    }

    protected void processConstantField(AtlasSession atlasSession, Mapping mapping) throws AtlasException {
        for (Field f : mapping.getInputField()) {
            if (f instanceof ConstantField && (f.getFieldType() == null && f.getValue() != null)) {
                f.setFieldType(getConversionService().fieldTypeFromClass(f.getValue().getClass()));
            }
        }
    }

    protected void processPropertyField(AtlasSession atlasSession, Mapping mapping,
            AtlasPropertyStrategy atlasPropertyStrategy) throws AtlasException {
        for (Field f : mapping.getInputField()) {
            if (f instanceof PropertyField) {
                atlasPropertyStrategy.processPropertyField(atlasSession.getMapping(), (PropertyField) f,
                        atlasSession.getProperties());
            }
        }

        for (Field f : mapping.getOutputField()) {
            if (f instanceof PropertyField) {
                atlasPropertyStrategy.processPropertyField(atlasSession.getMapping(), (PropertyField) f,
                        atlasSession.getProperties());
            }
        }
    }

    protected void processLookupField(AtlasSession session, Mapping mapping) throws AtlasException {

        if (mapping == null || mapping.getMappingType() == null || MappingType.LOOKUP.equals(mapping.getMappingType())
                || mapping.getLookupTableName() == null || mapping.getLookupTableName().trim().length() == 0) {
            throw new AtlasException("Lookup mapping must have lookupTableName specified");
        }

        if (session == null || session.getMapping() == null) {
            throw new AtlasException("AtlasSession must be initialized");
        }

        if (session.getMapping().getLookupTables() == null
                || session.getMapping().getLookupTables().getLookupTable() == null
                || session.getMapping().getLookupTables().getLookupTable().size() == 0) {
            addAudit(session, mapping.getOutputField().get(0).getDocId(), String.format(
                    "No lookup table found for specified lookupTableName=%s", mapping.getLookupTableName()),
                    null, AuditStatus.WARN, null);
            return;
        }

        LookupTable currentTable = null;
        for (LookupTable lookupTable : session.getMapping().getLookupTables().getLookupTable()) {
            if (lookupTable.getName() != null && lookupTable.getName().equals(mapping.getLookupTableName())) {
                currentTable = lookupTable;
            }
        }

        if (currentTable.getLookupEntry() == null || currentTable.getLookupEntry().isEmpty()) {
            addAudit(session, mapping.getOutputField().get(0).getDocId(), String.format(
                    "Lookup table lookupTableName=%s does not contain any entries", mapping.getLookupTableName()),
                    null, AuditStatus.WARN, null);
            return;
        }

        for (LookupEntry entry : currentTable.getLookupEntry()) {
            for (Field inputField : mapping.getInputField()) {
                if (entry.getSourceValue().equals(inputField.getValue())) {
                    inputField.setValue(entry.getTargetValue());
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(
                                String.format("Processing lookup value for iP=%s iV=%s lksV=%s lksT=%s lktV=%s lktT=%s",
                                        inputField.getPath(), inputField.getValue(), entry.getSourceValue(),
                                        entry.getSourceType(), entry.getTargetValue(), entry.getTargetType()));
                    }
                }
            }
        }

    }

    protected Field processSeparateField(AtlasSession session, Mapping mapping, Field inputField, Field outputField)
            throws AtlasException {
        if (outputField.getIndex() == null || outputField.getIndex() < 0) {
            addAudit(session, outputField.getDocId(),
                    String.format("Separate requires zero or positive Index value to be set on outputField outputField.path=%s",
                            outputField.getPath()),
                    outputField.getPath(), AuditStatus.WARN, null);
            return null;
        }

        Field inputFieldsep = mapping.getInputField().get(0);
        if ((inputFieldsep.getFieldType() != null && !FieldType.STRING.equals(inputFieldsep.getFieldType())
                || (inputFieldsep.getValue() == null
                        || !inputFieldsep.getValue().getClass().isAssignableFrom(String.class)))) {
            addAudit(session, outputField.getDocId(), String
                    .format("Separate requires String field type for inputField.path=%s", inputFieldsep.getPath()),
                    outputField.getPath(), AuditStatus.WARN, null);
            return null;
        }

        String inputValue = (String) inputFieldsep.getValue();
        List<String> separatedValues = null;
        if (mapping.getDelimiter() != null) {
            separatedValues = session.getAtlasContext().getContextFactory().getSeparateStrategy()
                    .separateValue(inputValue, mapping.getDelimiter());
        } else {
            separatedValues = session.getAtlasContext().getContextFactory().getSeparateStrategy()
                    .separateValue(inputValue);
        }

        if (separatedValues == null || separatedValues.isEmpty()) {
            LOG.debug(
                    String.format("Empty string for Separate mapping inputField.path=%s", inputFieldsep.getPath()));
            return null;
        }

        if (separatedValues.size() <= outputField.getIndex()) {
            String errorMessage = String.format(
                    "Separate returned fewer segments count=%s when outputField.path=%s requested index=%s",
                    separatedValues.size(), outputField.getPath(), outputField.getIndex());
            addAudit(session, outputField.getDocId(), errorMessage, outputField.getPath(), AuditStatus.WARN, null);
            return null;
        }

        SimpleField simpleField = AtlasModelFactory.cloneFieldToSimpleField(inputFieldsep);
        simpleField.setValue(separatedValues.get(outputField.getIndex()));
        return simpleField;
    }

    protected void processCombineField(AtlasSession session, Mapping mapping, List<Field> inputFields,
            Field outputField) throws AtlasException {
        Map<Integer, String> combineValues = null;
        for (Field inputField : inputFields) {
            if (inputField.getIndex() == null || inputField.getIndex() < 0) {
                addAudit(session, outputField.getDocId(),
                        String.format("Combine requires zero or positive Index value to be set on all inputFields inputField.path=%s",
                                inputField.getPath()),
                        outputField.getPath(), AuditStatus.WARN, null);
                continue;
            }

            if (combineValues == null) {
                // We need to support a sorted map w/ null values
                combineValues = new HashMap<Integer, String>();
            }

            if ((inputField.getFieldType() != null) || (inputField.getValue() != null)) {
                String convertedInput;
                try {
                    convertedInput = (String) getConversionService().convertType(inputField.getValue(),
                        inputField.getFieldType(), FieldType.STRING);

                } catch (AtlasConversionException e) {
                    LOG.warn(String.format("Suitable converter for inputField.path=%s hasn't been found",
                        inputField.getPath()));
                    addAudit(session, outputField.getDocId(), String
                            .format("Suitable converter for inputField.path=%s hasn't been found", inputField.getPath()),
                        outputField.getPath(), AuditStatus.WARN, null);

                    convertedInput = inputField.getValue().toString();
                }
                combineValues.put(inputField.getIndex(), convertedInput);
                continue;
            }

        }

        String combinedValue = null;
        if (mapping.getDelimiter() != null) {
            combinedValue = session.getAtlasContext().getContextFactory().getCombineStrategy()
                    .combineValues(combineValues, mapping.getDelimiter());
        } else {
            combinedValue = session.getAtlasContext().getContextFactory().getCombineStrategy()
                    .combineValues(combineValues);
        }

        if (combinedValue == null || combinedValue.trim().isEmpty()) {
            LOG.debug(String.format("Empty combined string for Combine mapping outputField.path=%s",
                    outputField.getPath()));
            return;
        }

        outputField.setValue(combinedValue);
    }

    protected void processLookupField(AtlasSession session, String lookupTableName, String inputValue,
            Field outputField) throws AtlasException {
        LookupTable table = null;
        for (LookupTable t : session.getMapping().getLookupTables().getLookupTable()) {
            if (t.getName().equals(lookupTableName)) {
                table = t;
                break;
            }
        }
        if (table == null) {
            throw new AtlasException("Could not find lookup table with name '" + lookupTableName + "' for outputField: "
                    + outputField.getPath());
        }

        String lookupValue = null;
        FieldType lookupType = null;
        for (LookupEntry lkp : table.getLookupEntry()) {
            if (lkp.getSourceValue().equals(inputValue)) {
                lookupValue = lkp.getTargetValue();
                lookupType = lkp.getTargetType();
                break;
            }
        }

        Object outputValue = null;
        if (lookupType == null || FieldType.STRING.equals(lookupType)) {
            outputValue = lookupValue;
        } else {
            outputValue = getConversionService().convertType(lookupValue, FieldType.STRING, lookupType);
        }

        if (outputField.getFieldType() != null && !outputField.getFieldType().equals(lookupType)) {
            outputValue = getConversionService().convertType(outputValue, lookupType, outputField.getFieldType());
        }

        outputField.setValue(outputValue);
    }

    protected void addAudit(AtlasSession session, String docId, String message, String path, AuditStatus status,
            String value) {
        Audit audit = new Audit();
        audit.setDocId(docId);
        audit.setMessage(message);
        audit.setPath(path);
        audit.setStatus(status);
        audit.setValue(value);
        session.getAudits().getAudit().add(audit);
    }

    protected void processFieldActions(AtlasFieldActionService fieldActionService, Field field) throws AtlasException {
        fieldActionService.processActions(field.getActions(), field);
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
    public Boolean isStatisticsSupported() {
        return false;
    }

    @Override
    public Boolean isStatisticsEnabled() {
        return false;
    }

    @Override
    public List<AtlasModuleMode> listSupportedModes() {
        return Arrays.asList(AtlasModuleMode.SOURCE, AtlasModuleMode.TARGET);
    }

    @Override
    public AtlasConversionService getConversionService() {
        return atlasConversionService;
    }

    @Override
    public void setConversionService(AtlasConversionService atlasConversionService) {
        this.atlasConversionService = atlasConversionService;
    }

}
