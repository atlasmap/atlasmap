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

import io.atlasmap.api.AtlasConversionService;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.v2.Collection;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.Mapping;
import java.util.List;

public interface AtlasModule {

	void init();
	void destroy();
	void processPreValidation(AtlasSession session) throws AtlasException;
	void processPreInputExecution(AtlasSession session) throws AtlasException;
    void processInputMapping(AtlasSession session, Mapping mapping) throws AtlasException;
    void processInputCollection(AtlasSession session, Collection mapping) throws AtlasException;
	void processInputActions(AtlasSession session, Mapping mapping) throws AtlasException;
	void processPostInputExecution(AtlasSession session) throws AtlasException;
	void processPreOutputExecution(AtlasSession session) throws AtlasException;
    void processOutputMapping(AtlasSession session, Mapping mapping) throws AtlasException;
    void processOutputCollection(AtlasSession session, Collection mapping) throws AtlasException;
	void processOutputActions(AtlasSession session, Mapping mapping) throws AtlasException;
	void processPostOutputExecution(AtlasSession session) throws AtlasException;
	void processPostValidation(AtlasSession session) throws AtlasException;
	AtlasModuleMode getMode();
	void setMode(AtlasModuleMode atlasModuleMode);
	AtlasConversionService getConversionService();
	void setConversionService(AtlasConversionService atlasConversionService);
	List<AtlasModuleMode> listSupportedModes();
	Boolean isStatisticsSupported();
	Boolean isStatisticsEnabled();
	Boolean isSupportedField(Field field);

}
