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

import java.lang.management.ManagementFactory;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasConstants;
import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasContextFactory;
import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.AtlasMappingService.AtlasMappingFormat;
import io.atlasmap.mxbean.AtlasContextMXBean;
import io.atlasmap.spi.AtlasModule;
import io.atlasmap.spi.AtlasModuleInfo;
import io.atlasmap.spi.AtlasModuleInfoRegistry;
import io.atlasmap.spi.AtlasModuleMode;
import io.atlasmap.spi.FieldDirection;
import io.atlasmap.spi.StringDelimiter;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.Audits;
import io.atlasmap.v2.BaseMapping;
import io.atlasmap.v2.Collection;
import io.atlasmap.v2.ConstantField;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.LookupTable;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import io.atlasmap.v2.PropertyField;
import io.atlasmap.v2.SimpleField;
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.Validations;

public class DefaultAtlasContext implements AtlasContext, AtlasContextMXBean {

    public static final String CONSTANTS_DOCUMENT_ID = "io.atlasmap.core.DefaultAtlasContext.constants.docId";
    public static final String PROPERTIES_DOCUMENT_ID = "io.atlasmap.core.DefaultAtlasContext.properties.docId";

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAtlasContext.class);
    private ObjectName jmxObjectName;
    private final UUID uuid;
    private DefaultAtlasContextFactory factory;
    private AtlasMapping mappingDefinition;
    private AtlasMappingFormat atlasMappingFormat;
    private URI atlasMappingUri;
    private Map<String, AtlasModule> sourceModules = new HashMap<>();
    private Map<String, AtlasModule> targetModules = new HashMap<>();
    private Map<String, LookupTable> lookupTables = new HashMap<>();

    public DefaultAtlasContext(URI atlasMappingUri) {
        this(DefaultAtlasContextFactory.getInstance(), atlasMappingUri, AtlasMappingFormat.XML);
    }

    public DefaultAtlasContext(DefaultAtlasContextFactory factory, URI atlasMappingUri) {
        this(factory, atlasMappingUri, AtlasMappingFormat.XML);
    }

    public DefaultAtlasContext(DefaultAtlasContextFactory factory, URI atlasMappingUri, AtlasMappingFormat format) {
        this.factory = factory;
        this.uuid = UUID.randomUUID();
        this.atlasMappingUri = atlasMappingUri;
        this.atlasMappingFormat = format;
    }

    public DefaultAtlasContext(DefaultAtlasContextFactory factory, AtlasMapping mapping) {
        this.factory = factory;
        this.uuid = UUID.randomUUID();
        this.mappingDefinition = mapping;
    }

    /**
     * TODO: For dynamic re-load. This needs lock()
     *
     * @throws AtlasException failed to initialize
     */
    protected void init() throws AtlasException {

        registerJmx(this);

        if (this.atlasMappingUri != null) {
            this.mappingDefinition = factory.getMappingService().loadMapping(this.atlasMappingUri, atlasMappingFormat);
        }

        sourceModules.clear();
        ConstantModule constant = new ConstantModule();
        constant.setConversionService(factory.getConversionService());
        constant.setFieldActionService(factory.getFieldActionService());
        sourceModules.put(CONSTANTS_DOCUMENT_ID, constant);
        PropertyModule propSource = new PropertyModule(factory.getPropertyStrategy());
        propSource.setMode(AtlasModuleMode.SOURCE);
        propSource.setConversionService(factory.getConversionService());
        propSource.setFieldActionService(factory.getFieldActionService());
        sourceModules.put(PROPERTIES_DOCUMENT_ID, propSource);
        targetModules.clear();

        lookupTables.clear();
        if (mappingDefinition.getLookupTables() != null
                && mappingDefinition.getLookupTables().getLookupTable() != null) {
            for (LookupTable table : mappingDefinition.getLookupTables().getLookupTable()) {
                lookupTables.put(table.getName(), table);
            }
        }

        AtlasModuleInfoRegistry moduleInfoRegistry = factory.getModuleInfoRegistry();
        for (DataSource ds : mappingDefinition.getDataSource()) {
            AtlasModuleInfo moduleInfo = moduleInfoRegistry.lookupByUri(ds.getUri());
            if (moduleInfo == null) {
                LOG.error("Cannot find module info for the DataSource uri '{}'", ds.getUri());
                continue;
            }
            if (ds.getDataSourceType() != DataSourceType.SOURCE && ds.getDataSourceType() != DataSourceType.TARGET) {
                LOG.error("Unsupported DataSource type '{}'", ds.getDataSourceType());
                continue;
            }

            String docId = ds.getId();
            if (docId == null || docId.isEmpty()) {
                docId = ds.getDataSourceType() == DataSourceType.SOURCE ? AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID
                        : AtlasConstants.DEFAULT_TARGET_DOCUMENT_ID;
            }
            if (ds.getDataSourceType() == DataSourceType.SOURCE && sourceModules.containsKey(docId)) {
                LOG.error("Duplicated {} DataSource ID '{}' was detected, ignoring...", ds.getDataSourceType(),
                        ds.getId());
                continue;
            }
            if (ds.getDataSourceType() == DataSourceType.TARGET && targetModules.containsKey(docId)) {
                LOG.error("Duplicated {} DataSource ID '{}' was detected, ignoring...", ds.getDataSourceType(), docId);
                continue;
            }

            try {
                AtlasModule module = moduleInfo.getModuleClass().newInstance();
                module.setConversionService(factory.getConversionService());
                module.setFieldActionService(factory.getFieldActionService());
                module.setUri(ds.getUri());
                if (ds.getDataSourceType() == DataSourceType.SOURCE) {
                    module.setMode(AtlasModuleMode.SOURCE);
                    getSourceModules().put(docId, module);
                } else if (ds.getDataSourceType() == DataSourceType.TARGET) {
                    module.setMode(AtlasModuleMode.TARGET);
                    getTargetModules().put(docId, module);
                }
                module.setDocId(docId);
                module.init();
            } catch (Throwable t) {
                LOG.error("Unable to initialize {} module: {}", ds.getDataSourceType(), moduleInfo.toString());
                LOG.error(t.getMessage(), t);
                throw new AtlasException(String.format("Unable to initialize %s module: %s", ds.getDataSourceType(),
                        moduleInfo.toString()), t);
            }
        }
    }

    protected void registerJmx(DefaultAtlasContext context) {
        try {
            setJmxObjectName(new ObjectName(
                    getDefaultAtlasContextFactory().getJmxObjectName() + ",context=Contexts,uuid=" + uuid.toString()));
            ManagementFactory.getPlatformMBeanServer().registerMBean(this, getJmxObjectName());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Registered AtlasContext {} with JMX", context.getUuid());
            }
        } catch (Throwable t) {
            LOG.warn("Failed to register AtlasContext {} with JMX", context.getUuid());
            LOG.warn(t.getMessage(), t);
        }
    }

    /**
     * Process single mapping entry in preview mode. Since modules don't participate
     * in preview mode, any document format specific function won't be applied.
     *
     * @param mapping A @link{Mapping} entry to process
     */
    @Override
    public Audits processPreview(Mapping mapping) {
        DefaultAtlasSession session = new DefaultAtlasSession(null);
        MappingType mappingType = mapping.getMappingType();
        List<Field> sourceFields = mapping.getInputField();
        List<Field> targetFields = mapping.getOutputField();

        targetFields.forEach(tf -> tf.setValue(null));
        if (sourceFields.isEmpty() || targetFields.isEmpty()) {
            return session.getAudits();
        }
        for (Field sf : sourceFields) {
            if (sf.getFieldType() == null || sf.getValue() == null) {
                continue;
            }
            if (sf.getValue() instanceof String && ((String)sf.getValue()).isEmpty()) {
                continue;
            }
            if (!restoreSourceFieldType(session, sf)) {
                return session.getAudits();
            }
        }

        Field sourceField;
        Field targetField;
        switch (mappingType) {

        case MAP:
            sourceField = sourceFields.get(0);
            targetField = targetFields.get(0);
            applyFieldActions(session, sourceField);
            if (!convertSourceToTarget(session, sourceField, targetField)) {
                break;
            }
            applyFieldActions(session, targetField);
            break;

        case COMBINE:
            targetField = targetFields.get(0);
            sourceFields.forEach(sf -> applyFieldActions(session, sf));
            Field combined;
            try {
                combined = processCombineField(session, mapping, sourceFields, targetField);
            } catch (AtlasException e) {
                AtlasUtil.addAudit(session, sourceFields.get(0).getDocId(), String.format(
                        "Failed to combine fields: %s", AtlasUtil.getChainedMessage(e)),
                        sourceFields.get(0).getPath(), AuditStatus.ERROR, null);
                if (LOG.isDebugEnabled()) {
                    LOG.error("", e);
                }
                break;
            }
            if (!convertSourceToTarget(session, combined, targetField)) {
                break;
            }
            applyFieldActions(session, targetField);
            break;

        case SEPARATE:
            sourceField = sourceFields.get(0);
            applyFieldActions(session, sourceField);
            List<Field> separatedFields;
            try {
                separatedFields = processSeparateField(session, mapping, sourceField);
            } catch (AtlasException e) {
                AtlasUtil.addAudit(session, sourceField.getDocId(), String.format(
                        "Failed to separate field: %s", AtlasUtil.getChainedMessage(e)),
                        sourceField.getPath(), AuditStatus.ERROR, null);
                if (LOG.isDebugEnabled()) {
                    LOG.error("", e);
                }
                break;
            }
            if (separatedFields == null) {
                break;
            }
            for (Field f : targetFields) {
                targetField = f;
                if (targetField.getIndex() == null || targetField.getIndex() < 0) {
                    AtlasUtil.addAudit(session, targetField.getDocId(), String.format(
                            "Separate requires zero or positive Index value to be set on targetField targetField.path=%s",
                            targetField.getPath()), targetField.getPath(), AuditStatus.WARN, null);
                    continue;
                }
                if (separatedFields.size() <= targetField.getIndex()) {
                    String errorMessage = String.format(
                            "Separate returned fewer segments count=%s when targetField.path=%s requested index=%s",
                            separatedFields.size(), targetField.getPath(), targetField.getIndex());
                    AtlasUtil.addAudit(session, targetField.getDocId(), errorMessage, targetField.getPath(),
                            AuditStatus.WARN, null);
                    break;
                }
                if (!convertSourceToTarget(session, separatedFields.get(targetField.getIndex()), targetField)) {
                    break;
                }
                applyFieldActions(session, targetField);
            }
            break;

        default:
            AtlasUtil.addAudit(session, null, String.format(
                    "Unsupported mappingType=%s detected", mapping.getMappingType()),
                    null, AuditStatus.ERROR, null);
        }
        return session.getAudits();
    }

    private boolean restoreSourceFieldType(DefaultAtlasSession session, Field sourceField) {
        try {
            Object sourceValue = factory.getConversionService().convertType(
                    sourceField.getValue(), null, sourceField.getFieldType(), null);
            sourceField.setValue(sourceValue);
        } catch (AtlasConversionException e) {
            AtlasUtil.addAudit(session, sourceField.getDocId(), String.format(
                    "Wrong format for source value : %s", AtlasUtil.getChainedMessage(e)),
                    sourceField.getPath(), AuditStatus.ERROR, null);
            if (LOG.isDebugEnabled()) {
                LOG.error("", e);
            }
            return false;
        }
        return true;
    }

    private boolean convertSourceToTarget(DefaultAtlasSession session, Field sourceField, Field targetField) {
        Object targetValue = null;
        if (sourceField.getFieldType() != null && sourceField.getFieldType().equals(targetField.getFieldType())) {
            targetValue = sourceField.getValue();
        } else if (sourceField.getValue() != null) {
            try {
                targetValue = factory.getConversionService().convertType(sourceField.getValue(), sourceField.getFormat(),
                        targetField.getFieldType(), targetField.getFormat());
            } catch (AtlasConversionException e) {
                AtlasUtil.addAudit(session, targetField.getDocId(), String.format(
                        "Failed to convert source value to target type: %s", AtlasUtil.getChainedMessage(e)),
                        targetField.getPath(), AuditStatus.ERROR, null);
                if (LOG.isDebugEnabled()) {
                    LOG.error("", e);
                }
                return false;
            }
        }

        targetField.setValue(targetValue);
        return true;
    }

    private boolean applyFieldActions(DefaultAtlasSession session, Field field) {
        if (field.getActions() == null) {
            return true;
        }
        try {
            factory.getFieldActionService().processActions(field.getActions(), field);
        } catch (AtlasException e) {
            AtlasUtil.addAudit(session, field.getDocId(), String.format(
                    "Failed to apply field action: %s", AtlasUtil.getChainedMessage(e)),
                    field.getPath(), AuditStatus.ERROR, null);
            if (LOG.isDebugEnabled()) {
                LOG.error("", e);
            }
            return false;
        }
        return true;
    }

    /**
     * Process session lifecycle
     *
     */
    @Override
    public void process(AtlasSession userSession) throws AtlasException {
        if (!(userSession instanceof DefaultAtlasSession)) {
            throw new AtlasException(String.format("Unsupported session class '%s'", userSession.getClass().getName()));
        }
        if (!this.equals(userSession.getAtlasContext())) {
            throw new AtlasException("Cannot execute AtlasSession created by the other AtlasContext");
        }

        DefaultAtlasSession session = (DefaultAtlasSession) userSession;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Begin process {}", (session == null ? null : session.toString()));
        }

        session.head().unset();
        session.getAudits().getAudit().clear();
        session.getValidations().getValidation().clear();

        processValidation(session);
        for (Validation v : session.getValidations().getValidation()) {
            AtlasUtil.addAudit(session, v);
        }
        if (session.hasErrors()) {
            if (LOG.isDebugEnabled()) {
                LOG.error("Aborting due to {} errors in pre-validation", session.errorCount());
            }
            return;
        }

        for (AtlasModule module : getSourceModules().values()) {
            module.processPreSourceExecution(session);
        }
        for (AtlasModule module : getTargetModules().values()) {
            module.processPreTargetExecution(session);
        }

        if (session.hasErrors()) {
            if (LOG.isDebugEnabled()) {
                LOG.error("Aborting due to {} errors in pre-execution", session.errorCount());
            }
            return;
        }

        for (BaseMapping baseMapping : session.getMapping().getMappings().getMapping()) {
            for (Mapping mapping : extractCollectionMappings(session, baseMapping)) {
                session.head().setMapping(mapping).setLookupTable(lookupTables.get(mapping.getLookupTableName()));

                if (mapping.getOutputField() == null || mapping.getOutputField().isEmpty()) {
                    AtlasUtil.addAudit(session, null,
                            String.format("Mapping does not contain at least one output field: alias=%s desc=%s",
                                    mapping.getAlias(), mapping.getDescription()),
                            null, AuditStatus.WARN, null);
                    continue;
                }

                if (mapping.getInputField() == null || mapping.getInputField().isEmpty()) {
                    session.head().addAudit(AuditStatus.WARN, null, null, String.format(
                            "Mapping does not contain at least one source field: alias=%s desc=%s",
                            mapping.getAlias(), mapping.getDescription()));
                } else {
                    try {
                        processSourceFieldMappings(session, mapping.getInputField());
                    }catch (Throwable t) {
                        Field sourceField = session.head().getSourceField();
                        String docId = sourceField != null ? sourceField.getDocId() : null;
                        String path =  sourceField != null ? sourceField.getPath() : null;
                        session.head().addAudit(AuditStatus.ERROR, docId, path, String.format(
                                "Unexpected exception is thrown while reading source field: %s", t.getMessage()));
                        if (LOG.isDebugEnabled()) {
                            LOG.error("", t);
                        }
                    }
                }

                if (!session.head().hasError()) {
                    try {
                        processTargetFieldMappings(session, mapping);
                    } catch (Throwable t) {
                        Field targetField = session.head().getTargetField();
                        String docId = targetField != null ? targetField.getDocId() : null;
                        String path = targetField != null ? targetField.getPath() : null;
                        session.head().addAudit(AuditStatus.ERROR, docId, path, String.format(
                                "Unexpected exception is thrown while populating target field: %s", t.getMessage()));
                        if (LOG.isDebugEnabled()) {
                            LOG.error("", t);
                        }
                    }
                }
                session.getAudits().getAudit().addAll(session.head().getAudits());
                session.head().getAudits().clear();
            }
        }

        for (AtlasModule module : getSourceModules().values()) {
            module.processPostValidation(session);
        }
        for (AtlasModule module : getTargetModules().values()) {
            module.processPostValidation(session);
        }

        for (AtlasModule module : getSourceModules().values()) {
            module.processPostSourceExecution(session);
        }
        for (AtlasModule module : getTargetModules().values()) {
            module.processPostTargetExecution(session);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("End process {}", session == null ? null : session.toString());
        }
    }

    protected final List<Mapping> extractCollectionMappings(DefaultAtlasSession session, BaseMapping baseMapping)
            throws AtlasException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Generating Source Mappings from mapping: {}", baseMapping);
        }
        if (!baseMapping.getMappingType().equals(MappingType.COLLECTION)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Mapping is not a collection mapping, not cloning: {}", baseMapping);
            }
            return Arrays.asList((Mapping) baseMapping);
        }
        List<Mapping> mappings = new LinkedList<>();
        for (BaseMapping m : ((Collection) baseMapping).getMappings().getMapping()) {
            Mapping mapping = (Mapping) m;
            Field sourceField = mapping.getInputField().get(0);
            boolean sourceIsCollection = AtlasPath.isCollection(sourceField.getPath());
            if (!sourceIsCollection) {
                // this is a input non-collection to output collection, ie: contact.firstName ->
                // contact[].firstName
                // this will be expanded later by generateOutputMappings, for input processing,
                // just copy it over
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Internal mapping's source field is not a collection, not cloning: {}", mapping);
                }

                // this is a output collection such as contact<>.firstName, but input is non
                // collection such as contact.firstName
                // so just set the output collection field path to be contact<0>.firstName,
                // which will cause at least one
                // output object to be created for our copied firstName value
                for (Field f : mapping.getOutputField()) {
                    f.setPath(AtlasPath.overwriteCollectionIndex(f.getPath(), 0));
                }
                mappings.add(mapping);
                continue;
            }

            AtlasModule module = resolveModule(FieldDirection.SOURCE, sourceField);
            int sourceCollectionSize = module.getCollectionSize(session, sourceField);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Internal mapping's source field is a collection. Cloning it for each item ({} clones): {}",
                        sourceCollectionSize, mapping);
            }
            for (int i = 0; i < sourceCollectionSize; i++) {
                Mapping cloneMapping = (Mapping) AtlasModelFactory.cloneMapping(mapping, false);
                for (Field f : mapping.getInputField()) {
                    Field clonedField = module.cloneField(f);
                    clonedField.setPath(AtlasPath.overwriteCollectionIndex(clonedField.getPath(), i));
                    cloneMapping.getInputField().add(clonedField);
                }
                for (Field f : mapping.getOutputField()) {
                    AtlasModule targetModule = resolveModule(FieldDirection.TARGET, f);
                    Field clonedField = targetModule.cloneField(f);
                    if (AtlasPath.isCollection(clonedField.getPath())) {
                        clonedField.setPath(AtlasPath.overwriteCollectionIndex(clonedField.getPath(), i));
                    }
                    cloneMapping.getOutputField().add(clonedField);
                }
                mappings.add(cloneMapping);
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Generated {} mappings from mapping: {}", mappings.size(), baseMapping);
        }

        return mappings;
    }

    private AtlasModule resolveModule(FieldDirection direction, Field field) {
        if (direction == FieldDirection.SOURCE && field instanceof ConstantField) {
            return sourceModules.get(CONSTANTS_DOCUMENT_ID);
        }
        if (direction == FieldDirection.SOURCE && field instanceof PropertyField) {
            return sourceModules.get(PROPERTIES_DOCUMENT_ID);
        }

        String docId = field.getDocId();
        if (docId == null || docId.isEmpty()) {
            docId = direction == FieldDirection.SOURCE ? AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID
                    : AtlasConstants.DEFAULT_TARGET_DOCUMENT_ID;
        }
        return direction == FieldDirection.SOURCE ? sourceModules.get(docId) : targetModules.get(docId);
    }

    private void processSourceFieldMappings(DefaultAtlasSession session, List<Field> sourceFields)
            throws AtlasException {
        for (Field sourceField : sourceFields) {
            session.head().setSourceField(sourceField);
            AtlasModule module = resolveModule(FieldDirection.SOURCE, sourceField);
            if (module == null) {
                AtlasUtil.addAudit(session, sourceField.getDocId(),
                        String.format("Module not found for docId '%s'", sourceField.getDocId()), sourceField.getPath(),
                        AuditStatus.ERROR, null);
                return;
            }
            if (!module.isSupportedField(sourceField)) {
                AtlasUtil.addAudit(session, sourceField.getDocId(),
                        String.format("Unsupported source field type '%s' for DataSource '%s'",
                                sourceField.getClass().getName(), module.getUri()),
                        sourceField.getPath(), AuditStatus.ERROR, null);
                return;
            }

            module.processSourceFieldMapping(session);
        }
    }

    private void processTargetFieldMappings(DefaultAtlasSession session, Mapping mapping) throws AtlasException {
        MappingType mappingType = mapping.getMappingType();
        List<Field> sourceFields = mapping.getInputField();
        List<Field> targetFields = mapping.getOutputField();

        AtlasModule module = null;
        Field targetField = null;
        switch (mappingType) {

        case LOOKUP:
        case MAP:
            targetField = targetFields.get(0);
            module = resolveModule(FieldDirection.TARGET, targetField);
            if (!auditTargetFieldType(session, module, targetField)) {
                return;
            }
            session.head().setTargetField(targetField);
            module.processTargetFieldMapping(session);
            return;

        case COMBINE:
            targetField = targetFields.get(0);
            module = resolveModule(FieldDirection.TARGET, targetField);
            if (!auditTargetFieldType(session, module, targetField)) {
                return;
            }
            Field sourceField = processCombineField(session, mapping, sourceFields, targetField);
            session.head().setSourceField(sourceField).setTargetField(targetField);
            module.processTargetFieldMapping(session);
            return;

        case SEPARATE:
            List<Field> separatedFields = processSeparateField(session, mapping, sourceFields.get(0));
            if (separatedFields == null) {
                return;
            }

            for (Field f : targetFields) {
                targetField = f;
                module = resolveModule(FieldDirection.TARGET, targetField);
                if (!auditTargetFieldType(session, module, targetField)) {
                    continue;
                }
                if (targetField.getIndex() == null || targetField.getIndex() < 0) {
                    AtlasUtil.addAudit(session, targetField.getDocId(), String.format(
                            "Separate requires zero or positive Index value to be set on targetField targetField.path=%s",
                            targetField.getPath()), targetField.getPath(), AuditStatus.WARN, null);
                    continue;
                }
                if (separatedFields.size() <= targetField.getIndex()) {
                    String errorMessage = String.format(
                            "Separate returned fewer segments count=%s when targetField.path=%s requested index=%s",
                            separatedFields.size(), targetField.getPath(), targetField.getIndex());
                    AtlasUtil.addAudit(session, targetField.getDocId(), errorMessage, targetField.getPath(),
                            AuditStatus.WARN, null);
                    break;
                }
                session.head().setSourceField(separatedFields.get(targetField.getIndex())).setTargetField(targetField);
                module.processTargetFieldMapping(session);
            }
            return;

        default:
            AtlasUtil.addAudit(session, null,
                    String.format("Unsupported mappingType=%s detected", mapping.getMappingType()), null,
                    AuditStatus.ERROR, null);
        }
    }

    private boolean auditTargetFieldType(DefaultAtlasSession session, AtlasModule module, Field field) {
        if (module == null) {
            AtlasUtil.addAudit(session, field.getDocId(), String
                    .format("Module not found for field type='%s', path='%s'", field.getFieldType(), field.getPath()),
                    field.getPath(), AuditStatus.ERROR, null);
            return false;
        }
        if (!module.isSupportedField(field)) {
            AtlasUtil
                    .addAudit(session, field.getDocId(),
                            String.format("Unsupported target field type '%s' for DataSource '%s'",
                                    field.getClass().getName(), module.getUri()),
                            field.getPath(), AuditStatus.ERROR, null);
            return false;
        }
        return true;
    }

    private Field processCombineField(DefaultAtlasSession session, Mapping mapping, List<Field> sourceFields,
            Field targetField) throws AtlasException {
        Map<Integer, String> combineValues = null;
        for (Field sourceField : sourceFields) {
            if (sourceField.getIndex() == null || sourceField.getIndex() < 0) {
                AtlasUtil.addAudit(session, targetField.getDocId(), String.format(
                        "Combine requires zero or positive Index value to be set on all sourceFields sourceField.path=%s",
                        sourceField.getPath()), targetField.getPath(), AuditStatus.WARN, null);
                continue;
            }

            if (combineValues == null) {
                // We need to support a sorted map w/ null values
                combineValues = new HashMap<>();
            }

            if ((sourceField.getFieldType() != null) || (sourceField.getValue() != null)) {
                String sourceValue;
                try {
                    sourceValue = (String) factory.getConversionService().convertType(sourceField.getValue(),
                            sourceField.getFormat(), FieldType.STRING, null);
                } catch (AtlasConversionException e) {
                    AtlasUtil.addAudit(session, targetField.getDocId(),
                            String.format("Suitable converter for sourceField.path=%s hasn't been found",
                                    sourceField.getPath()),
                            targetField.getPath(), AuditStatus.WARN, null);

                    sourceValue = sourceField.getValue() != null ? sourceField.getValue().toString() : null;
                }
                combineValues.put(sourceField.getIndex(), sourceValue);
                continue;
            }
        }

        String combinedValue = null;
        StringDelimiter delimiter = StringDelimiter.fromName(mapping.getDelimiter());
        if (delimiter != null) {
            combinedValue = factory.getCombineStrategy().combineValues(combineValues, delimiter);
        } else {
            combinedValue = factory.getCombineStrategy().combineValues(combineValues);
        }

        Field answer = AtlasModelFactory.cloneFieldToSimpleField(sourceFields.get(0));
        if (combinedValue == null || combinedValue.trim().isEmpty()) {
            LOG.debug(String.format("Empty combined string for Combine mapping targetField.path=%s",
                    targetField.getPath()));
        } else {
            answer.setValue(combinedValue);
        }
        return answer;
    }

    private List<Field> processSeparateField(DefaultAtlasSession session, Mapping mapping, Field sourceField)
            throws AtlasException {
        if (sourceField.getValue() == null) {
            AtlasUtil.addAudit(session, sourceField.getDocId(),
                    String.format("null value can't be separated for sourceField.path=%s",
                            sourceField.getPath()),
                    sourceField.getPath(), AuditStatus.WARN, null);
            return null;
        }
        if (!sourceField.getValue().getClass().isAssignableFrom(String.class)) {
            Object converted = factory.getConversionService().convertType(
                    sourceField.getValue(), sourceField.getFormat(), FieldType.STRING, null);
            sourceField.setValue(converted);
        }

        List<Field> answer = new ArrayList<>();

        String sourceValue;
        try {
            sourceValue = (String) factory.getConversionService().convertType(sourceField.getValue(),
                    sourceField.getFormat(), FieldType.STRING, null);
        } catch (AtlasConversionException e) {
            AtlasUtil.addAudit(session, sourceField.getDocId(), String
                    .format("Suitable converter for sourceField.path=%s hasn't been found", sourceField.getPath()),
                    sourceField.getPath(), AuditStatus.WARN, null);
            sourceValue = sourceField.getValue().toString();
        }
        List<String> separatedValues = null;
        StringDelimiter delimiter = StringDelimiter.fromName(mapping.getDelimiter());
        if (delimiter != null) {
            separatedValues = factory.getSeparateStrategy().separateValue(sourceValue, delimiter);
        } else {
            separatedValues = factory.getSeparateStrategy().separateValue(sourceValue);
        }

        if (separatedValues == null || separatedValues.isEmpty()) {
            LOG.debug(String.format("Empty string for Separate mapping sourceField.path=%s", sourceField.getPath()));
        } else {
            for (String separatedValue : separatedValues) {
                SimpleField simpleField = AtlasModelFactory.cloneFieldToSimpleField(sourceField);
                simpleField.setValue(separatedValue);
                simpleField.setFieldType(FieldType.STRING);
                answer.add(simpleField);
            }
        }
        return answer;
    }

    @Override
    public void processValidation(AtlasSession userSession) throws AtlasException {
        if (!(userSession instanceof DefaultAtlasSession)) {
            throw new AtlasException(String.format("Unsupported session class '%s'", userSession.getClass().getName()));
        }
        if (!this.equals(userSession.getAtlasContext())) {
            throw new AtlasException("Cannot execute AtlasSession created by the other AtlasContext");
        }

        DefaultAtlasSession session = (DefaultAtlasSession) userSession;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Begin processValidation {}", session == null ? null : session.toString());
        }

        List<Validation> validations = getContextFactory().getValidationService().validateMapping(session.getMapping());
        if (validations != null && !validations.isEmpty()) {
            session.getValidations().getValidation().addAll(validations);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Detected {} core validation notices", validations.size());
        }

        for (AtlasModule module : getSourceModules().values()) {
            module.processPreValidation(session);
        }
        for (AtlasModule module : getTargetModules().values()) {
            module.processPreValidation(session);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("End processValidation {}", session == null ? null : session.toString());
        }
    }

    protected DefaultAtlasContextFactory getDefaultAtlasContextFactory() {
        return this.factory;
    }

    @Override
    public AtlasContextFactory getContextFactory() {
        return this.factory;
    }

    public AtlasMapping getMapping() {
        return mappingDefinition;
    }

    @Override
    public AtlasSession createSession() throws AtlasException {
        if (mappingDefinition == null && atlasMappingUri != null) {
            init();
        }
        return doCreateSession();
    }

    public AtlasSession createSession(AtlasMapping mappingDefinition) throws AtlasException {
        this.atlasMappingUri = null;
        this.mappingDefinition = mappingDefinition;
        init();
        return doCreateSession();
    }

    private AtlasSession doCreateSession() {
        AtlasSession session = new DefaultAtlasSession(mappingDefinition);
        session.setAtlasContext(this);
        session.setAudits(new Audits());
        session.setValidations(new Validations());
        setDefaultSessionProperties(session);
        return session;
    }

    protected void setDefaultSessionProperties(AtlasSession session) {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        df.setTimeZone(TimeZone.getDefault());
        session.getProperties().put("Atlas.CreatedDateTimeTZ", df.format(date));
    }

    public Map<String, AtlasModule> getSourceModules() {
        return sourceModules;
    }

    public void setSourceModules(Map<String, AtlasModule> sourceModules) {
        this.sourceModules = sourceModules;
    }

    public Map<String, AtlasModule> getTargetModules() {
        return targetModules;
    }

    public void setTargetModules(Map<String, AtlasModule> targetModules) {
        this.targetModules = targetModules;
    }

    public Map<String, LookupTable> getLookupTables() {
        return lookupTables;
    }

    public void setLookupTables(Map<String, LookupTable> lookupTables) {
        this.lookupTables = lookupTables;
    }

    protected void setJmxObjectName(ObjectName jmxObjectName) {
        this.jmxObjectName = jmxObjectName;
    }

    public ObjectName getJmxObjectName() {
        return this.jmxObjectName;
    }

    @Override
    public String getUuid() {
        return (this.uuid != null ? this.uuid.toString() : null);
    }

    @Override
    public String getVersion() {
        return this.getClass().getPackage().getImplementationVersion();
    }

    @Override
    public String getMappingName() {
        return (mappingDefinition != null ? mappingDefinition.getName() : null);
    }

    protected void setMappingUri(URI atlasMappingUri) {
        this.atlasMappingUri = atlasMappingUri;
    }

    @Override
    public String getMappingUri() {
        return (atlasMappingUri != null ? atlasMappingUri.toString() : null);
    }

    @Override
    public String getClassName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getThreadName() {
        return Thread.currentThread().getName();
    }

    @Override
    public String toString() {
        return "DefaultAtlasContext [jmxObjectName=" + jmxObjectName + ", uuid=" + uuid + ", factory=" + factory
                + ", mappingName=" + getMappingName() + ", mappingUri=" + getMappingUri() + ", sourceModules="
                + sourceModules + ", targetModules=" + targetModules + "]";
    }
}
