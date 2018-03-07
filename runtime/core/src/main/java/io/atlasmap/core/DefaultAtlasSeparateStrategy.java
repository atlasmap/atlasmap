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
import io.atlasmap.spi.StringDelimiter;

public class DefaultAtlasSeparateStrategy implements AtlasSeparateStrategy {

    public static final Integer DEFAULT_SEPARATE_LIMIT = new Integer(512);
    public static final StringDelimiter DEFAULT_SEPARATE_DELIMITER = StringDelimiter.MULTI_SPACE;

    private StringDelimiter delimiter = DEFAULT_SEPARATE_DELIMITER;
    private Integer limit = DEFAULT_SEPARATE_LIMIT;

    @Override
    public StringDelimiter getDelimiter() {
        return delimiter;
    }

    @Override
    public void setDelimiter(StringDelimiter delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public Integer getLimit() {
        return limit;
    }

    @Override
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @Override
    public List<String> separateValue(String value) {
        return separateValue(value, getDelimiter(), getLimit());
    }

    @Override
    public List<String> separateValue(String value, StringDelimiter delimiter) {
        return separateValue(value, delimiter, getLimit());
    }

    @Override
    public List<String> separateValue(String value, StringDelimiter delimiter, Integer limit) {
        List<String> values = new ArrayList<String>();
        if (value == null || value.isEmpty()) {
            return values;
        }

        values.addAll(Arrays.asList(((String) value).split((delimiter == null ? DEFAULT_SEPARATE_DELIMITER.getRegex() : delimiter.getRegex()),
                (limit == null ? DEFAULT_SEPARATE_LIMIT : limit))));
        return values;
    }
}
