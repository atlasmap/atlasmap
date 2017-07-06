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
package io.atlasmap.mock.module;

import io.atlasmap.api.AtlasConversionService;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.BaseAtlasModule;
import io.atlasmap.spi.AtlasModuleDetail;
import io.atlasmap.spi.AtlasModuleMode;
import io.atlasmap.v2.Collection;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MockField;

import java.util.List;

@AtlasModuleDetail(name="MockModule", uri="atlas:mock", modes={"SOURCE", "TARGET"}, dataFormats={"mock"}, configPackages={"io.atlasmap.v2"})
public class MockModule extends BaseAtlasModule {

	@Override
    public void processPostExecution(AtlasSession arg0) throws AtlasException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void processPostValidation(AtlasSession arg0) throws AtlasException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void processPreExecution(AtlasSession arg0) throws AtlasException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void processPreValidation(AtlasSession arg0) throws AtlasException {
        // TODO Auto-generated method stub
        
    }

    @Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void processInputMapping(AtlasSession session, Mapping mapping) throws AtlasException {
        // TODO Auto-generated method stub
	}
	
	@Override
	public void processInputCollection(AtlasSession session, Collection collection) throws AtlasException {
	    // TODO Auto-generated method stub
	}
	
	@Override
	public void processOutputMapping(AtlasSession session, Mapping mapping) throws AtlasException {
	    // TODO Auto-generated method stub
	}
	
	@Override
	public void processOutputCollection(AtlasSession session, Collection collection) throws AtlasException {
	    // TODO Auto-generated method stub
	}

	@Override
	public AtlasModuleMode getMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMode(AtlasModuleMode atlasModuleMode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<AtlasModuleMode> listSupportedModes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean isStatisticsSupported() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean isStatisticsEnabled() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean isSupportedField(Field field) {
		if(field instanceof MockField) {
			return true;
		}
		return false;
	}

    @Override
    public AtlasConversionService getConversionService() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setConversionService(AtlasConversionService arg0) {
        // TODO Auto-generated method stub
        
    }
}
