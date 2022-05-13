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
package io.atlasmap.csv.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.ADMArchiveHandler;
import io.atlasmap.csv.core.CsvConfig;
import io.atlasmap.csv.core.CsvFieldReader;
import io.atlasmap.csv.v2.CsvDataSource;
import io.atlasmap.csv.v2.CsvInspectionRequest;
import io.atlasmap.csv.v2.CsvInspectionResponse;
import io.atlasmap.service.AtlasService;
import io.atlasmap.service.DocumentService;
import io.atlasmap.service.ModuleService;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.Document;
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
 * CSV Service provides CSV inspection service which generate an AtlasMap Document object from the CSV.
 */
@Path("/csv/")
public class CsvService extends ModuleService {

    private static final Logger LOG = LoggerFactory.getLogger(CsvService.class);

    /**
     * A constructor.
     * @param atlasService AtlasService
     * @param documentService DocumentService
     */
    public CsvService(AtlasService atlasService, DocumentService documentService) {
        super(atlasService, documentService);
        getDocumentService().registerModuleService(DocumentType.CSV, this);
    }

    /**
     * Imports a CSV instance and return a Document object.
     * @param requestIn request
     * @param mappingDefinitionId Mapping Definition ID
     * @param dataSourceType DataSourceType
     * @param documentId Document ID
     * @param uriInfo URI info
     * @return {@link CsvInspectionResponse}
     * @throws IOException unexpected error
     */
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/project/{mappingDefinitionId}/document/{dataSourceType}/{documentId}")
    @Operation(summary = "Import CSV", description = "Import a CSV instance and return a Document object")
    @RequestBody(description = "CsvInspectionRequest object",  content = @Content(schema = @Schema(implementation = CsvInspectionRequest.class)))
    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CsvInspectionResponse.class)),
        description = "Return a Document object"))
    public Response importCsvDocument(
            InputStream requestIn,
            @Parameter(description = "Mapping Definition ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId,
            @Parameter(description = "DataSource Type") @PathParam("dataSourceType") DataSourceType dataSourceType,
            @Parameter(description = "Document ID") @PathParam("documentId") String documentId,
            @Context UriInfo uriInfo
        ) throws IOException  {
        long startTime = System.currentTimeMillis();

        CsvInspectionRequest request = fromJson(requestIn, CsvInspectionRequest.class);
        DocumentMetadata metadata = createDocumentMetadataFrom(request, dataSourceType, documentId);
        CsvDataSource dataSource = createDataSource(metadata);
        Map<String,String> options = request.getOptions();
        CsvInspectionResponse response = new CsvInspectionResponse();
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Options: {}", options);
            }
            storeDocumentMetadata(mappingDefinitionId, metadata.getDataSourceType(), documentId, metadata, dataSource);
            InputStream specification = new ByteArrayInputStream(request.getCsvData().getBytes());
            storeDocumentSpecification(mappingDefinitionId, metadata.getDataSourceType(), documentId, specification);
            ADMArchiveHandler admHandler = getAtlasService().getADMArchiveHandler(mappingDefinitionId);
            DocumentKey docKey = new DocumentKey(metadata.getDataSourceType(), documentId);
            File specFile = admHandler.getDocumentSpecificationFile(docKey);
            performDocumentInspection(mappingDefinitionId, metadata, specFile);
            File f = admHandler.getDocumentInspectionResultFile(docKey);
            response.setCsvDocument(fromJson(new FileInputStream(f), Document.class));
        } catch (Exception e) {
            LOG.error("Error importing CSV: " + e.getMessage(), e);
            response.setErrorMessage(e.getMessage());
        } finally {
            response.setExecutionTime(System.currentTimeMillis() - startTime);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(("Response: {}" + new ObjectMapper().writeValueAsString(response)));
        }
        return Response.ok().entity(toJson(response)).build();
    }

    private CsvDataSource createDataSource(DocumentMetadata meta) {
        CsvDataSource answer = new CsvDataSource();
        answer.setDataSourceType(meta.getDataSourceType());
        answer.setId(meta.getId());
        answer.setName(meta.getName());
        answer.setDescription(meta.getDescription());
        StringBuffer uri = new StringBuffer("atlas:csv:");
        uri.append(meta.getId());
        if (!meta.getInspectionParameters().isEmpty()) {
            uri.append("?");
            boolean first = true;
            for (Map.Entry<String,String> entry : meta.getInspectionParameters().entrySet()) {
                if (!first) {
                    uri.append("&");
                }
                first = false;
                uri.append(entry.getKey());
                uri.append("=");
                uri.append(entry.getValue());
            }
        }
        answer.setUri(uri.toString());
        return answer;
    }

    @Override
    public void performDocumentInspection(Integer mappingDefinitionId, DocumentMetadata meta, File spec)
            throws AtlasException {
        try {
            CsvConfig csvConfig = CsvConfig.newConfig(meta.getInspectionParameters());
            CsvFieldReader csvFieldReader = new CsvFieldReader(csvConfig);
            csvFieldReader.setDocument(new FileInputStream(spec));
            Document document = csvFieldReader.readSchema();
            storeInspectionResult(mappingDefinitionId, meta.getDataSourceType(), meta.getId(), document);
        } catch (Exception e) {
            throw new AtlasException(e);
        }
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
        return CsvService.LOG;
    }

}
