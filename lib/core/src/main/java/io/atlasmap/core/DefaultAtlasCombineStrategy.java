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

import java.util.Map;
import java.util.TreeMap;

import io.atlasmap.spi.AtlasCombineStrategy;
import io.atlasmap.spi.StringDelimiter;

public class DefaultAtlasCombineStrategy implements AtlasCombineStrategy {

    public static final StringDelimiter DEFAULT_COMBINE_DELIMITER = StringDelimiter.SPACE;
    public static final Integer DEFAULT_COMBINE_LIMIT = 512;
    private StringDelimiter delimiter = DEFAULT_COMBINE_DELIMITER;
    private Integer limit = DEFAULT_COMBINE_LIMIT;
    private boolean disableAutoTrim = false;
    private boolean disableAddDelimiterOnNull = false;

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

    public boolean isDisableAutoTrim() {
        return disableAutoTrim;
    }

    public void setDisableAutoTrim(boolean disableAutoTrim) {
        this.disableAutoTrim = disableAutoTrim;
    }

    public boolean isDisableAddDelimiterOnNull() {
        return disableAddDelimiterOnNull;
    }

    public void setDisableAddDelimiterOnNull(boolean disableAddDelimiterOnNull) {
        this.disableAddDelimiterOnNull = disableAddDelimiterOnNull;
    }

    @Override
    public String combineValues(Map<Integer, String> values) {
        return combineValues(values, getDelimiter(), getLimit());
    }

    @Override
    public String combineValues(Map<Integer, String> values, StringDelimiter delimiter) {
        return combineValues(values, delimiter, getLimit());
    }

    @Override
    public String combineValues(Map<Integer, String> values, StringDelimiter delimiter, Integer limit) {
        if (values == null || values.isEmpty()) {
            return null;
        }

        String combinedString = "";
        if (values.size() == 1) {
            combinedString = values.get(0);
            return combinedString;
        }

        Map<Integer, String> sortedMap = sortByKey(values);
        Integer last = sortedMap.keySet().toArray(new Integer[0])[sortedMap.size()-1];

        boolean first = true;
        int count = 0;
        for (int i=0; i<=last; i++) {
            String value = sortedMap.get(i);
            if (first) {
                first = false;
                if (value == null) {
                    continue;
                } else if (isDisableAutoTrim()) {
                    combinedString = combinedString.concat(value);
                } else {
                    combinedString = combinedString.concat(value.trim());
                }
            } else {
                if (value == null) {
                    if (!disableAddDelimiterOnNull) {
                        combinedString = combinedString.concat((delimiter != null ? delimiter.getValue() : DEFAULT_COMBINE_DELIMITER.getValue()));
                    }
                } else if (isDisableAutoTrim()) {
                    combinedString = combinedString.concat((delimiter != null ? delimiter.getValue() : DEFAULT_COMBINE_DELIMITER.getValue()))
                            .concat(value);
                } else {
                    combinedString = combinedString.concat((delimiter != null ? delimiter.getValue() : DEFAULT_COMBINE_DELIMITER.getValue()))
                            .concat(value.trim());
                }
            }

            count++;
            if (count >= (limit != null ? limit : DEFAULT_COMBINE_LIMIT)) {
                break;
            }
        }

        return combinedString;
    }

    @Override
    public String combineValues(Map<Integer, String> values, String delimiter) {
        return null;
    }

    protected static Map<Integer, String> sortByKey(Map<Integer, String> map) {
        TreeMap<Integer, String> treeMap = new TreeMap<>((key1, key2) -> {
            if (key1 == null && key2 == null) {
                return 0;
            } else if (key1 == null) {
                return -1;// 1 or -1; whatever, the null value can be retrieved only with .get(null)
            } else if (key2 == null) {
                return 1;
            } else {
                return key1.compareTo(key2);
            }
        });
        treeMap.putAll(map);
        return treeMap;
    }
}
