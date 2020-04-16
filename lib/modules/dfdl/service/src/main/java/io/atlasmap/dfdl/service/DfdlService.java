/**
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

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.atlasmap.v2.Json;
import io.atlasmap.dfdl.inspect.DfdlInspectionService;
import io.atlasmap.dfdl.v2.DfdlInspectionRequest;
import io.atlasmap.dfdl.v2.DfdlInspectionResponse;
import io.atlasmap.service.AtlasService;
import io.atlasmap.xml.v2.XmlDocument;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Path("/dfdl/")
public class DfdlService {

    private static final Logger LOG = LoggerFactory.getLogger(DfdlService.class);

    @Context
    private ResourceContext resourceContext;

    protected byte[] toJson(Object value) {
        try {
            return Json.mapper().writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    protected <T> T fromJson(InputStream value, Class<T>clazz) {
        try {
            return Json.mapper().readValue(value, clazz);
        } catch (IOException e) {
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
    }

    @GET
    @Path("/simple")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Simple", description = "Simple hello service")
    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = String.class)), description = "Return a response"))
    public String simpleHelloWorld(@QueryParam("from") String from) {
        return "Got it! " + from;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/inspect")
    @Operation(summary = "Inspect DFDL", description = "Inspect a DFDL schema or instance and return a Document object")
    @RequestBody(description = "DfdlInspectionRequest object", content = @Content(schema = @Schema(implementation = DfdlInspectionRequest.class)))
    @ApiResponses(@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = DfdlInspectionResponse.class)), description = "Return a Document object represented by XmlDocument"))
    public Response inspect(InputStream request) {
        return inspect(fromJson(request, DfdlInspectionRequest.class));
    }

    public Response inspect(DfdlInspectionRequest request) {
        long startTime = System.currentTimeMillis();

        DfdlInspectionResponse response = new DfdlInspectionResponse();
        XmlDocument d = null;

        try {

            if (request.getType() == null) {
                response.setErrorMessage("Instance or Schema type must be specified in request");
            } else {
                ClassLoader loader = resourceContext != null
                    ? resourceContext.getResource(AtlasService.class).getLibraryLoader()
                    : DfdlService.class.getClassLoader();
                DfdlInspectionService s = new DfdlInspectionService(loader);

                switch (request.getType()) {
                case INSTANCE:
                    d = s.inspectDfdlInstance(request.getDfdlSchemaName(), request.getOptions());
                    break;
                case SCHEMA:
                    d = s.inspectDfdlSchema(request.getDfdlSchemaName(), request.getOptions());
                    break;
                default:
                    response.setErrorMessage("Unsupported inspection type: " + request.getType());
                    break;
                }
            }
        } catch (Exception e) {
            LOG.error("Error inspecting DFDL: " + e.getMessage(), e);
            response.setErrorMessage(e.getMessage());
        } finally {
            response.setExecutionTime(System.currentTimeMillis() - startTime);
        }

        response.setXmlDocument(d);
        return Response.ok().entity(toJson(response)).build();
    }
}
