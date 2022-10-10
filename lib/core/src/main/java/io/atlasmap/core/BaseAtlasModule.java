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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.openmbean.TabularData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasException;
import io.atlasmap.mxbean.AtlasModuleMXBean;
import io.atlasmap.spi.AtlasCollectionHelper;
import io.atlasmap.spi.AtlasConversionService;
import io.atlasmap.spi.AtlasFieldActionService;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasModule;
import io.atlasmap.spi.AtlasModuleDetail;
import io.atlasmap.spi.AtlasModuleMode;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.DataSourceMetadata;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.LookupEntry;
import io.atlasmap.v2.LookupTable;
import io.atlasmap.v2.SimpleField;

public abstract class BaseAtlasModule implements AtlasModule, AtlasModuleMXBean {
    private static final Logger LOG = LoggerFactory.getLogger(BaseAtlasModule.class);

    private boolean automaticallyProcessOutputFieldActions = true;
    private AtlasConversionService atlasConversionService = null;
    private AtlasFieldActionService atlasFieldActionService = null;
    private AtlasCollectionHelper collectionHelper = null;
    private AtlasModuleMode atlasModuleMode = AtlasModuleMode.UNSET;
    private String docId;
    private String docName;
    private String uri;
    private String uriDataType;
    private Map<String,String> uriParameters = new HashMap<>();
    private ClassLoader classLoader;
    private DataSourceMetadata dataSourceMetadata;

    @Override
    public void init() throws AtlasException {
        // no-op now
    }

    @Override
    public void destroy() throws AtlasException {
        // no-op now
    }

