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
package io.atlasmap.api;

import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.Audits;
import io.atlasmap.v2.Validations;
import java.util.Map;

public interface AtlasSession {

	Map<String, Object> getProperties();
	AtlasContext getAtlasContext();
	void setAtlasContext(AtlasContext atlasContext);
	AtlasMapping getMapping();
	Object getInput();
	void setInput(Object inputObject);
    Object getInput(String docId);
    void setInput(Object inputObject, String docId);
	Object getOutput();
	void setOutput(Object outputObject);
	Object getOutput(String docId);
	void setOutput(Object outputObject, String docId);
	Validations getValidations();
	void setValidations(Validations validations);
	Audits getAudits();
	void setAudits(Audits audits);
	boolean hasErrors();
	boolean hasWarns();
	Integer errorCount();
	Integer warnCount();
}
