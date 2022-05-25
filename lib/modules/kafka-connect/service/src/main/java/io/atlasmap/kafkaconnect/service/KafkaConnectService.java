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
package io.atlasmap.kafkaconnect.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
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
import io.atlasmap.kafkaconnect.core.KafkaConnectUtil;
import io.atlasmap.kafkaconnect.inspect.KafkaConnectInspectionService;
import io.atlasmap.kafkaconnect.v2.KafkaConnectConstants;
import io.atlasmap.kafkaconnect.v2.KafkaConnectDocument;
import io.atlasmap.kafkaconnect.v2.KafkaConnectInspectionRequest;
import io.atlasmap.kafkaconnect.v2.KafkaConnectInspectionResponse;
import io.atlasmap.kafkaconnect.v2.KafkaConnectSchemaType;
import io.atlasmap.service.AtlasService;
import io.atlasmap.service.DocumentService;
import io.atlasmap.service.ModuleService;
import io.atlasmap.v2.DataSource;
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
 * Kafka Connect Service provides Kafka Connect schema inspection service which generate an AtlasMap Document object from
 * Kafka Connect schema such as JSON or AVRO.
 */
@Path("/kafkaconnect/")
public class KafkaConnectService extends ModuleService {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConnectService.class);

    /**
     * A constructor.
     * @param atlasService AtlasService
     * @param documentService DocumentService
     */
    public KafkaConnectService(AtlasService atlasService, DocumentService documentService) {
        super(atlasService, documentService);
        getDocumentService().registerModuleService(DocumentType.KAFKA_AVRO, this);
        getDocumentService().registerModuleService(DocumentType.KAFKA_JSON, this);
    }

    /**
     * Import a Kafka Connect schema and return a Document object.
     * @param request {@link KafkaConnectInspectionRequest}
     * @param mappingDefinitionId Mapping Definition ID
     * @param dataSourceType DataSourceType
     * @param documentId Document ID
     * @param uriInfo URI info
     * @return {@link KafkaConnectInspectionResponse}
     */
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/project/{mappingDefinitionId}/document/{dataSourceType}/{documentId}")
    @Operation(summary = "Import Kafka Connect Document", description = "Import a Kafka Connect schema and return a Document object")
    @RequestBody(description = "KafkaConnectInspectionRequest object", content = @Content(schema = @Schema(implementation = KafkaConnectInspectionRequest.class)))
    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = KafkaConnectInspectionResponse.class)), description = "Return a Document object represented by KafkaConnectDocument"))
    public Response importKafkaConnectDocument(
            InputStream request,
            @Parameter(description = "Mapping Definition ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId,
            @Parameter(description = "DataSource Type") @PathParam("dataSourceType") DataSourceType dataSourceType,
            @Parameter(description = "Document ID") @PathParam("documentId") String documentId,
            @Context UriInfo uriInfo
        ) {
        long startTime = System.currentTimeMillis();

        KafkaConnectInspectionRequest inspectionRequest = fromJson(request, KafkaConnectInspectionRequest.class);
        KafkaConnectInspectionResponse response = new KafkaConnectInspectionResponse();
        KafkaConnectDocument d = null;

        try {
            DocumentMetadata metadata = createDocumentMetadataFrom(inspectionRequest, dataSourceType, documentId);
            DataSource dataSource = createDataSource(metadata);
            storeDocumentMetadata(mappingDefinitionId, dataSourceType, documentId, metadata, dataSource);
            storeDocumentSpecification(mappingDefinitionId, dataSourceType, documentId, new ByteArrayInputStream(inspectionRequest.getSchemaData().getBytes()));
            ADMArchiveHandler admHandler = getAtlasService().getADMArchiveHandler(mappingDefinitionId);
            DocumentKey docKey = new DocumentKey(dataSourceType, documentId);
            File specFile = admHandler.getDocumentSpecificationFile(docKey);
            performDocumentInspection(mappingDefinitionId, metadata, specFile);
            File f = admHandler.getDocumentInspectionResultFile(docKey);
            d = fromJson(new FileInputStream(f), KafkaConnectDocument.class);
        } catch (Exception e) {
            LOG.error("Error inspecting Kafka Connect schema: " + e.getMessage(), e);
            response.setErrorMessage(e.getMessage());
        } finally {
            response.setExecutionTime(System.currentTimeMillis() - startTime);
        }

        response.setKafkaConnectDocument(d);
        return Response.ok().entity(toJson(response)).build();
    }

    private DataSource createDataSource(DocumentMetadata meta) {
        DataSource answer = new DataSource();
        answer.setDataSourceType(meta.getDataSourceType());
        answer.setId(meta.getId());
        answer.setName(meta.getName());
        answer.setDescription(meta.getDescription());
        StringBuffer uri = new StringBuffer("atlas:kafkaconnect:");
        uri.append(meta.getId());
        answer.setUri(uri.toString());
        return answer;
    }

    @Override
    public void performDocumentInspection(Integer mappingDefinitionId, DocumentMetadata meta, File spec)
            throws AtlasException {
                ClassLoader loader = getAtlasService() != null
                ? getAtlasService().getLibraryLoader()
                : KafkaConnectService.class.getClassLoader();
        KafkaConnectInspectionService s = new KafkaConnectInspectionService(loader);

        String schemaTypeStr = meta.getInspectionParameters().get(KafkaConnectConstants.OPTIONS_SCHEMA_TYPE);
        KafkaConnectSchemaType schemaType = KafkaConnectSchemaType.valueOf(schemaTypeStr);
        HashMap<String, Object> options = KafkaConnectUtil.repackParserOptions(meta.getInspectionParameters());

        KafkaConnectDocument d;
        try {
            switch (schemaType) {
                case JSON:
                    d = s.inspectJson(new FileInputStream(spec), options);
                    break;
                case AVRO:
                    d = s.inspectAvro(new FileInputStream(spec), options);
                    break;
                default:
                    throw new AtlasException("Unsupported inspection type: " + schemaType);
            }
        } catch (FileNotFoundException e) {
            throw new AtlasException(e);
        }
        if (d != null) {
            storeInspectionResult(mappingDefinitionId, meta.getDataSourceType(), meta.getId(), d);
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
        return KafkaConnectService.LOG;
    }

}
