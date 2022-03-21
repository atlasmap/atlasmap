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

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.core.AtlasUtil;
import io.atlasmap.service.ModuleService;
import io.atlasmap.v2.Field;
import io.atlasmap.xml.inspect.XmlInspectionService;
import io.atlasmap.xml.v2.XmlDocument;
import io.atlasmap.xml.v2.XmlInspectionRequest;
import io.atlasmap.xml.v2.XmlInspectionResponse;
import io.swagger.v3.oas.annotations.Operation;
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
     * Inspects a XML schema or instance and return a Document object.
     * @param request {@link XmlInspectionRequest}
     * @return {@link XmlInspectionResponse}
     */
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/inspect")
    @Operation(summary = "Inspect XML", description = "Inspect a XML schema or instance and return a Document object")
    @RequestBody(description = "XmlInspectionRequest object", content = @Content(schema = @Schema(implementation = XmlInspectionRequest.class)))
    @ApiResponses(@ApiResponse(responseCode = "200",  content = @Content(schema = @Schema(implementation = XmlInspectionResponse.class)), description = "Return a Document object represented by XmlDocument"))
    public Response inspect(InputStream request) {
        return inspect(fromJson(request, XmlInspectionRequest.class));
    }

    /**
     * Inspets a XML schema or instance and return a Document object.
     * @param request request
     * @return {@link XmlInspectionResponse}
     */
    public Response inspect(XmlInspectionRequest request) {
        long startTime = System.currentTimeMillis();

        XmlInspectionResponse response = new XmlInspectionResponse();
        XmlDocument d = null;

        try {

            if (request.getType() == null) {
                response.setErrorMessage("Instance or Schema type must be specified in request");
            } else {
                XmlInspectionService s = new XmlInspectionService();

                switch (request.getType()) {
                case INSTANCE:
                    d = s.inspectXmlDocument(request.getXmlData());
                    break;
                case SCHEMA:
                    d = s.inspectSchema(request.getXmlData());
                    break;
                default:
                    response.setErrorMessage("Unsupported inspection type: " + request.getType());
                    break;
                }
            }
        } catch (Exception e) {
            LOG.error("Error inspecting xml: " + e.getMessage(), e);
            response.setErrorMessage(e.getMessage());
        } finally {
            response.setExecutionTime(System.currentTimeMillis() - startTime);
        }

        AtlasUtil.excludeNotRequestedFields(d, request.getInspectPaths(), request.getSearchPhrase());

        response.setXmlDocument(d);
        return Response.ok().entity(toJson(response)).build();
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
}

