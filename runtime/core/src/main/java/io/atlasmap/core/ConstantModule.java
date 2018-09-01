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

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;
import io.atlasmap.spi.AtlasConversionService;
import io.atlasmap.spi.AtlasFieldActionService;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasModule;
import io.atlasmap.spi.AtlasModuleDetail;
import io.atlasmap.spi.AtlasModuleMode;
import io.atlasmap.v2.ConstantField;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;

@AtlasModuleDetail(name = "ConstantModule", uri = "", modes = { "SOURCE" }, dataFormats = {}, configPackages = {})
public class ConstantModule implements AtlasModule {
    private static final Logger LOG = LoggerFactory.getLogger(ConstantModule.class);

    private AtlasConversionService conversionService;
    private AtlasFieldActionService fieldActionService;

    @Override
    public void init() {
        // no-op
    }

    @Override
    public void destroy() {
        // no-op
    }

    @Override
    public void processPreValidation(AtlasInternalSession session) throws AtlasException {
        // no-op
    }

    @Override
    public void processPreSourceExecution(AtlasInternalSession session) throws AtlasException {
        // no-op
    }

    @Override
    public void processSourceFieldMapping(AtlasInternalSession session) throws AtlasException {
        Field sourceField = session.head().getSourceField();
        if (!(sourceField instanceof ConstantField)) {
            return;
        }
        if (getConversionService() != null && sourceField.getFieldType() != null
                && sourceField.getValue() != null) {
            sourceField.setValue(getConversionService().convertType(sourceField.getValue(),
                    null, getConversionService().classFromFieldType(sourceField.getFieldType()), null));
        } else if (sourceField.getFieldType() == null) {
            sourceField.setFieldType(FieldType.STRING);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Processed source ConstantField sPath=" + sourceField.getPath() + " sV="
                    + sourceField.getValue() + " sT=" + sourceField.getFieldType() + " docId: " + sourceField.getDocId());
        }
    }

    @Override
    public void processPostSourceExecution(AtlasInternalSession session) throws AtlasException {
        // no-op
    }

    @Override
    public void processPreTargetExecution(AtlasInternalSession session) throws AtlasException {
        throw new UnsupportedOperationException("ConstantField cannot be placed as a target field");
    }

    @Override
    public void processTargetFieldMapping(AtlasInternalSession session) throws AtlasException {
        throw new UnsupportedOperationException("ConstantField cannot be placed as a target field");
    }

    @Override
    public void processPostTargetExecution(AtlasInternalSession session) throws AtlasException {
        throw new UnsupportedOperationException("ConstantField cannot be placed as a target field");
    }

    @Override
    public void processPostValidation(AtlasInternalSession session) throws AtlasException {
        // no-op
    }

    @Override
    public AtlasModuleMode getMode() {
        return AtlasModuleMode.SOURCE;
    }

    @Override
    public void setMode(AtlasModuleMode atlasModuleMode) {
        // no-op
    }

    @Override
    public AtlasConversionService getConversionService() {
        return this.conversionService;
    }

    @Override
    public void setConversionService(AtlasConversionService atlasConversionService) {
        this.conversionService = atlasConversionService;
    }

    @Override
    public List<AtlasModuleMode> listSupportedModes() {
        return Arrays.asList(new AtlasModuleMode[] { AtlasModuleMode.SOURCE });
    }

    @Override
    public String getDocId() {
        return null;
    }

    @Override
    public void setDocId(String docId) {
        // no-op
    }

    @Override
    public String getUri() {
        return null;
    }

    @Override
    public void setUri(String uri) {
        // no-op
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
        return field instanceof ConstantField;
    }

    @Override
    public Field cloneField(Field field) throws AtlasException {
        return null;
    }

    @Override
    public AtlasFieldActionService getFieldActionService() {
        return this.fieldActionService;
    }

    @Override
    public void setFieldActionService(AtlasFieldActionService atlasFieldActionService) {
        this.fieldActionService = atlasFieldActionService;
    }

}
