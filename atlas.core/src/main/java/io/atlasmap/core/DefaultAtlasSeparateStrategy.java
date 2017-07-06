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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.atlasmap.spi.AtlasSeparateStrategy;

public class DefaultAtlasSeparateStrategy implements AtlasSeparateStrategy {

	private String delimiter = StringDelimiter.MULTISPACE.getValue();
	public Integer DEFAULT_SPLIT_LIMIT = new Integer(512);
	public String DEFAULT_SPLIT_DELIMITER = StringDelimiter.MULTISPACE.getValue();
	
	@Override
	public String getName() {
		return "DefaultAtlasSeparateStrategy";
	}
	
	@Override
	public String getDelimiter() {
		return delimiter;
	}

	@Override
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	@Override
	public List<String> separateValue(String value, String delimiter, Integer limit) {
		List<String> values = new ArrayList<String>();
		if(value == null || value.isEmpty()) {
			return values;
		}
		
		values.addAll(Arrays.asList(((String)value).split((delimiter == null ? DEFAULT_SPLIT_DELIMITER : delimiter), (limit == null ? DEFAULT_SPLIT_LIMIT : 512))));
		return values;
	}
}
