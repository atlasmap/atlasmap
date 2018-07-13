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

import java.util.Map;

public interface AtlasCombineStrategy {

    default String getName() {
        return this.getClass().getSimpleName();
    }

    StringDelimiter getDelimiter();

    void setDelimiter(StringDelimiter delimiter);

    Integer getLimit();

    void setLimit(Integer limit);

    String combineValues(Map<Integer, String> values);

    String combineValues(Map<Integer, String> values, StringDelimiter delimiter);

    String combineValues(Map<Integer, String> values, StringDelimiter delimiter, Integer maxItems);

    String combineValues(Map<Integer, String> values, String delimiter);
}
