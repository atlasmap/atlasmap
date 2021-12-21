/*
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

import java.util.List;

/**
 * AtlasSeparateStrategy.
 * @deprecated COMBINE/SEPARATE mode has been deprecated. Use transformations with multiple field selection.
 */
@Deprecated
public interface AtlasSeparateStrategy {

    /**
     * Gets the name.
     * @return name
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Gets the delimiter.
     * @return delimiter
     */
    StringDelimiter getDelimiter();

    /**
     * Sets the delimiter.
     * @param delimiter delimiter
     */
    void setDelimiter(StringDelimiter delimiter);

    /**
     * Gets the limit.
     * @return limit
     */
    Integer getLimit();

    /**
     * Sets the limit.
     * @param limit limit
     */
    void setLimit(Integer limit);

    /**
     * Separates the value.
     * @param value value
     * @return separated
     */
    List<String> separateValue(String value);

    /**
     * Separates the value.
     * @param value value
     * @param delimiter delimiter
     * @return separated
     */
    List<String> separateValue(String value, StringDelimiter delimiter);

    /**
     * Separates the value.
     * @param value value
     * @param delimiter delimiter
     * @param limit limit
     * @return separated
     */
    List<String> separateValue(String value, StringDelimiter delimiter, Integer limit);
}
