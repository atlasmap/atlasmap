/*
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

import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.nio.file.Paths;
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
import java.util.stream.Collectors;

import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasConstants;
import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasContextFactory;
import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.mxbean.AtlasContextMXBean;
import io.atlasmap.spi.AtlasInternalSession;
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
import io.atlasmap.v2.CopyTo;
import io.atlasmap.v2.CustomMapping;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceKey;
import io.atlasmap.v2.DataSourceMetadata;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.LookupTable;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import io.atlasmap.v2.Mappings;
import io.atlasmap.v2.PropertyField;
import io.atlasmap.v2.SimpleField;
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.Validations;

public class DefaultAtlasContext implements AtlasContext, AtlasContextMXBean {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAtlasContext.class);
    private ObjectName jmxObjectName;
    private final UUID uuid;
    private DefaultAtlasContextFactory factory;
    private URI atlasMappingUri;
    private ADMArchiveHandler admHandler;
    private Map<String, AtlasModule> sourceModules = new HashMap<>();
    private Map<String, AtlasModule> targetModules = new HashMap<>();
    private Map<String, LookupTable> lookupTables = new HashMap<>();
    private Map<DataSourceKey, DataSourceMetadata> dataSourceMetadataMap;
    private boolean initialized;

    public DefaultAtlasContext(URI atlasMappingUri) {
        this(DefaultAtlasContextFactory.getInstance(), atlasMappingUri);
    }

    public DefaultAtlasContext(DefaultAtlasContextFactory factory, URI atlasMappingUri) {
        this.factory = factory;
        this.uuid = UUID.randomUUID();
        this.atlasMappingUri = atlasMappingUri;
    }

    public DefaultAtlasContext(DefaultAtlasContextFactory factory, AtlasMapping mapping) {
        this.factory = factory;
        this.uuid = UUID.randomUUID();
        this.admHandler = new ADMArchiveHandler(factory.getClassLoader());
        this.admHandler.setIgnoreLibrary(true);
        this.admHandler.setMappingDefinition(mapping);
    }

    public DefaultAtlasContext(DefaultAtlasContextFactory factory,
            AtlasContextFactory.Format format, InputStream stream) throws AtlasException {
        this.factory = factory;
        this.uuid = UUID.randomUUID();
        this.admHandler = new ADMArchiveHandler(factory.getClassLoader());
        this.admHandler.setIgnoreLibrary(true);
        this.admHandler.load(format, stream);
        this.dataSourceMetadataMap = this.admHandler.getDataSourceMetadataMap();
    }

    /**
     * Compare the core version to the extract mapping version.  No mapping version
     * we'll accept as ok, otherwise compare major.minor.
     *
     * @param coreVersion - this runtime version
     * @param mappingVersion - the extracted mapping version
     *
     * @return true if valid, false otherwise
     */
    private boolean validateVersion(String coreVersion, String mappingVersion) {
        if (mappingVersion != null && mappingVersion.length() > 0) {
            String[] mappingVersionComps = mappingVersion.split("\\.");
            String[] coreVersionComps = coreVersion.split("\\.");
            if (coreVersionComps.length < 2 || mappingVersionComps.length < 2) {
                return false;
            }
            if (Integer.parseInt(coreVersionComps[0]) < Integer.parseInt(mappingVersionComps[0])) {
                return false;
            }
            if (Integer.parseInt(coreVersionComps[0]) == Integer.parseInt(mappingVersionComps[0]) &&
                Integer.parseInt(coreVersionComps[1]) < Integer.parseInt(mappingVersionComps[1])) {
                return false;
            }
        }
        return true;
    }

    /**
     * TODO: For dynamic re-load. This needs lock()
     *
     * @throws AtlasException failed to initialize
     */
    protected synchronized void init() throws AtlasException {

        if (this.initialized) {
            return;
        }

        registerJmx(this);

        if (this.atlasMappingUri != null) {
            this.admHandler = new ADMArchiveHandler(factory.getClassLoader());
            this.admHandler.setIgnoreLibrary(true);
            this.admHandler.load(Paths.get(this.atlasMappingUri));
            this.dataSourceMetadataMap = this.admHandler.getDataSourceMetadataMap();
        }
        if (this.admHandler == null || this.admHandler.getMappingDefinition() == null) {
            LOG.warn("AtlasMap context cannot initialize without mapping definition, ignoring:"
                    + " Mapping URI={}"
                    , this.atlasMappingUri);
            return;
        }
        AtlasMapping atlasMapping = this.admHandler.getMappingDefinition();
        String version = factory.getProperties().get(AtlasContextFactory.PROPERTY_ATLASMAP_CORE_VERSION);
        String mappingVersion = atlasMapping.getVersion();
        if (!validateVersion(version, mappingVersion)) {
            LOG.error("Mapping definition version {} detected. It may not work as expected with runtime version {}.",
                mappingVersion,
                version);
        }
        sourceModules.clear();
        ConstantModule constant = new ConstantModule();
        constant.setConversionService(factory.getConversionService());
        constant.setFieldActionService(factory.getFieldActionService());
        sourceModules.put(AtlasConstants.CONSTANTS_DOCUMENT_ID, constant);
        PropertyModule property = new PropertyModule(factory.getPropertyStrategy());
        property.setConversionService(factory.getConversionService());
        property.setFieldActionService(factory.getFieldActionService());
        property.setMode(AtlasModuleMode.SOURCE);
        sourceModules.put(AtlasConstants.PROPERTIES_SOURCE_DOCUMENT_ID, property);
        targetModules.clear();
        property = new PropertyModule(factory.getPropertyStrategy());
        property.setConversionService(factory.getConversionService());
        property.setFieldActionService(factory.getFieldActionService());
        property.setMode(AtlasModuleMode.TARGET);
        targetModules.put(AtlasConstants.PROPERTIES_TARGET_DOCUMENT_ID, property);

        lookupTables.clear();
        if (admHandler.getMappingDefinition().getLookupTables() != null
                && admHandler.getMappingDefinition().getLookupTables().getLookupTable() != null) {
            for (LookupTable table : admHandler.getMappingDefinition().getLookupTables().getLookupTable()) {
                lookupTables.put(table.getName(), table);
            }
        }

        AtlasModuleInfoRegistry moduleInfoRegistry = factory.getModuleInfoRegistry();
        for (DataSource ds : admHandler.getMappingDefinition().getDataSource()) {
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
                AtlasModule module = moduleInfo.getModuleClass().getDeclaredConstructor().newInstance();
                module.setClassLoader(factory.getClassLoader());
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
                module.setDocName(ds.getName());
                if (this.dataSourceMetadataMap != null) {
                    DataSourceKey dskey = new DataSourceKey(ds.getDataSourceType() == DataSourceType.SOURCE, docId);
                    DataSourceMetadata meta = this.dataSourceMetadataMap.get(dskey);
                    if (meta != null) {
                        module.setDataSourceMetadata(meta);
                    }
                }
                module.init();
            } catch (Exception t) {
                LOG.error("Unable to initialize {} module: {}", ds.getDataSourceType(), moduleInfo);
                LOG.error(t.getMessage(), t);
                throw new AtlasException(String.format("Unable to initialize %s module: %s", ds.getDataSourceType(),
                        moduleInfo.toString()), t);
            }
        }
        initialized = true;
    }

    protected void registerJmx(DefaultAtlasContext context) {
        try {
            setJmxObjectName(new ObjectName(
                    getContextFactory().getJmxObjectName() + ",context=Contexts,uuid=" + uuid.toString()));
            if (ManagementFactory.getPlatformMBeanServer().isRegistered(getJmxObjectName())) {
                ManagementFactory.getPlatformMBeanServer().registerMBean(this, getJmxObjectName());
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Registered AtlasContext {} with JMX", context.getUuid());
                }
            }
        } catch (Exception t) {
            LOG.warn("Failed to register AtlasContext {} with JMX", context.getUuid());
            LOG.warn(t.getMessage(), t);
        }
    }

    /**
     * Process single mapping entry in preview mode. Since modules don't participate
     * in preview mode, any document format specific function won't be applied.
     * @deprecated Use {@code AtlasPreviewContext#processPreview(Mapping)}
     *
     * @param mapping A @link{Mapping} entry to process
     */
    @Override
    @Deprecated
    public Audits processPreview(Mapping mapping) throws AtlasException {
        return this.factory.createPreviewContext().processPreview(mapping);
    }

    protected Field applyFieldActions(DefaultAtlasSession session, Field field) {
        if (field.getActions() == null) {
            return field;
        }
        try {
            return factory.getFieldActionService().processActions(session, field);
        } catch (AtlasException e) {
            AtlasUtil.addAudit(session, field, String.format(
                    "Failed to apply field action: %s", AtlasUtil.getChainedMessage(e)),
                    AuditStatus.ERROR, null);
            if (LOG.isDebugEnabled()) {
                LOG.error("", e);
            }
            return field;
        }
    }

    /**
     * Process session lifecycle.
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

        // TODO https://github.com/atlasmap/atlasmap/issues/863 - Add an option to enable/disable runtime validation
        processValidation(session);
        for (Validation v : session.getValidations().getValidation()) {
            AtlasUtil.addAudit(session, v);
        }

        // Additional runtime only audit
        Mappings mappings = session.getMapping().getMappings();
        if (mappings != null && mappings.getMapping().isEmpty()) {
            AtlasUtil.addAudit(session, (String)null,
                String.format("Field mappings should not be empty"),
                AuditStatus.WARN, null);
        }

        session.getValidations().getValidation().clear();
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
            for (BaseMapping innerMapping : unwrapCollectionMappings(session, baseMapping)) {
                if (innerMapping instanceof CustomMapping) {
                    DefaultAtlasCustomMappingProcessor.getInstance().process(
                            session, (CustomMapping)innerMapping);
                    continue;
                }

                Mapping mapping = (Mapping) innerMapping;
                session.head().setMapping(mapping).setLookupTable(lookupTables.get(mapping.getLookupTableName()));

                if (mapping.getOutputField() == null || mapping.getOutputField().isEmpty()) {
                    AtlasUtil.addAudit(session, (String)null,
                            String.format("Mapping does not contain at least one target field: alias=%s desc=%s",
                                    mapping.getAlias(), mapping.getDescription()),
                            AuditStatus.WARN, null);
                    continue;
                }

                processSourceFieldMapping(session);
                if (!session.head().hasError()) {
                    processTargetFieldMapping(session, mapping);
                }
                session.getAudits().getAudit().addAll(session.head().getAudits());
                session.head().unset();
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

    // just unwrap collection mappings to be compatible with older UI
    private List<BaseMapping> unwrapCollectionMappings(DefaultAtlasSession session, BaseMapping baseMapping) {
        if (baseMapping.getMappingType() == null || !baseMapping.getMappingType().equals(MappingType.COLLECTION)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Mapping is not a collection mapping, not cloning: {}", baseMapping);
            }
            return Arrays.asList((BaseMapping) baseMapping);
        }

        List<BaseMapping> mappings = new LinkedList<>();
        for(BaseMapping m : ((Collection) baseMapping).getMappings().getMapping()) {
            mappings.add(m);
        }
        return mappings;
    }

    protected void processSourceFieldMapping(DefaultAtlasSession session) {
        try {
            Mapping mapping = session.head().getMapping();
            if (mapping.getInputFieldGroup() != null) {
                if (mapping.getExpression() != null) {
                    session.head().setSourceField(mapping.getInputFieldGroup());
                    DefaultAtlasExpressionProcessor.processExpression(session, mapping.getExpression());
                } else {
                    processSourceFieldGroup(session, mapping.getInputFieldGroup());
                }
            } else if (mapping.getInputField() != null && !mapping.getInputField().isEmpty()) {
                if (mapping.getExpression() != null) {
                    FieldGroup sourceFieldGroup = new FieldGroup();
                    sourceFieldGroup.getField().addAll(mapping.getInputField());
                    session.head().setSourceField(sourceFieldGroup);
                    DefaultAtlasExpressionProcessor.processExpression(session, mapping.getExpression());
                } else {
                    List<Field> sourceFields = mapping.getInputField();
                    applyCopyToActions(sourceFields, mapping);
                    processSourceFields(session, sourceFields);
                }
            } else {
                session.head().addAudit(AuditStatus.WARN, null, String.format(
                    "Mapping does not contain expression or at least one source field: alias=%s desc=%s",
                    mapping.getAlias(), mapping.getDescription()));
            }
        } catch (Exception t) {
            Field sourceField = session.head().getSourceField();
            String docId = sourceField != null ? sourceField.getDocId() : null;
            String path =  sourceField != null ? sourceField.getPath() : null;
            session.head().addAudit(AuditStatus.ERROR, sourceField, String.format(
                    "Unexpected exception is thrown while reading source field: %s", t.getMessage()));
            if (LOG.isDebugEnabled()) {
                LOG.error("", t);
            }
        }
    }

    private void processSourceFieldGroup(DefaultAtlasSession session, FieldGroup sourceFieldGroup) throws AtlasException {
        processSourceFields(session, sourceFieldGroup.getField());
        session.head().setSourceField(sourceFieldGroup);
        Field processed = applyFieldActions(session, session.head().getSourceField());
        session.head().setSourceField(processed);
    }

    private void processSourceFields(DefaultAtlasSession session, List<Field> sourceFields)
            throws AtlasException {
        for (int i = 0; i < sourceFields.size(); i++) {
            Field sourceField = sourceFields.get(i);
            session.head().setSourceField(sourceField);
            if (sourceField instanceof FieldGroup) {
                processSourceFields(session, ((FieldGroup)sourceField).getField());
                Field processed = applyFieldActions(session, sourceField);
                session.head().setSourceField(processed);
                continue;
            }

            AtlasModule module = resolveModule(FieldDirection.SOURCE, sourceField);
            if (module == null) {
                AtlasUtil.addAudit(session, sourceField,
                        String.format("Module not found for docId '%s'", sourceField.getDocId()),
                        AuditStatus.ERROR, null);
                return;
            }
            if (!module.isSupportedField(sourceField)) {
                AtlasUtil.addAudit(session, sourceField,
                        String.format("Unsupported source field type '%s' for DataSource '%s'",
                                sourceField.getClass().getName(), module.getUri()),
                        AuditStatus.ERROR, null);
                return;
            }

            module.readSourceValue(session);
            Field processed = applyFieldActions(session, session.head().getSourceField());
            session.head().setSourceField(processed);
            sourceFields.set(i, processed);
        }
    }

    protected AtlasModule resolveModule(FieldDirection direction, Field field) {
        String docId = field.getDocId();
        if (docId == null || docId.isEmpty()) {
            docId = direction == FieldDirection.SOURCE ? AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID
                    : AtlasConstants.DEFAULT_TARGET_DOCUMENT_ID;
        }
        Map<String, AtlasModule> modules =
                direction == FieldDirection.SOURCE ? sourceModules : targetModules;
        if (direction == FieldDirection.SOURCE && field instanceof ConstantField) {
            AtlasModule answer = sourceModules.get(AtlasConstants.CONSTANTS_DOCUMENT_ID);
            if (!modules.containsKey(docId)) {
                modules.put(docId, answer);
            }
            return answer;
        }
        if (field instanceof PropertyField) {
            AtlasModule answer = modules.get(
                    direction == FieldDirection.SOURCE ? AtlasConstants.PROPERTIES_SOURCE_DOCUMENT_ID : AtlasConstants.PROPERTIES_TARGET_DOCUMENT_ID);
            if (!modules.containsKey(docId)) {
                modules.put(docId, answer);
            }
            return answer;
        }
        return modules.get(docId);
    }

    /**
     * Checks for CopyTo actions and correctly sets the path for targetField by setting the indexes specified in each action
     */
    private void applyCopyToActions(List<Field> sourceFields, Mapping mapping) {
        for (Field sourceField : sourceFields) {

            if (sourceField instanceof FieldGroup) {
                applyCopyToActions(((FieldGroup) sourceField).getField(), mapping);
                continue;
            }

            if (sourceField.getActions() == null) {
                continue;
            }

            List<CopyTo> copyTos = sourceField.getActions().stream().filter(a -> a instanceof CopyTo).map(a -> (CopyTo) a).collect(Collectors.toList());
            if (copyTos.size() == 0) {
                return;
            }

            if (copyTos.stream().flatMap(c -> c.getIndexes().stream().filter(i -> i < 0)).count() > 0) {
                throw new IllegalArgumentException("Indexes must be >= 0");
            }

            /*
             * For each index present in CopyTo, set the corresponding index in the path.
             * each index of copyTo is supposed to have a counterpart in the path.
             */
            for (CopyTo copyTo : copyTos) {
                for (Field field : mapping.getOutputField()) {
                    AtlasPath path = new AtlasPath(field.getPath());
                    List<AtlasPath.SegmentContext> segments = path.getCollectionSegments(true);
                    for (int i = 0; i < copyTo.getIndexes().size(); i++) {
                        if (i < segments.size()) { // In case there are too many indexes specified
                            path.setCollectionIndex(i + 1, copyTo.getIndexes().get(i));// +1 since 0 is the root segment
                        }
                    }
                    field.setPath(path.toString());
                }
                // The processor associated to this action is a fake. It shall not execute, so remove the action.
                sourceField.getActions().remove(copyTo);
            }
        }
    }

    private void processTargetFieldMapping(DefaultAtlasSession session, Mapping mapping) {
        MappingType mappingType = mapping.getMappingType();
        List<Field> sourceFields = mapping.getInputField();
        List<Field> targetFields = mapping.getOutputField();

        AtlasModule module = null;
        Field targetField = null;
        if (mappingType == null || mappingType == MappingType.LOOKUP || mappingType == MappingType.MAP) {
            Field sourceField = session.head().getSourceField();
            FieldGroup sourceFieldGroup = null;
            if (sourceField instanceof FieldGroup) {
                sourceFieldGroup = unwrapNestedGroup((FieldGroup)sourceField);
            }
            for (Field f : targetFields) {
                targetField = f;
                module = resolveModule(FieldDirection.TARGET, targetField);
                if (!auditTargetFieldType(session, module, targetField)) {
                    continue;
                }
                session.head().setTargetField(targetField);
                if (sourceFieldGroup != null) {
                    Integer index = targetField.getIndex();
                    AtlasPath targetPath = new AtlasPath(targetField.getPath());
                    if (targetPath.hasCollection() && !targetPath.isIndexedCollection()) {
                        if (targetFields.size() > 1) {
                            AtlasUtil.addAudit(session, targetField,
                                    "It's not yet supported to have a collection field as a part of multiple target fields in a same mapping",
                                    AuditStatus.ERROR, null);
                            return;
                        }
                        session.head().setSourceField(sourceFieldGroup);
                    } else if (index == null) {
                        if (sourceFieldGroup.getField().size() > 0) {
                            session.head().setSourceField(sourceFieldGroup.getField().get(sourceFieldGroup.getField().size()-1));
                        }
                    } else {
                        if (sourceFieldGroup.getField().size() > index) {
                            session.head().setSourceField(sourceFieldGroup.getField().get(index));
                        } else {
                            AtlasUtil.addAudit(session, targetField, String.format(
                                    "The number of source fields '%s' is fewer than expected via target field index '%s'",
                                    sourceFieldGroup.getField().size(), targetField.getIndex()),
                                    AuditStatus.WARN, null);
                            continue;
                        }
                    }
                }
                try {
                    module.populateTargetField(session);
                } catch (Exception e) {
                    AtlasUtil.addAudit(session, targetField,
                            "Failed to populate target field: " + e.getMessage(),
                            AuditStatus.ERROR, null);
                    if (LOG.isDebugEnabled()) {
                        LOG.error(String.format("populateTargetField() failed for %s:%s",
                                targetField.getDocId(), targetField.getPath()), e);
                    }
                    return;
                }
                Field processed = applyFieldActions(session, session.head().getTargetField());
                session.head().setTargetField(processed);
                try {
                    module.writeTargetValue(session);
                } catch (Exception e) {
                    AtlasUtil.addAudit(session, targetField,
                            "Failed to write field value into target document: " + e.getMessage(),
                            AuditStatus.ERROR, null);
                    if (LOG.isDebugEnabled()) {
                        LOG.error(String.format("writeTargetValue() failed for %s:%s",
                                targetField.getDocId(), targetField.getPath()), e);
                    }
                    return;
                }
            }
            return;

        } else if (mappingType == MappingType.COMBINE) {
            targetField = targetFields.get(0);
            module = resolveModule(FieldDirection.TARGET, targetField);
            if (!auditTargetFieldType(session, module, targetField)) {
                return;
            }
            Field sourceField = processCombineField(session, mapping, sourceFields, targetField);
            session.head().setSourceField(sourceField).setTargetField(targetField);
            try {
                module.populateTargetField(session);
            } catch (Exception e) {
                AtlasUtil.addAudit(session, targetField,
                        "Failed to populate target field: " + e.getMessage(),
                        AuditStatus.ERROR, null);
                return;
            }
            applyFieldActions(session, session.head().getTargetField());
            try {
                module.writeTargetValue(session);
            } catch (Exception e) {
                AtlasUtil.addAudit(session, targetField,
                        "Failed to write field value into target document: " + e.getMessage(),
                        AuditStatus.ERROR, null);
                return;
            }
            return;

        } else if (mappingType == MappingType.SEPARATE) {
            List<Field> separatedFields = null;
            try {
                separatedFields = processSeparateField(session, mapping, sourceFields.get(0));
            } catch (Exception e) {
                AtlasUtil.addAudit(session, targetField,
                        "Failed to process separate mode: " + e.getMessage(),
                        AuditStatus.ERROR, null);
                return;
            }
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
                    AtlasUtil.addAudit(session, targetField, String.format(
                            "Separate requires zero or positive Index value to be set on targetField targetField.path=%s",
                            targetField.getPath()), AuditStatus.WARN, null);
                    continue;
                }
                if (separatedFields.size() <= targetField.getIndex()) {
                    String errorMessage = String.format(
                            "Separate returned fewer segments count=%s when targetField.path=%s requested index=%s",
                            separatedFields.size(), targetField.getPath(), targetField.getIndex());
                    AtlasUtil.addAudit(session, targetField, errorMessage, AuditStatus.WARN, null);
                    break;
                }
                session.head().setSourceField(separatedFields.get(targetField.getIndex())).setTargetField(targetField);
                try {
                    module.populateTargetField(session);
                } catch (Exception e) {
                    AtlasUtil.addAudit(session, targetField,
                            "Failed to populate target field: " + e.getMessage(),
                            AuditStatus.ERROR, null);
                    return;
                }
                Field processed = applyFieldActions(session, session.head().getTargetField());
                session.head().setTargetField(processed);
                try {
                    module.writeTargetValue(session);
                } catch (Exception e) {
                    AtlasUtil.addAudit(session, targetField,
                            "Failed to write field value into target document: " + e.getMessage(),
                            AuditStatus.ERROR, null);
                    return;
                }
            }
            return;
        }

        AtlasUtil.addAudit(session, (String)null,
                String.format("Unsupported mappingType=%s detected", mapping.getMappingType()),
                AuditStatus.ERROR, null);
    }

    private FieldGroup unwrapNestedGroup(FieldGroup parent) {
        if (parent.getPath() == null && parent.getField().size() == 1
                && parent.getField().get(0) instanceof FieldGroup) {
            return (FieldGroup) parent.getField().get(0);
        }
        return parent;
    }

    private boolean auditTargetFieldType(DefaultAtlasSession session, AtlasModule module, Field field) {
        if (module == null) {
            AtlasUtil.addAudit(session, field, String
                    .format("Module not found for field type='%s', path='%s'", field.getFieldType(), field.getPath()),
                    AuditStatus.ERROR, null);
            return false;
        }
        if (!module.isSupportedField(field)) {
            AtlasUtil
                    .addAudit(session, field,
                            String.format("Unsupported target field type '%s' for DataSource '%s'",
                                    field.getClass().getName(), module.getUri()),
                            AuditStatus.ERROR, null);
            return false;
        }
        return true;
    }

    protected Field processCombineField(DefaultAtlasSession session, Mapping mapping, List<Field> sourceFields,
            Field targetField) {
        Map<Integer, String> combineValues = null;
        for (Field sourceField : sourceFields) {
            if (sourceField.getIndex() == null || sourceField.getIndex() < 0) {
                AtlasUtil.addAudit(session, targetField, String.format(
                        "Combine requires zero or positive Index value to be set on all sourceFields sourceField.path=%s",
                        sourceField.getPath()), AuditStatus.WARN, null);
                continue;
            }

            if (combineValues == null) {
                // We need to support a sorted map w/ null values
                combineValues = new HashMap<>();
            }

            if (sourceField.getValue() != null) {
                String sourceValue;
                try {
                    sourceValue = (String) factory.getConversionService().convertType(sourceField.getValue(),
                            sourceField.getFormat(), FieldType.STRING, null);
                } catch (AtlasConversionException e) {
                    AtlasUtil.addAudit(session, targetField,
                            String.format("Suitable converter for sourceField.path=%s hasn't been found",
                                    sourceField.getPath()),
                            AuditStatus.WARN, null);

                    sourceValue = sourceField.getValue() != null ? sourceField.getValue().toString() : null;
                }
                combineValues.put(sourceField.getIndex(), sourceValue);
            }
        }

        String combinedValue = null;
        StringDelimiter delimiter = StringDelimiter.fromName(mapping.getDelimiter());
        if (delimiter != null) {
            combinedValue = factory.getCombineStrategy().combineValues(combineValues, delimiter);
        } else if (mapping.getDelimiterString() != null && !mapping.getDelimiterString().isEmpty()) {
            combinedValue = factory.getCombineStrategy().combineValues(combineValues, mapping.getDelimiterString());
        } else {
            combinedValue = factory.getCombineStrategy().combineValues(combineValues);
        }

        Field answer = AtlasModelFactory.cloneFieldToSimpleField(sourceFields.get(0));
        if (combinedValue == null || combinedValue.trim().isEmpty()) {
            LOG.debug("Empty combined string for Combine mapping targetField.path={}",
                      targetField.getPath());
        } else {
            answer.setValue(combinedValue);
        }
        return answer;
    }

    protected List<Field> processSeparateField(DefaultAtlasSession session, Mapping mapping, Field sourceField)
            throws AtlasException {
        if (sourceField.getValue() == null) {
            AtlasUtil.addAudit(session, sourceField,
                    String.format("null value can't be separated for sourceField.path=%s",
                            sourceField.getPath()),
                    AuditStatus.WARN, null);
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
            AtlasUtil.addAudit(session, sourceField, String
                    .format("Suitable converter for sourceField.path=%s hasn't been found", sourceField.getPath()),
                    AuditStatus.WARN, null);
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
            LOG.debug("Empty string for Separate mapping sourceField.path={}", sourceField.getPath());
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
            LOG.debug("Begin processValidation {}", session);
        }

        AtlasMapping atlasMapping = this.admHandler.getMappingDefinition();
        String version = factory.getProperties().get(AtlasContextFactory.PROPERTY_ATLASMAP_CORE_VERSION);
        String mappingVersion = atlasMapping.getVersion();
        if (!validateVersion(version, mappingVersion)) {
            AtlasUtil.addAudit((AtlasInternalSession)userSession, (Field)null,
                String.format("Mapping definition version %s detected. It may not work as expected with runtime version %s.",
                    mappingVersion,
                    version),
                AuditStatus.WARN, null);
        }

        List<Validation> validations = getContextFactory().getValidationService().validateMapping(session.getMapping());
        if (validations != null && !validations.isEmpty()) {
            session.getValidations().getValidation().addAll(validations);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Detected {} core validation notices", validations == null ? 0 : validations.size());
        }

        for (AtlasModule module : getSourceModules().values()) {
            module.processPreValidation(session);
        }
        for (AtlasModule module : getTargetModules().values()) {
            module.processPreValidation(session);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("End processValidation {}", session);
        }
    }

    @Override
    public DefaultAtlasContextFactory getContextFactory() {
        return this.factory;
    }

    public AtlasMapping getMapping() {
        return admHandler != null ? admHandler.getMappingDefinition() : null;
    }

    @Override
    public AtlasSession createSession() throws AtlasException {
        init();
        return doCreateSession();
    }

    public AtlasSession createSession(AtlasMapping mappingDefinition) throws AtlasException {
        this.atlasMappingUri = null;
        this.admHandler = new ADMArchiveHandler(this.factory.getClassLoader());
        this.admHandler.setIgnoreLibrary(true);
        this.admHandler.setMappingDefinition(mappingDefinition);
        this.initialized = false;
        init();
        return doCreateSession();
    }

    private AtlasSession doCreateSession() throws AtlasException {
        AtlasSession session = new DefaultAtlasSession(this);
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
        return (admHandler.getMappingDefinition() != null
                ? admHandler.getMappingDefinition().getName() : null);
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

    public ADMArchiveHandler getADMArchiveHandler() {
        return this.admHandler;
    }

}
