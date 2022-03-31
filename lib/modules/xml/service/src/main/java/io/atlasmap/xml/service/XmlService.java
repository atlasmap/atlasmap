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
package io.atlasmap.xml.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
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
import io.atlasmap.service.AtlasService;
import io.atlasmap.service.DocumentService;
import io.atlasmap.service.ModuleService;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.DocumentKey;
import io.atlasmap.v2.DocumentMetadata;
import io.atlasmap.v2.DocumentType;
import io.atlasmap.v2.Field;
import io.atlasmap.xml.inspect.XmlInspectionService;
import io.atlasmap.xml.v2.XmlDocument;
import io.atlasmap.xml.v2.XmlInspectionRequest;
import io.atlasmap.xml.v2.XmlInspectionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * XML Service provides XML inspection service which generate an AtlasMap Document object from XML instance or XML schema.
 */
@Path("/xml/")
public class XmlService extends ModuleService {

    private static final Logger LOG = LoggerFactory.getLogger(XmlService.class);

    /**
     * A constructor.
     * @param atlasService AtlasService
     * @param documentService DocumentService
     */
    public XmlService(AtlasService atlasService, DocumentService documentService) {
        super(atlasService, documentService);
        getDocumentService().registerModuleService(DocumentType.XML, this);
        getDocumentService().registerModuleService(DocumentType.XSD, this);
    }

    /**
     * Import a XML schema or instance and return a Document object.
     * @param request {@link XmlInspectionRequest}
     * @param mappingDefinitionId Mapping Definition ID
     * @param dataSourceType DataSourceType
     * @param documentId Document ID
     * @param uriInfo URI info
     * @return {@link XmlInspectionResponse}
     */
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/project/{mappingDefinitionId}/document/{dataSourceType}/{documentId}")
    @Operation(summary = "Import XML", description = "Import a XML schema or instance and return a Document object")
    @RequestBody(description = "XmlInspectionRequest object", content = @Content(schema = @Schema(implementation = XmlInspectionRequest.class)))
    @ApiResponses(@ApiResponse(responseCode = "200",  content = @Content(schema = @Schema(implementation = XmlInspectionResponse.class)), description = "Return a Document object represented by XmlDocument"))
    public Response importXmlDocument(
            InputStream request,
            @Parameter(description = "Mapping Definition ID") @PathParam("mappingDefinitionId") Integer mappingDefinitionId,
            @Parameter(description = "DataSource Type") @PathParam("dataSourceType") DataSourceType dataSourceType,
            @Parameter(description = "Document ID") @PathParam("documentId") String documentId,
            @Context UriInfo uriInfo
        ) {
        long startTime = System.currentTimeMillis();

        XmlInspectionRequest inspectionRequest = fromJson(request, XmlInspectionRequest.class);
        XmlInspectionResponse response = new XmlInspectionResponse();
        XmlDocument d = null;

        try {

            if (inspectionRequest.getInspectionType() == null) {
                response.setErrorMessage("Instance or Schema type must be specified in request");
                return Response.ok().entity(toJson(response)).build();
            }
            DocumentMetadata metadata = createDocumentMetadataFrom(inspectionRequest, dataSourceType, documentId);
            storeDocumentMetadata(mappingDefinitionId, dataSourceType, documentId, metadata);
            storeDocumentSpecification(mappingDefinitionId, dataSourceType, documentId, new ByteArrayInputStream(inspectionRequest.getXmlData().getBytes()));
            ADMArchiveHandler admHandler = getAtlasService().getADMArchiveHandler(mappingDefinitionId);
            DocumentKey docKey = new DocumentKey(dataSourceType, documentId);
            File specFile = admHandler.getDocumentSpecificationFile(docKey);
            performDocumentInspection(mappingDefinitionId, metadata, specFile);
            File f = admHandler.getDocumentInspectionResultFile(docKey);
            d = fromJson(new FileInputStream(f), XmlDocument.class);
        } catch (Exception e) {
            LOG.error("Error inspecting xml: " + e.getMessage(), e);
            response.setErrorMessage(e.getMessage());
        } finally {
            response.setExecutionTime(System.currentTimeMillis() - startTime);
        }

        AtlasUtil.excludeNotRequestedFields(d, inspectionRequest.getInspectPaths(), inspectionRequest.getSearchPhrase());

        response.setXmlDocument(d);
        return Response.ok().entity(toJson(response)).build();
    }

    @Override
    public void performDocumentInspection(Integer mappingDefinitionId, DocumentMetadata meta, File spec)
            throws AtlasException {
        XmlInspectionService s = new XmlInspectionService();
        XmlDocument d;
        switch (meta.getInspectionType()) {
            case INSTANCE:
                d = s.inspectXmlDocument(spec);
                break;
            case SCHEMA:
                d = s.inspectSchema(spec);
                break;
            default:
                throw new AtlasException("Unsupported inspection type: " + meta.getInspectionType());
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
        return XmlService.LOG;
    }

}

