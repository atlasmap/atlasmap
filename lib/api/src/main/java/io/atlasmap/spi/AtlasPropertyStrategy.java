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

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.api.AtlasUnsupportedException;
import io.atlasmap.v2.PropertyField;

/**
 * A plug-in interface for property strategy which read a source property and write a target property.
 *
 */
public interface AtlasPropertyStrategy {

    /**
     * Read a source property value and set into source Field.
     * @param session {@code AtlasSession}
     * @param propertyField source {@code PropertyField} to set a property value
     * @throws AtlasUnsupportedException if reading property is not supported
     * @throws AtlasConversionException if type conversion fails
     */
    void readProperty(AtlasSession session, PropertyField propertyField) throws AtlasUnsupportedException, AtlasConversionException;

    /**
     * Write a target property value from target Field.
     * @param session {@code AtlasSession}
     * @param propertyField target {@code PropertyField} to read a property value from
     * @throws AtlasUnsupportedException if reading property is not supported
     * @throws AtlasConversionException if type conversion fails
     */
    void writeProperty(AtlasSession session, PropertyField propertyField) throws AtlasUnsupportedException, AtlasConversionException;

}
