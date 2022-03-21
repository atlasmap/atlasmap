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
package io.atlasmap.service;

import javax.ws.rs.Path;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides backend REST services for handling Documents.
 * @see AtlasService
 * @see MappingService
 */
@Path("/project/{mappingDefinitionId}/document")
public class DocumentService extends BaseAtlasService {
    private static final Logger LOG = LoggerFactory.getLogger(DocumentService.class);

    @Context
    private ResourceContext resourceContext;
    private AtlasService atlasService;

    /**
     * A constructor.
     */
    public DocumentService() {
        if (resourceContext == null) {
            throw new IllegalStateException("JAX-RS ResourceContext is not injected");
        }
        this.atlasService = resourceContext.getResource(AtlasService.class);
    }

    /**
     * A constructor.
     * @param parent parent {@link AtlasService}
     */
    public DocumentService(AtlasService parent) {
        this.atlasService = parent;
    }

}
