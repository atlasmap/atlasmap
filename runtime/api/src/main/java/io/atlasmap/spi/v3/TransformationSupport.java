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
package io.atlasmap.spi.v3;

import java.util.Set;

import io.atlasmap.api.v3.Mapping;
import io.atlasmap.api.v3.Message;
import io.atlasmap.spi.v3.util.AtlasException;

/**
 *
 */
public interface TransformationSupport extends DataHandlerSupport {

    /**
     * @return the mapping that provided this support
     */
    Mapping mapping();

    void autoSave();

    DataHandler handler(String id);

    BaseParameter parameterWithOutputProperty(String outputProperty);

    /**
     * @return all messages in this mapping's mapping document
     */
    Set<Message> documentMessages();

    void addOutputProperty(String outputProperty, BaseParameter parameter);

    /**
     * @param outputProperty
     */
    void removeOutputProperty(String outputProperty);

    /**
     * @param outputProperty
     * @param parameter
     */
    void addReferenceToOutputProperty(String outputProperty, BaseParameter parameter);

    /**
     * @param outputProperty
     * @param parameter
     */
    void removeReferenceToOutputPropertyReference(String outputProperty, BaseParameter parameter);

    /**
     * @param targetFieldReference
     * @param parameter
     * @throws AtlasException
     */
    void addTargetFieldReference(String targetFieldReference, BaseParameter parameter) throws AtlasException;

    /**
     * @param targetFieldReference
     * @param parameter
     */
    void removeTargetFieldReference(String targetFieldReference, BaseParameter parameter);

    /**
     *
     */
    void validate();
}
