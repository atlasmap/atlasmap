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

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.ADMArchiveHandler;
import io.atlasmap.v2.BaseInspectionRequest;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.DocumentKey;
import io.atlasmap.v2.DocumentMetadata;
import io.atlasmap.v2.Field;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * The base class for module services which provides Document format specific
 * backend REST services.
 */
public abstract class ModuleService extends BaseAtlasService {
    private AtlasService atlasService;
    private DocumentService documentService;

    /**
     * A constructor.
     * @param atlasService AtlasService
     * @param documentService DocumentService
     */
    public ModuleService(AtlasService atlasService, DocumentService documentService) {
        this.atlasService = atlasService;
        this.documentService = documentService;
    }


    /**
     * Gets the Document Field that has the given path. If the field
     * is a COMPLEX field and the {@code recursive} is true, the children
     * are also filled.
     * @param path the field path
     * @param recursive set true to get a field tree including children
     * @return the field
     */
    public abstract Field getField(String path, boolean recursive);

    /**
     * Searches the Document Field that matches with the given keywords.
     * @param keywords keywords
     * @return matched fields
     */
    public abstract List<Field> searchFields(String keywords);

    /**
     * Perform Document inspection and store the inspection result.
     * @param mappingDefinitionId Mapping Definition ID
     * @param meta DocumentMetadata
     * @param spec Document Specification
     */
    public abstract void performDocumentInspection(Integer mappingDefinitionId, DocumentMetadata meta, File spec) throws AtlasException;

    /**
     * Gets the logger.
     * @return logger
     */
    protected abstract Logger getLogger();

    /**
     * Gets the {@link AtlasService}.
     * @return AtlasService
     */
    protected AtlasService getAtlasService() {
        return this.atlasService;
    }

    /**
     * Gets the {@link DocumentService}.
     * @return DocumentService
     */
    protected DocumentService getDocumentService() {
        return this.documentService;
    }

    /**
     * Creates the {@link DocumentMetadata} from {@link BaseInspectionRequest}.
     * @param request request
     * @return created
     */
    protected <T extends BaseInspectionRequest> DocumentMetadata createDocumentMetadataFrom(T request, DataSourceType dsType, String documentId) {
        DocumentMetadata answer = new DocumentMetadata();
        if (request.getDocumentId() == null || !request.getDocumentId().equals(documentId)) {
            getLogger().warn(
                "Document ID is not consistent - path parameter has '{}' while '{}' is in the InspectionRequest. '{}' will be used",
                documentId, request.getDocumentId(), documentId);
        }
        answer.setDataSourceType(dsType);
        answer.setId(documentId);
        answer.setName(request.getDocumentName());
        answer.setDescription(request.getDocumentDescription());
        answer.setDocumentType(request.getDocumentType());
        answer.setInspectionType(request.getInspectionType());
        answer.setInspectionParameters(request.getOptions());
        answer.setFieldNameExclusions(request.getFieldNameExclusions());
        answer.setTypeNameExclusions(request.getTypeNameExclusions());
        answer.setNamespaceExclusions(request.getNamespaceExclusions());
        return answer;
    }

    /**
     * Persists the Document Metadata. The combination of the {@link DocumentMetadata} and
     * the Document specification stored with {@link #storeDocumentSpecification(Integer,DataSourceType,String,InputStream)}
     * have to be a complete set for reproducing Document inspection.
     * This also sets the corresponding {@link DataSource} into the Mapping Definition.
     * @param mappingDefinitionId Mapping Definition ID
     * @param dsType DataSourceType indicating SOURCE or TARGET
     * @param documentId Document ID
     * @param metadata Document inspection request object, which contains Document metadata such as
     * Document ID/name/description, inspection parameters, etc
     */
    protected void storeDocumentMetadata(Integer mappingDefinitionId, DataSourceType dsType, String documentId,
            DocumentMetadata metadata, DataSource dataSource) {
        try {
            ADMArchiveHandler handler = atlasService.getADMArchiveHandler(mappingDefinitionId);
            DocumentKey docKey = new DocumentKey(dsType, documentId);
            handler.setDocumentMetadata(docKey, metadata);
            handler.getAtlasMappingHandler().setDataSource(docKey, dataSource);
            handler.persist();
        } catch (Exception e) {
            throw new WebApplicationException(
                    String.format("Failed to store a metadata of the Document %s:%s", mappingDefinitionId, documentId),
                    e);
        }
    }

    /**
     * Persists the Document specification such as JSON schema for the JSON Document. The combination of
     * the InspectionRequest stored with {@link #storeDocumentMetadata(Integer,DataSourceType,String,DocumentMetadata,DataSource)}
     *  and the Document specification have to be a complete set for reproducing Document inspection.
     * @param mappingDefinitionId Mapping Definition ID
     * @param dsType DataSourceType indicating SOURCE or TARGET
     * @param documentId Document ID
     * @param specification The Document specification to be inspected
     */
    protected void storeDocumentSpecification(Integer mappingDefinitionId, DataSourceType dsType, String documentId,
            InputStream specification) {
        try {
            ADMArchiveHandler handler = atlasService.getADMArchiveHandler(mappingDefinitionId);
            handler.setDocumentSpecification(new DocumentKey(dsType, documentId), specification);
            handler.persist();
        } catch (Exception e) {
            throw new WebApplicationException(
                    String.format("Failed to store a specification of the Document %s:%s", mappingDefinitionId,
                            documentId),
                    e);
        }
    }

    /**
     * Persists the Document inspection result as a JSON file. The <code>resultObject</code> argument
     * has to be serializable with Jackson.
     * @param mappingDefinitionId Mapping Definition ID
     * @param dsType DataSourceType indicating SOURCE or TARGET
     * @param documentId Document ID
     * @param resultObject Document inspection result object
     */
    protected void storeInspectionResult(Integer mappingDefinitionId, DataSourceType dsType, String documentId,
            Serializable resultObject) {
        try {
            ADMArchiveHandler handler = atlasService.getADMArchiveHandler(mappingDefinitionId);
            handler.setDocumentInspectionResult(new DocumentKey(dsType, documentId), resultObject);
            handler.persist();
        } catch (Exception e) {
            throw new WebApplicationException(
                    String.format("Failed to store an inspection result of the Document %s:%s", mappingDefinitionId,
                            documentId),
                    e);
        }
    }

    /**
     * Simple liveness check method used in liveness checks. Must not be protected via authetication.
     * @return pong
     */
    @GET
    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Ping", description = "Simple liveness check method used in liveness checks. Must not be protected via authetication.")
    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(type = "string")), description = "Return 'pong'"))
    public Response ping() {
        getLogger().debug("Ping...  responding with 'pong'.");
        return Response.ok().entity("pong").build();
    }

}
