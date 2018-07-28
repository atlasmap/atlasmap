/**
 * Copyright (C) 2018 Red Hat, Inc.
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
package io.atlasmap.core.transformation;

import io.atlasmap.api.v3.Parameter.Role;
import io.atlasmap.api.v3.ValueType;
import io.atlasmap.spi.v3.BaseParameter;
import io.atlasmap.spi.v3.BaseTransformation;
import io.atlasmap.spi.v3.util.AtlasException;
import io.atlasmap.spi.v3.util.I18n;

public class MapTransformation extends BaseTransformation {

    public static final String NAME = I18n.localize("Map");
    public static final String FROM_PARAMETER = I18n.localize("From");

    public static final String TO_PARAMETER = I18n.localize("To");

    private final transient BaseParameter fromParameter;
    private final transient BaseParameter toParameter;

    public MapTransformation() {
        super(NAME, "Maps a source field, property, or constant to a target field");
        fromParameter = addParameter(new BaseParameter(this, FROM_PARAMETER, Role.INPUT, ValueType.ANY, false, false,
                                                       "A source field, property, or constant from which to map"));
        toParameter = addParameter(new BaseParameter(this, TO_PARAMETER, Role.OUTPUT, ValueType.ANY, false, false,
                                                     "A target field or property to which to map"));
    }

    /**
     * @see BaseTransformation#execute()
     */
    @Override
    protected void execute() throws AtlasException {
        toParameter.setOutputValue(fromParameter.value());
    }
}
