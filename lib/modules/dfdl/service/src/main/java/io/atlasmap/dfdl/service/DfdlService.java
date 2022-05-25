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
package io.atlasmap.dfdl.service;

import static io.atlasmap.dfdl.v2.DfdlConstants.OPTION_DFDL_SCHEMA_NAME;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.ADMArchiveHandler;
import io.atlasmap.dfdl.inspect.DfdlInspectionService;
import io.atlasmap.dfdl.v2.DfdlInspectionRequest;
import io.atlasmap.dfdl.v2.DfdlInspectionResponse;
import io.atlasmap.service.AtlasService;
import io.atlasmap.service.DocumentService;
import io.atlasmap.service.ModuleService;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.DocumentKey;
import io.atlasmap.v2.DocumentMetadata;
import io.atlasmap.v2.DocumentType;
import io.atlasmap.v2.Field;
import io.atlasmap.xml.v2.XmlDataSource;
import io.atlasmap.xml.v2.XmlDocument;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * DFDL Service provides DFDL inspection service which generate an AtlasMap Document object from DFDL instance or DFDL schema.
 */
@Path("/dfdl/")
public class DfdlService extends ModuleService {

    private static final Logger LOG = LoggerFactory.getLogger(DfdlService.class);

    @Context
    private ResourceContext resourceContext;

    /**
     * A constructor.
     * @param atlasService AtlasService
     * @param documentService DocumentService
     */
    public DfdlService(AtlasService atlasService, DocumentService documentService) {
        super(atlasService, documentService);
        getDocumentService().registerModuleService(DocumentType.DFDL, this);
    }

    /**
     * Import a DFDL schema or instance and return a Document object.
     * @param request {@link DfdlInspectionRequest}
     * @param mappingDefinitionId Mapping Definition ID
     * @param dataSourceType DataSourceType
     * @param documentId Document ID
     * @param uriInfo URI info
     * @return {@link DfdlInspectionResponse}
     */
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/project/{mappingDefinitionId}/document/{dataSourceType}/{documentId}")
    @Operation(summary = "Import DFDL", description = "Import a DFDL schema or instance and return a Document object")
    @RequestBody(description = "DfdlInspectionRequest object", content = @Content(schema = @Schema(implementation = DfdlInspectionRequest.class)))
    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = DfdlInspectionResponse.class)), description = "Return a Document object represented by XmlDocument"))
    public Response importDfdlDocument(
            InputStream request,
            @Parameter(description = "Mapping Definition ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId,
            @Parameter(description = "DataSource Type") @PathParam("dataSourceType") DataSourceType dataSourceType,
            @Parameter(description = "Document ID") @PathParam("documentId") String documentId,
            @Context UriInfo uriInfo
        ) {
        long startTime = System.currentTimeMillis();

        DfdlInspectionRequest inspectionRequest = fromJson(request, DfdlInspectionRequest.class);
        DfdlInspectionResponse response = new DfdlInspectionResponse();
        XmlDocument d = null;

        try {

            if (inspectionRequest.getInspectionType() == null) {
                response.setErrorMessage("Instance or Schema type must be specified in request");
                return Response.ok().entity(toJson(response)).build();
            }
            DocumentMetadata meta = createDocumentMetadataFrom(inspectionRequest, dataSourceType, documentId);
            ADMArchiveHandler admHandler = getAtlasService().getADMArchiveHandler(mappingDefinitionId);
            DocumentKey docKey = new DocumentKey(dataSourceType, documentId);
            performDocumentInspection(mappingDefinitionId, meta, null);
            File f = admHandler.getDocumentInspectionResultFile(docKey);
            d = fromJson(new FileInputStream(f), XmlDocument.class);
            XmlDataSource dataSource = createDataSource(meta, d);
            storeDocumentMetadata(mappingDefinitionId, dataSourceType, documentId, meta, dataSource);
        } catch (Exception e) {
            LOG.error("Error inspecting DFDL: " + e.getMessage(), e);
            response.setErrorMessage(e.getMessage());
        } finally {
            response.setExecutionTime(System.currentTimeMillis() - startTime);
        }

        response.setXmlDocument(d);
        return Response.ok().entity(toJson(response)).build();
    }

    private DocumentMetadata createDocumentMetadataFrom(DfdlInspectionRequest request, DataSourceType dsType, String documentId) {
        DocumentMetadata meta = super.createDocumentMetadataFrom(request, dsType, documentId);
        meta.getInspectionParameters().put(OPTION_DFDL_SCHEMA_NAME, request.getDfdlSchemaName());
        return meta;
    }

    private XmlDataSource createDataSource(DocumentMetadata meta, XmlDocument xmlDoc) {
        XmlDataSource answer = new XmlDataSource();
        answer.setDataSourceType(meta.getDataSourceType());
        answer.setId(meta.getId());
        answer.setName(meta.getName());
        answer.setDescription(meta.getDescription());
        StringBuffer uri = new StringBuffer("atlas:dfdl:");
        uri.append(meta.getId());
        answer.setUri(uri.toString());
        answer.setXmlNamespaces(xmlDoc.getXmlNamespaces());
        return answer;
    }

    @Override
    public void performDocumentInspection(Integer mappingDefinitionId, DocumentMetadata meta, File spec)
            throws AtlasException {
        ClassLoader loader = resourceContext != null
                ? resourceContext.getResource(AtlasService.class).getLibraryLoader()
                : DfdlService.class.getClassLoader();
        DfdlInspectionService s = new DfdlInspectionService(loader);

        XmlDocument d;
        try {
            switch (meta.getInspectionType()) {
                case INSTANCE:
                    d = s.inspectDfdlInstance(meta.getInspectionParameters().get(OPTION_DFDL_SCHEMA_NAME),
                            meta.getInspectionParameters());
                    break;
                case SCHEMA:
                    d = s.inspectDfdlSchema(meta.getInspectionParameters().get(OPTION_DFDL_SCHEMA_NAME),
                            meta.getInspectionParameters());
                    break;
                default:
                    throw new AtlasException("Unsupported inspection type: " + meta.getInspectionType());
            }
        } catch (AtlasException e) {
            throw e;
        } catch (Exception e2) {
            throw new AtlasException(e2);
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
        return DfdlService.LOG;
    }

}
