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

import io.atlasmap.api.AtlasException;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.ActionDetail;

/**
 * The field action processor.
 */
public interface ActionProcessor {

    /**
     * Gets {@link ActionDetail}.
     * @return field action detail
     */
    ActionDetail getActionDetail();

    /**
     * Gets the field action model class.
     * @return field action model class
     */
    Class<? extends Action> getActionClass();

    /**
     * Process the field action.
     * @param action field action model class
     * @param sourceObject source object
     * @return result
     * @throws AtlasException unexpected error
     */
    Object process(Action action, Object sourceObject) throws AtlasException;

}
