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

import io.atlasmap.v2.Field;

public interface AtlasCollectionHelper {

    /**
     * Determines target collection count based on path and actions.
     * @param targetField target field
     * @return target collection count
     */
    int determineTargetCollectionCount(Field targetField);

    /**
     * Determines source collection count based on path and actions.
     * @param sourceParentField source parent field
     * @param sourceField source field
     * @return source collection count
     */
    int determineSourceCollectionCount(Field sourceParentField, Field sourceField);

    /**
     * Copies over collection indexes from source to target applying adjustments if collection counts differ.
     *
     * @param sourceParentField source parent field
     * @param sourceField source field
     * @param targetField target field
     * @param previousTargetField previous target fiend
     */
    void copyCollectionIndexes(Field sourceParentField, Field sourceField, Field targetField, Field previousTargetField);

}
