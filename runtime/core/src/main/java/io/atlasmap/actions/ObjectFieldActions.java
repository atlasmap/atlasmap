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
package io.atlasmap.actions;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Contains;
import io.atlasmap.v2.FieldType;

public class ObjectFieldActions implements AtlasFieldAction {

    @AtlasFieldActionInfo(name = "Contains", sourceType = FieldType.ALL, targetType = FieldType.BOOLEAN, sourceCollectionType = CollectionType.ALL, targetCollectionType = CollectionType.NONE)
    public static Boolean contains(Action action, Object input) {
        if (action == null || !(action instanceof Contains)) {
            throw new IllegalArgumentException("Action must be a Contains action");
        }

        Contains contains = (Contains) action;

        if (contains.getValue() == null) {
            throw new IllegalArgumentException("Contains must be specfied with a value");
        }

        if (input == null) {
            return false;
        }
        if (input instanceof String) {
            return input.toString().contains(contains.getValue());
        }
        return false;
    }
}
