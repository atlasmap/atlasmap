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
package io.atlasmap.json.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.ADMArchiveHandler;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.json.inspect.JsonInspectionService;
import io.atlasmap.json.v2.JsonDataSource;
import io.atlasmap.json.v2.JsonDocument;
import io.atlasmap.json.v2.JsonInspectionRequest;
import io.atlasmap.json.v2.JsonInspectionResponse;
import io.atlasmap.service.AtlasService;
import io.atlasmap.service.DocumentService;
import io.atlasmap.service.ModuleService;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.DocumentKey;
import io.atlasmap.v2.DocumentMetadata;
import io.atlasmap.v2.DocumentType;
import io.atlasmap.v2.Field;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * The JSON Service provides JSON inspection service which generate an AtlasMap Document object from JSON instance or JSON schema.
 */
@Path("/json/")
public class JsonService extends ModuleService {

    private static final Logger LOG = LoggerFactory.getLogger(JsonService.class);

    /**
     * A constructor.
     * @param atlasService AtlasService
     * @param documentService DocumentService
     */
    public JsonService(AtlasService atlasService, DocumentService documentService) {
        super(atlasService, documentService);
        getDocumentService().registerModuleService(DocumentType.JSON, this);
    }

    /**
     * Import a JSON schema or instance and return a Document object.
     * @param requestIn request
     * @param mappingDefinitionId Mapping Definition ID
     * @param dataSourceType DataSourceType
     * @param documentId Document ID
     * @param uriInfo URI info
     * @return {@link JsonInspectionResponse}
     */
    @POST
    @Path("/project/{mappingDefinitionId}/document/{dataSourceType}/{documentId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary ="Import JSON Document", description = "Import a JSON schema or instance and return a Document object")
    @RequestBody(description = "JsonInspectionRequest object",  content = @Content(schema = @Schema(implementation = JsonInspectionRequest.class)))
    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = JsonInspectionResponse.class)), description = "Return a Document object represented by JsonDocument"))
    public Response importJsonDocument(
            InputStream requestIn,
            @Parameter(description = "Mapping Definition ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId,
            @Parameter(description = "DataSource Type") @PathParam("dataSourceType") DataSourceType dataSourceType,
            @Parameter(description = "Document ID") @PathParam("documentId") String documentId,
            @Context UriInfo uriInfo
        ) {
        long startTime = System.currentTimeMillis();

        JsonInspectionRequest request = fromJson(requestIn, JsonInspectionRequest.class);
        JsonInspectionResponse response = new JsonInspectionResponse();
        JsonDocument d = null;

        try {
            if (request.getInspectionType() == null || request.getJsonData() == null) {
                response.setErrorMessage(
                        "Json data and Instance or Schema inspection type must be specified in request");
                return Response.ok().entity(toJson(response)).build();
            }
            String jsonData = cleanJsonData(request.getJsonData());
            if (!validJsonData(jsonData)) {
                response.setErrorMessage("Invalid json payload specified");
                return Response.ok().entity(toJson(response)).build();
            }
            DocumentMetadata metadata = createDocumentMetadataFrom(request, dataSourceType, documentId);
            JsonDataSource dataSource = createDataSource(metadata);
            storeDocumentMetadata(mappingDefinitionId, dataSourceType, documentId, metadata, dataSource);
            storeDocumentSpecification(mappingDefinitionId, dataSourceType, documentId, new ByteArrayInputStream(request.getJsonData().getBytes()));
            ADMArchiveHandler admHandler = getAtlasService().getADMArchiveHandler(mappingDefinitionId);
            DocumentKey docKey = new DocumentKey(dataSourceType, documentId);
            File specFile = admHandler.getDocumentSpecificationFile(docKey);
            performDocumentInspection(mappingDefinitionId, metadata, specFile);
            File f = admHandler.getDocumentInspectionResultFile(docKey);
            d = fromJson(new FileInputStream(f), JsonDocument.class);
        } catch (Exception e) {
            LOG.error("Error importing JSON: " + e.getMessage(), e);
            response.setErrorMessage(e.getMessage());
        } finally {
            response.setExecutionTime(System.currentTimeMillis() - startTime);
        }

        AtlasUtil.excludeNotRequestedFields(d, request.getInspectPaths(), request.getSearchPhrase());
        response.setJsonDocument(d);
        return Response.ok().entity(toJson(response)).build();
    }

    private JsonDataSource createDataSource(DocumentMetadata meta) {
        JsonDataSource answer = new JsonDataSource();
        answer.setDataSourceType(meta.getDataSourceType());
        answer.setId(meta.getId());
        answer.setName(meta.getName());
        answer.setDescription(meta.getDescription());
        StringBuffer uri = new StringBuffer("atlas:json:");
        uri.append(meta.getId());
        answer.setUri(uri.toString());
        return answer;
    }

    @Override
    public void performDocumentInspection(Integer mappingDefinitionId, DocumentMetadata meta, File spec) throws AtlasException {
        JsonInspectionService s = new JsonInspectionService();

        JsonDocument d;
        try {
            switch (meta.getInspectionType()) {
            case INSTANCE:
                d = s.inspectJsonDocument(new FileInputStream(spec));
                break;
            case SCHEMA:
                d = s.inspectJsonSchema(new FileInputStream(spec));
                break;
            default:
                throw new AtlasException("Unsupported inspection type: " + meta.getInspectionType());
            }
        } catch (FileNotFoundException e) {
            throw new AtlasException(e);
        }
        if (d != null) {
            storeInspectionResult(mappingDefinitionId, meta.getDataSourceType(), meta.getId(), d);
        }
    }

    /**
     * Gets if the JSON data is valid.
     * @param jsonData json data
     * @return true if valid, or false
     */
    protected boolean validJsonData(String jsonData) {
        if (jsonData == null || jsonData.isEmpty()) {
            return false;
        }

        return jsonData.startsWith("{") || jsonData.startsWith("[");
    }

    /**
     * Trims the String.
     * @param jsonData String
     * @return trimmed
     */
    protected String cleanJsonData(String jsonData) {
        return jsonData.trim();
    }

    @Override
    public Field getField(String path, boolean recursive) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Field> searchFields(String keywords) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Logger getLogger() {
        return JsonService.LOG;
    }

}
