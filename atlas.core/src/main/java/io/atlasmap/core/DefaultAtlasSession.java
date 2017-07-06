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

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.v2.AtlasMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultAtlasSession implements AtlasSession {

	private AtlasContext atlasContext;
	private AtlasMapping atlasMapping;
	private Map<String, Object> properties;
	private List<Map<String, Object>> data;
	private Object input;
	private Object output;
	
	public DefaultAtlasSession() { initialize(); }
	
	protected void initialize() { properties = new ConcurrentHashMap<String, Object>(); 
								  data = new ArrayList<Map<String, Object>>(); }

    public AtlasContext getAtlasContext() { return atlasContext; }
    public void setAtlasContext(AtlasContext atlasContext) { this.atlasContext = atlasContext; }
    public AtlasMapping getAtlasMapping() { return atlasMapping; }
	public void setAtlasMapping(AtlasMapping atlasMapping) { this.atlasMapping = atlasMapping; }
	@Override
	public Object getInput() { return input; }
	@Override
	public Object getOutput() { return output; }
	@Override
	public void setInput(Object input) { this.input = input; }
	@Override
	public void setOutput(Object output) { this.output = output; }
	public Map<String, Object> getProperties() { return this.properties; }
	public List<Map<String, Object>> getData() { return data; }
	public void setData(List<Map<String, Object>> data) { this.data = data; }
	
}
