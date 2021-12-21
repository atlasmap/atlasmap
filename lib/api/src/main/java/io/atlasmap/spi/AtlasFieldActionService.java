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

/**
 * AtlasFieldActionService handles AtlasMap field action.
 */
public interface AtlasFieldActionService {

    /**
     * Gets a list of field action metadata {@link ActionDetail}.
     * @return a list of action detail
     */
    List<ActionDetail> listActionDetails();

    /**
     * Looks up an {@link ActionDetail} that corresponds to the specified action model object and field type.
     * @param action action model object
     * @param type field type
     * @return action detail
     * @throws AtlasException unexpected error
     */
    ActionDetail findActionDetail(Action action, FieldType type) throws AtlasException;

    /**
     * Processes field actions that declared on the specified field.
     * @param session session
     * @param field field
     * @return processed field
     * @throws AtlasException unexpected error
     */
    Field processActions(AtlasInternalSession session, Field field) throws AtlasException;

    /**
     * Looks up an {@link ActionProcessor} that corresponds to the specified action model mobject and field type.
     * @param action action model object
     * @param sourceType field type
     * @return action processor
     * @throws AtlasException unexpected error
     */
    ActionProcessor findActionProcessor(Action action, FieldType sourceType) throws AtlasException;

}
