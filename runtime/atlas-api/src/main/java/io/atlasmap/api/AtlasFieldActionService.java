/**
 * Copyright (C) 2017 Red Hat, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atlasmap.api;

import io.atlasmap.v2.ActionDetail;
import io.atlasmap.v2.Actions;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;

import java.util.List;

public interface AtlasFieldActionService {

    List<ActionDetail> listActionDetails();
    void processActions(Actions actions, Field field) throws AtlasException;
    Object processActions(Actions actions, Object sourceValue, FieldType targetType) throws AtlasException;

}
