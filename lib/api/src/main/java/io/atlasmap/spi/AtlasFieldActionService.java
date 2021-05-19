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

import io.atlasmap.api.AtlasException;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.ActionDetail;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;

public interface AtlasFieldActionService {

    List<ActionDetail> listActionDetails();
    ActionDetail findActionDetail(Action action, FieldType type) throws AtlasException;
    Field processActions(AtlasInternalSession session, Field field) throws AtlasException;
    ActionProcessor findActionProcessor(Action action, FieldType sourceType) throws AtlasException;

}
