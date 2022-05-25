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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.ADMArchiveHandler;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.DocumentCatalog;
import io.atlasmap.v2.DocumentKey;
import io.atlasmap.v2.DocumentMetadata;
import io.atlasmap.v2.DocumentType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * Provides backend REST services for handling Documents.
 * @see AtlasService
 * @see MappingService
 */
@Path("/project/{mappingDefinitionId}/document")
public class DocumentService extends BaseAtlasService {
    private static final Logger LOG = LoggerFactory.getLogger(DocumentService.class);

    private AtlasService atlasService;
    private Map<DocumentType, ModuleService> moduleServices = new ConcurrentHashMap<>();

    /**
     * A constructor.
     * @param atlasService AtlasService
     */
    public DocumentService(AtlasService atlasService) {
        this.atlasService = atlasService;
    }

    /**
     * Retrieve a DocumentCatalog.
     * @param mappingDefinitionId mapping definition ID
     * @return DocumentCatalog
     */
    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Get DocumentCatalog", description = "Retrieve a Document catalog file saved on the server")
    @ApiResponses({
        @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = DocumentCatalog.class)), description = "Return a DocumentCatalog content"),
        @ApiResponse(responseCode = "204", description = "Document catalog file was not found"),
        @ApiResponse(responseCode = "500", description = "Document catalog file access error")})
    public Response getDocumentCatalogRequest(
      @Parameter(description = "Mapping Definition ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId) {
        LOG.debug("getDocumentCatalogRequest: {}", mappingDefinitionId);
        ADMArchiveHandler admHandler = this.atlasService.getADMArchiveHandler(mappingDefinitionId);

        byte[] serialized = null;
        try {
            serialized = admHandler.getSerializedDocumentCatalog();
        } catch (Exception e) {
            LOG.error("Error retrieving Document catalog file for ID:" + mappingDefinitionId, e);
            throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
        }
        if (LOG.isDebugEnabled() && serialized != null) {
            LOG.debug(new String(serialized));
        }
        if (serialized == null) {
            LOG.debug("Document catalog not found for ID:{}", mappingDefinitionId);
            return Response.noContent().build();
        }
        return Response.ok().entity(serialized).build();
    }

    @GET
    @Path("/{dataSourceType}/{documentId}/inspected")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Get Document inspection result", description = "Get the Document inspection result")
    @ApiResponses({
        @ApiResponse(responseCode = "404", description = "Document inspection result was not found"),
        @ApiResponse(responseCode = "500", description = "Document inspection result access error")})
    public Response getDocumentInspectionResultRequest(
      @Parameter(description = "Mapping Definition ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId,
      @Parameter(description = "DataSource Type") @PathParam("dataSourceType") DataSourceType dataSourceType,
      @Parameter(description = "Document ID") @PathParam("documentId") String documentId
      ) {
        LOG.debug("getDocumentInspectionResultRequest: {}:{}:{}", mappingDefinitionId, dataSourceType, documentId);
        try {
            ADMArchiveHandler admHandler = this.atlasService.getADMArchiveHandler(mappingDefinitionId);
            DocumentKey docKey = new DocumentKey(dataSourceType, documentId);
            File f = admHandler.getDocumentInspectionResultFile(docKey);
            if (f == null || !f.exists()) {
                DocumentMetadata meta = admHandler.getDocumentMetadata(docKey);
                File spec = admHandler.getDocumentSpecificationFile(docKey);
                ModuleService mod = moduleServices.get(meta.getDocumentType());
                if (mod == null) {
                    throw new WebApplicationException(String.format("Unsupported Document type '%s'", meta.getDocumentType()));
                }
                mod.performDocumentInspection(mappingDefinitionId, meta, spec);
                f = admHandler.getDocumentInspectionResultFile(docKey);
            }
            return Response.ok(f).build();
        } catch (AtlasException e) {
            throw new WebApplicationException(
                String.format("Failed to get the Document inspection result %s:%s:%s", mappingDefinitionId, dataSourceType, documentId),
                e);
        }
    }

    /**
     * Set the Document Name of an existing Document.
     * @param mappingDefinitionId mapping definition ID
     * @param dataSourceType DataSourceType
     * @param documentId Document ID
     * @param name Document Name to set
     * @return no content
     */
    @PUT
    @Path("/{dataSourceType}/{documentId}/name")
    @Produces({MediaType.TEXT_PLAIN})
    @Operation(summary = "Set Document Name", description = "Set the Document Name of an existing Document")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Document was not found"),
        @ApiResponse(responseCode = "500", description = "Document access error")})
    public Response setDocumentNameRequest(
      @Parameter(description = "Mapping Definition ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId,
      @Parameter(description = "DataSource Type") @PathParam("dataSourceType") DataSourceType dataSourceType,
      @Parameter(description = "Document ID") @PathParam("documentId") String documentId,
      String name
      ) {
        LOG.debug("setDocumentNameRequest: {}:{}:{}:{}", mappingDefinitionId, dataSourceType, documentId, name);
        try {
            ADMArchiveHandler admHandler = this.atlasService.getADMArchiveHandler(mappingDefinitionId);
            DocumentCatalog catalog = admHandler.getDocumentCatalog();
            Optional<DocumentMetadata> meta =
                (dataSourceType == DataSourceType.SOURCE ? catalog.getSources() : catalog.getTargets())
                    .stream().filter((m -> m.getId().equals(documentId))).findAny();
            if (!meta.isPresent()) {
                return Response.status(Status.NOT_FOUND).build();
            }
            meta.get().setName(name);
            admHandler.persist();
        } catch (AtlasException e) {
            throw new WebApplicationException(
                String.format("Failed to set the Document name %s:%s:%s:%s", mappingDefinitionId, dataSourceType, documentId, name),
                e);
        }
        return Response.ok().build();
    }

    /**
     * Delete the Document and the mappings that refer to the Document being deleted.
     * @param mappingDefinitionId mapping definition ID
     * @param dataSourceType DataSourceType
     * @param documentId Document ID
     * @return no content
     */
    @DELETE
    @Path("/{dataSourceType}/{documentId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Delete Document", description = "Delete the Document")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Document was not found"),
        @ApiResponse(responseCode = "500", description = "Document access error")})
    public Response deleteDocumentRequest(
      @Parameter(description = "Mapping Definition ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId,
      @Parameter(description = "DataSource Type") @PathParam("dataSourceType") DataSourceType dataSourceType,
      @Parameter(description = "Document ID") @PathParam("documentId") String documentId
      ) {
        LOG.debug("deleteDocumentRequest: {}:{}:{}", mappingDefinitionId, dataSourceType, documentId);
        try {
            ADMArchiveHandler admHandler = this.atlasService.getADMArchiveHandler(mappingDefinitionId);
            DocumentKey docKey = new DocumentKey(dataSourceType, documentId);
            admHandler.deleteDocument(docKey);
            admHandler.getAtlasMappingHandler().removeDocumentReference(docKey);
            admHandler.persist();
        } catch (AtlasException e) {
            throw new WebApplicationException(
                String.format("Failed to delete the Document %s:%s:%s", mappingDefinitionId, dataSourceType, documentId),
                e);
        }
        return Response.ok().build();
    }

    /**
     * Registers the {@link ModuleService} with the its ID as a key.
     * @param documentType Document Type
     * @param instance service
     */
    public void registerModuleService(DocumentType documentType, ModuleService instance) {
        moduleServices.put(documentType, instance);
    }

}
