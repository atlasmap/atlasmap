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
package io.atlasmap.javapath;

import java.util.ArrayList;
import java.util.List;

public class JavaPath {

	private List<String> segments = new ArrayList<String>();
	public static final String JAVAPATH_SEPARATOR = "."; 
	public static final String JAVAPATH_SEPARATOR_ESCAPTED = "\\."; 

	
	public JavaPath() {}
	
	public JavaPath(String javaPath) {
		if(javaPath != null) {
			if(javaPath.contains(JAVAPATH_SEPARATOR)) {
				String[] parts = javaPath.split(JAVAPATH_SEPARATOR_ESCAPTED, 512);
				for(String part : parts) {
					getSegments().add(part);
				}
			}
		}
	}
	
	public JavaPath appendField(String fieldName) {
		this.segments.add(fieldName);
		return this;
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		
		int i=0;
		for(String part : getSegments()) {
			buffer.append(part);
			if(i < (getSegments().size()-1)) {
				buffer.append(JAVAPATH_SEPARATOR);
			}
			i++;
		}
		return buffer.toString();
	}
	
	public List<String> getSegments() {
		return this.segments;
	}
}
