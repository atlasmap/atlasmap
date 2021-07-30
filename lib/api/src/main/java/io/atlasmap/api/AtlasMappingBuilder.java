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
package io.atlasmap.api;

/**
 * An interface to define a custom mapping logic. User can implement this class and
 * define custom mapping logic in {@code #processMapping()}.
 *
 */
public interface AtlasMappingBuilder {

    /**
     * Define custom mapping logic. User can implement this interface and define
     * custom mapping logic in this method.
     */
    void process();

    /**
     * Set {@code AtlasSession}.
     * @param session {@code AtlasSession}
     * @throws AtlasException
     */
    void setAtlasSession(AtlasSession session) throws AtlasException;

}
