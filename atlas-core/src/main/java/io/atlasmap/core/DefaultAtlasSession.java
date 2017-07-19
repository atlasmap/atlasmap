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

import io.atlasmap.api.AtlasConstants;
import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.Audit;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.Audits;
import io.atlasmap.v2.Validations;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultAtlasSession implements AtlasSession {

	private AtlasContext atlasContext;
	private final AtlasMapping mapping;
	private Audits audits;
	private Validations validations;
	private Map<String, Object> properties;
	private Map<String, Object> inputMap = new HashMap<String, Object>();
	private Map<String, Object> outputMap = new HashMap<String, Object>();
		
	public DefaultAtlasSession(AtlasMapping mapping) { 
	    initialize();
	    this.mapping = mapping;
	}
	
	protected void initialize() { properties = new ConcurrentHashMap<String, Object>(); 
								 validations = new Validations();
								 audits = new Audits();}

    public AtlasContext getAtlasContext() { return atlasContext; }
    public void setAtlasContext(AtlasContext atlasContext) { this.atlasContext = atlasContext; }
    public AtlasMapping getMapping() { return mapping; }
	@Override
    public Validations getValidations() { return this.validations; }
    @Override
    public void setValidations(Validations validations) { this.validations = validations; }
    @Override
    public Audits getAudits() { return this.audits; }
    @Override
    public void setAudits(Audits audits) { this.audits = audits; }
    @Override
	public Object getInput() { return inputMap.get(AtlasConstants.DEFAULT_SOURCE_DOC_ID); }
	@Override
    public Object getInput(String docId) { return inputMap.get(docId); }
    @Override
	public Object getOutput() { return outputMap.get(AtlasConstants.DEFAULT_TARGET_DOC_ID); }
	@Override
    public Object getOutput(String docId) { return outputMap.get(docId); }
    @Override
	public void setInput(Object input) { this.inputMap.put(AtlasConstants.DEFAULT_SOURCE_DOC_ID, input); }
	@Override
    public void setInput(Object inputObject, String docId) { this.inputMap.put(docId, inputObject); }
    @Override
    public void setOutput(Object output) { this.outputMap.put(AtlasConstants.DEFAULT_TARGET_DOC_ID, output); }
	@Override
    public void setOutput(Object outputObject, String docId) { this.outputMap.put(docId, outputObject); }
    public Map<String, Object> getProperties() { return this.properties; }

    @Override
    public Integer errorCount() {
        int e=0;
        for(Audit audit : getAudits().getAudit()) {
            if(AuditStatus.ERROR.equals(audit.getStatus())) {
                e++;
            }
        }
        return e;
    }

    @Override
    public boolean hasErrors() {
        for(Audit audit : getAudits().getAudit()) {
            if(AuditStatus.ERROR.equals(audit.getStatus())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasWarns() {
        for(Audit audit : getAudits().getAudit()) {
            if(AuditStatus.WARN.equals(audit.getStatus())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Integer warnCount() {
        int w=0;
        for(Audit audit : getAudits().getAudit()) {
            if(AuditStatus.WARN.equals(audit.getStatus())) {
                w++;
            }
        }
        return w;
    }
    
}