    @Override
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    @Override
    public void processPostValidation(AtlasInternalSession session) throws AtlasException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processPostValidation completed", getDocId());
        }
    }

    @Override
    public void populateTargetField(AtlasInternalSession session) throws AtlasException {
        Field sourceField = session.head().getSourceField();
        Field targetField = session.head().getTargetField();
        Object targetValue = null;
        if (targetField.getFieldType() == null
                || (sourceField.getFieldType() != null && sourceField.getFieldType().equals(targetField.getFieldType()))) {
            targetValue = sourceField.getValue();
        } else if (sourceField.getValue() != null) {
            try {
                targetValue = getConversionService().convertType(sourceField.getValue(), sourceField.getFormat(),
                        targetField.getFieldType(), targetField.getFormat());
            } catch (AtlasConversionException e) {
                AtlasUtil.addAudit(session, targetField,
                        String.format("Unable to auto-convert for sT=%s tT=%s tF=%s msg=%s", sourceField.getFieldType(),
                                targetField.getFieldType(), targetField.getPath(), e.getMessage()),
                        AuditStatus.ERROR, null);
                return;
            }
        }
        targetField.setValue(targetValue);
        targetField.setStatus(sourceField.getStatus());

        LookupTable lookupTable = session.head().getLookupTable();
        if (lookupTable != null) {
            processLookupField(session, lookupTable, targetField.getValue(), targetField);
        }
    }

    protected void processLookupField(AtlasInternalSession session, LookupTable lookupTable, Object sourceValue,
            Field targetField) throws AtlasException {
        String lookupValue = null;
        FieldType lookupType = null;
        for (LookupEntry lkp : lookupTable.getLookupEntry()) {
            if (lkp.getSourceValue().equals(sourceValue)) {
                lookupValue = lkp.getTargetValue();
                lookupType = lkp.getTargetType();
                break;
            }
        }

        Object targetValue = null;
        if (lookupType == null || FieldType.STRING.equals(lookupType)) {
            targetValue = lookupValue;
        } else {
            targetValue = atlasConversionService.convertType(lookupValue, FieldType.STRING, lookupType);
        }

        FieldType targetFieldType = targetField.getFieldType();
        if (targetFieldType == null || targetFieldType == FieldType.COMPLEX) {
            targetField.setFieldType(lookupType != null ? lookupType : FieldType.STRING);
            targetFieldType = targetField.getFieldType();
        }
        if (targetFieldType != null && !targetFieldType.equals(lookupType)) {
            targetValue = atlasConversionService.convertType(targetValue, lookupType, targetField.getFieldType());
        }

        targetField.setValue(targetValue);
    }

    protected Field applyTargetFieldActions(AtlasInternalSession session) throws AtlasException {
        Field field = session.head().getTargetField();
        if (isAutomaticallyProcessOutputFieldActions() && field.getActions() != null
                && field.getActions() != null) {
            return getFieldActionService().processActions(session, field);
        }
        return field;
    }

    protected Field applySourceFieldActions(AtlasInternalSession session) throws AtlasException {
        Field field = session.head().getSourceField();
        if (field.getActions() != null && field.getActions() != null) {
            return getFieldActionService().processActions(session, field);
        }
        return field;
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
    public AtlasCollectionHelper getCollectionHelper() {
        return collectionHelper;
    }

    @Override
    public String getDocId() {
        return docId;
    }

    @Override
    public void setDocId(String docId) {
        this.docId = docId;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public void setUri(String uri) {
        this.uri = uri;
        this.uriDataType = AtlasUtil.getUriDataType(uri);
        this.uriParameters = AtlasUtil.getUriParameters(uri);
    }

    @Override
    public String getUriDataType() {
        return this.uriDataType;
    }

    @Override
    public Map<String, String> getUriParameters() {
        return Collections.unmodifiableMap(uriParameters);
    }

    @Override
    public void setConversionService(AtlasConversionService atlasConversionService) {
        this.atlasConversionService = atlasConversionService;
    }

    @Override
    public AtlasFieldActionService getFieldActionService() {
        return this.atlasFieldActionService;
    }

    @Override
    public void setFieldActionService(AtlasFieldActionService atlasFieldActionService) {
        this.atlasFieldActionService = atlasFieldActionService;
        this.collectionHelper = createCollectionHelper(atlasFieldActionService);
    }

    protected AtlasCollectionHelper createCollectionHelper(AtlasFieldActionService fieldActionService) {
        return new DefaultAtlasCollectionHelper(fieldActionService);
    }

    public boolean isAutomaticallyProcessOutputFieldActions() {
        return automaticallyProcessOutputFieldActions;
    }

    public void setAutomaticallyProcessOutputFieldActions(boolean automaticallyProcessOutputFieldActions) {
        this.automaticallyProcessOutputFieldActions = automaticallyProcessOutputFieldActions;
    }

    @Override
    public Boolean isSupportedField(Field field) {
        return field instanceof SimpleField || field instanceof FieldGroup;
    }

    @Override
    public void setDataSourceMetadata(DataSourceMetadata meta) {
        this.dataSourceMetadata = meta;
    }

    @Override
    public DataSourceMetadata getDataSourceMetadata() {
        return this.dataSourceMetadata;
    }

    @Override
    public void setDocName(String docName) {
        this.docName = docName;
    }

    @Override
    public String getDocName() {
        return this.docName;
    }

    //-----------------------------------------
    // JMX MBean methods
    //-----------------------------------------

    @Override
    public boolean isSourceSupported() {
        return Arrays.asList(this.getClass().getAnnotation(AtlasModuleDetail.class).modes()).contains("SOURCE");
    }

    @Override
    public boolean isTargetSupported() {
        return Arrays.asList(this.getClass().getAnnotation(AtlasModuleDetail.class).modes()).contains("TARGET");
    }

    @Override
    public String getClassName() {
        return this.getClass().getName();
    }

    @Override
    public String[] getDataFormats() {
        return this.getClass().getAnnotation(AtlasModuleDetail.class).dataFormats();
    }

    @Override
    public String getModeName() {
        return this.atlasModuleMode.name();
    }

    @Override
    public String getName() {
        return this.getClass().getAnnotation(AtlasModuleDetail.class).name();
    }

    @Override
    public String[] getPackageNames() {
        return null;
    }

    @Override
    public long getSourceErrorCount() {
        return 0L;
    }

    @Override
    public long getSourceCount() {
        return 0L;
    }

    @Override
    public long getSourceMaxExecutionTime() {
        return 0L;
    }

    @Override
    public long getSourceMinExecutionTime() {
        return 0L;
    }

    @Override
    public long getSourceSuccessCount() {
        return 0L;
    }

    @Override
    public long getSourceTotalExecutionTime() {
        return 0L;
    }

    @Override
    public long getTargetCount() {
        return 0L;
    }

    @Override
    public long getTargetErrorCount() {
        return 0L;
    }

    @Override
    public long getTargetMaxExecutionTime() {
        return 0L;
    }

    @Override
    public long getTargetMinExecutionTime() {
        return 0L;
    }

    @Override
    public long getTargetSuccessCount() {
        return 0L;
    }

    @Override
    public long getTargetTotalExecutionTime() {
        return 0L;
    }

    @Override
    public String getUuid() {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public TabularData readAndResetStatistics() {
        return null;
    }

    @Override
    public void setStatisticsEnabled(boolean enabled) {
        LOG.warn("Statistics is not yet implemented");
    }

}
