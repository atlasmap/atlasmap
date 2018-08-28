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
package io.atlasmap.spi;

import java.util.List;

import io.atlasmap.api.AtlasException;
import io.atlasmap.v2.Field;

public interface AtlasModule {

    void init();

    void destroy();

    void processPreValidation(AtlasInternalSession session) throws AtlasException;

    void processPreSourceExecution(AtlasInternalSession session) throws AtlasException;

    void processPreTargetExecution(AtlasInternalSession session) throws AtlasException;

    void processSourceFieldMapping(AtlasInternalSession session) throws AtlasException;

    void processTargetFieldMapping(AtlasInternalSession session) throws AtlasException;

    void processPostSourceExecution(AtlasInternalSession session) throws AtlasException;

    void processPostTargetExecution(AtlasInternalSession session) throws AtlasException;

    void processPostValidation(AtlasInternalSession session) throws AtlasException;

    AtlasModuleMode getMode();

    void setMode(AtlasModuleMode atlasModuleMode);

    AtlasConversionService getConversionService();

    void setConversionService(AtlasConversionService atlasConversionService);

    AtlasFieldActionService getFieldActionService();

    void setFieldActionService(AtlasFieldActionService atlasFieldActionService);

    List<AtlasModuleMode> listSupportedModes();

    String getDocId();

    void setDocId(String docId);

    String getUri();

    void setUri(String uri);

    Boolean isStatisticsSupported();

    Boolean isStatisticsEnabled();

    Boolean isSupportedField(Field field);

    Field cloneField(Field field) throws AtlasException;

}
